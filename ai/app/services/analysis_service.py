import json
import datetime
import asyncio
import math
import pandas as pd
import numpy as np
from statsmodels.tsa.statespace.sarimax import SARIMAX
from statsmodels.tsa.seasonal import seasonal_decompose
from statsmodels.tsa.holtwinters import ExponentialSmoothing
from pmdarima import auto_arima
import warnings
from sklearn.metrics import mean_absolute_error


from ..integrations.openai_clients import openai_client
from ..schemas.responses import (
    SalesForecastResponse,
    ForecastPoint,
    InventoryAnalysisResponse,
    ProductAbcXyz,
    ReorderRecommendationResponse,
    ReorderItem,
    DashboardSummaryResponse,
    SalesForecastLLMResponse,
    InventoryLLMResponse,
    ReorderRecommendationLLMResponse,
    ReorderItemLLM
)

warnings.filterwarnings('ignore')


class AnalysisService:
    def __init__(self):
        self.openai_client = openai_client

    def _validate_time_series(self, ts: pd.Series, min_non_zero_days: int = 7) -> tuple:
        """
        Kiểm tra tính hợp lệ của chuỗi thời gian.
        
        Args:
            ts: Pandas Series chứa dữ liệu thời gian
            min_non_zero_days: Số ngày tối thiểu có giao dịch
            
        Returns:
            (is_valid, non_zero_days, std_dev)
        """
        non_zero_days = (ts > 0).sum()
        std_dev = ts.std()
        is_valid = non_zero_days >= min_non_zero_days and std_dev > 0
        
        return is_valid, non_zero_days, std_dev

    def _fit_sarima_params(self, train: pd.Series) -> tuple:
        """
        Tìm tham số ARIMA(p,d,q)(P,D,Q)[m] bằng auto_arima với m=4 (monthly seasonality).
        """
        try:
            use_seasonal = len(train) >= 8
            model = auto_arima(
                train,
                seasonal=use_seasonal,
                m=4 if use_seasonal else 1,
                max_p=3, max_q=3,
                max_P=2, max_Q=2,
                max_d=2,
                error_action="ignore",
                suppress_warnings=True,
                stepwise=True
            )
            order = model.order
            seasonal_order = model.seasonal_order if use_seasonal else (0, 0, 0, 0)
            print(f"[DEBUG] ARIMA auto-tuning selected: ARIMA{order}x{seasonal_order} AIC={model.aic():.2f}")
            return order, seasonal_order, True
        except Exception as e:
            print(f"[DEBUG] auto_arima failed: {e}")
            return (1, 1, 0), (0, 0, 0, 0), True

    def _walk_forward_arima(
        self, ts: pd.Series, initial_split: int, n_steps: int,
        order: tuple, seasonal_order: tuple
    ) -> tuple:
        """
        Walk-forward (expanding-window) validation cho ARIMA.
        Returns: (mae, mape, predictions)
        """
        errors = []
        actuals = []
        preds = []
        for step in range(n_steps):
            train_wf = ts.iloc[:initial_split + step]
            actual   = float(ts.iloc[initial_split + step])
            try:
                m = SARIMAX(
                    train_wf,
                    order=order,
                    seasonal_order=seasonal_order,
                    enforce_stationarity=False,
                    enforce_invertibility=False
                )
                r = m.fit(disp=False, maxiter=200)
                pred = max(0.0, float(r.forecast(steps=1).iloc[0]))
                errors.append(abs(actual - pred))
                actuals.append(actual)
                preds.append(pred)
            except Exception as e:
                print(f"[DEBUG] ARIMA WF step {step} failed: {e}")
                errors.append(float("inf"))
                actuals.append(actual)
                preds.append(0.0)

        mae = float(np.mean(errors)) if errors else float("inf")
        pct_errors = []
        for act, pred in zip(actuals, preds):
            if act != 0:
                pct_errors.append(abs(act - pred) / act)
            else:
                pct_errors.append(0.0)
        mape = float(np.mean(pct_errors) * 100) if pct_errors else float("inf")
        
        print(f"[DEBUG] ARIMA{order} WF-MAE = {mae:.4f}, WF-MAPE = {mape:.2f}%")
        return mae, mape, preds

    def _calculate_ljungbox_pvalue(self, residuals) -> float:
        try:
            from statsmodels.stats.diagnostic import acorr_ljungbox
            n = len(residuals)
            lag = min(10, max(1, n // 5))
            lb_df = acorr_ljungbox(residuals, lags=[lag], return_df=True)
            p_val = float(lb_df['lb_pvalue'].iloc[0])
            return p_val
        except Exception as e:
            print(f"[DEBUG] Ljung-Box test calculation failed: {e}")
            return 1.0

    def _clip_outliers_iqr(self, ts: pd.Series) -> pd.Series:
        """
        Thay thế outlier bằng upper-fence (IQR method) để tránh ARIMA học trend giả.
        Các ERP lớn (SAP Analytics Cloud, Odoo Forecasting) đều bước này trước khi fit model.
        """
        q1, q3 = ts.quantile(0.25), ts.quantile(0.75)
        iqr = q3 - q1
        upper = q3 + 1.5 * iqr
        # Chỉ clip trên (không clip dưới vì revenue >= 0)
        clipped = ts.clip(upper=upper)
        n_clipped = int((ts > upper).sum())
        if n_clipped > 0:
            print(f"[INFO] Clipped {n_clipped} outlier weeks (upper fence={upper:.0f})")
        return clipped

    def _select_best_forecast(self, ts: pd.Series, val_size: int = 4, periods: int = 4) -> tuple:
        """
        Huấn luyen mo hinh SARIMA voi outlier treatment va fallback sang Holt-Winters
        khi MAPE > 30% (nguong chap nhan cho ERP forecasting).
        """
        # --- Step 0: Outlier treatment truoc khi fit ---
        ts_clean = self._clip_outliers_iqr(ts)

        is_valid, non_zero, std_dev = self._validate_time_series(ts_clean)
        if not is_valid or len(ts_clean) < val_size + 8:
            print(f"[WARNING] Series too short/invalid. len={len(ts_clean)}, non_zero={non_zero}")
            mean_val = max(0.0, float(ts_clean.mean())) if len(ts_clean) > 0 else 0.0
            fitted_naive = ts_clean.rolling(4, min_periods=1).mean()
            return [mean_val] * periods, "Naive Mean", {"mae": 0.0, "mape": 0.0, "aic": 0.0, "p_value": 1.0}, fitted_naive

        initial_split = len(ts_clean) - val_size
        train_initial = ts_clean.iloc[:initial_split]

        # --- Step 1: Thu SARIMA ---
        order, seasonal_order, arima_ok = self._fit_sarima_params(train_initial)
        mae, mape, preds = float('inf'), float('inf'), []
        forecast_values, model_info, metrics, fitted_values = [], "", {}, ts_clean

        if arima_ok:
            mae, mape, preds = self._walk_forward_arima(
                ts_clean, initial_split, val_size, order, seasonal_order
            )

        MAPE_THRESHOLD = 30.0  # Nguong chap nhan cua SAP / Odoo analytics
        use_sarima = arima_ok and mape <= MAPE_THRESHOLD

        if use_sarima:
            try:
                m_full = SARIMAX(
                    ts_clean, order=order, seasonal_order=seasonal_order,
                    enforce_stationarity=False, enforce_invertibility=False
                )
                r_full = m_full.fit(disp=False, maxiter=200)
                raw_forecast = r_full.forecast(steps=periods)

                # Cap forecast: khong vuot qua 2x max cua 4 tuan gan nhat
                recent_max = float(ts_clean.tail(4).max()) if len(ts_clean) >= 4 else float(ts_clean.max())
                cap = max(recent_max * 2.0, float(ts_clean.mean()) * 3.0)
                forecast_values = [min(cap, max(0.0, float(v))) for v in raw_forecast.values]

                aic = float(r_full.aic)
                p_val = self._calculate_ljungbox_pvalue(r_full.resid)
                model_info = f"SARIMA{order}x{seasonal_order}" if seasonal_order != (0,0,0,0) else f"ARIMA{order}"
                metrics = {"mae": mae, "mape": mape, "aic": aic, "p_value": p_val}

                # Fitted values = rolling 4-week smoothed actual (de hien thi duong xu huong ro rang)
                fitted_values = ts_clean.rolling(window=4, min_periods=1).mean()
                fitted_values = pd.Series(np.clip(fitted_values.values, 0.0, None), index=ts_clean.index)

                print("\n" + "="*50)
                print(f"  SARIMA MODEL METRICS (MAPE={mape:.1f}% <= {MAPE_THRESHOLD}% threshold)")
                print("="*50)
                print(f"Model: {model_info} | AIC={aic:.2f} | MAE={mae:.0f} | MAPE={mape:.2f}% | LB p={p_val:.4f}")
                print("="*50 + "\n")

            except Exception as e:
                print(f"[ERROR] SARIMA final fit failed: {e}")
                use_sarima = False

        # --- Step 2: Fallback -> Holt-Winters (Triple Exponential Smoothing) ---
        if not use_sarima:
            print(f"[INFO] SARIMA MAPE={mape:.1f}% > {MAPE_THRESHOLD}% threshold OR failed. Falling back to Holt-Winters ETS.")
            try:
                hw = ExponentialSmoothing(
                    ts_clean,
                    trend='add',
                    seasonal=None,  # weekly data too short for seasonal HW
                    damped_trend=True,  # damped trend prevents exponential blowup
                    initialization_method='estimated'
                )
                hw_fit = hw.fit(optimized=True)

                # Cap forecast giong SARIMA
                recent_max = float(ts_clean.tail(4).max()) if len(ts_clean) >= 4 else float(ts_clean.max())
                cap = max(recent_max * 2.0, float(ts_clean.mean()) * 3.0)
                raw_hw = hw_fit.forecast(periods)
                forecast_values = [min(cap, max(0.0, float(v))) for v in raw_hw]

                model_info = "Holt-Winters ETS (Damped Trend)"
                hw_mape = mape if mape != float('inf') else 0.0
                metrics = {"mae": mae if mae != float('inf') else 0.0,
                           "mape": hw_mape, "aic": 0.0, "p_value": 1.0}

                # Smooth trend line = rolling 4-week mean
                fitted_values = ts_clean.rolling(window=4, min_periods=1).mean()
                fitted_values = pd.Series(np.clip(fitted_values.values, 0.0, None), index=ts_clean.index)

                print(f"[INFO] Holt-Winters ETS fitted. Forecast: {[round(v,0) for v in forecast_values]}")

            except Exception as e2:
                print(f"[ERROR] Holt-Winters also failed: {e2}. Using naive rolling mean.")
                recent_mean = float(ts_clean.tail(4).mean()) if len(ts_clean) >= 4 else float(ts_clean.mean())
                forecast_values = [max(0.0, recent_mean)] * periods
                model_info = "Naive Rolling Mean (4-week)"
                metrics = {"mae": 0.0, "mape": 0.0, "aic": 0.0, "p_value": 1.0}
                fitted_values = ts_clean.rolling(window=4, min_periods=1).mean()
                fitted_values = pd.Series(np.clip(fitted_values.values, 0.0, None), index=ts_clean.index)

        return forecast_values, model_info, metrics, fitted_values


    async def analyze_sales_forecast(self, organization_id: str, history: list) -> SalesForecastResponse:
        """
        Dự báo doanh số 4 tuần tiếp theo sử dụng cỗi thời gian theo tuần (Weekly ETS / ARIMA).
        Dữ liệu thô (daily) được gộp theo tuần (W-MON), sau đó mô hình được đánh giá
        trên cùng tập validation 4 tuần trước khi chọn model tối ưu.
        """
        print(f"[DEBUG] analyze_sales_forecast received history size: {len(history)}")

        # --- Bước 1: Thu thập doanh thu theo ngày trong 180 ngày gần nhất ---
        end_date = datetime.datetime.now()
        daily_revenue = {}
        for day_idx in range(180):
            d_str = (end_date - datetime.timedelta(days=day_idx)).strftime("%Y-%m-%d")
            daily_revenue[d_str] = 0.0

        for row in history:
            d = row.get("date")
            if d:
                d_str = d.replace("T", " ").split(" ")[0]
                if d_str in daily_revenue:
                    daily_revenue[d_str] += float(row.get("revenue") or 0.0)

        non_zero_days = sum(1 for v in daily_revenue.values() if v > 0.0)
        print(f"[DEBUG] Found {non_zero_days} non-zero sales days in the last 180 days.")

        # --- Bước 2: Tạo chuỗi thời gian daily, sau đó resample sang weekly ---
        sorted_dates = sorted(daily_revenue.keys())
        y_hist = [daily_revenue[d] for d in sorted_dates]

        df_daily = pd.DataFrame({"revenue": y_hist}, index=pd.to_datetime(sorted_dates))
        df_daily = df_daily.asfreq("D", fill_value=0.0)
        daily_ts = df_daily["revenue"]

        # Gộp theo tuần bắt đầu Tứ Hai (W-MON), label = ngày đầu tuần
        weekly_ts = daily_ts.resample("W-MON", closed="left", label="left").sum()

        # Loại bỏ tuần hiện tại nếu chưa kết thúc (chưa đủ 7 ngày)
        today = pd.Timestamp.now(tz=None).normalize()
        current_week_start = today - pd.Timedelta(days=today.weekday())
        if len(weekly_ts) > 0 and weekly_ts.index[-1] >= current_week_start:
            weekly_ts = weekly_ts.iloc[:-1]

        num_weeks = len(weekly_ts)
        print(f"[DEBUG] Weekly series: {num_weeks} complete weeks available.")

        # --- Bước 3: Huấn luyện mô hình và dự báo 4 tuần tiếp theo ---
        forecast_values, model_info_str, metrics, fitted_values = self._select_best_forecast(
            weekly_ts, val_size=4, periods=4
        )
        print(f"[INFO] Selected model: {model_info_str} with metrics: {metrics}")

        # --- Bước 4: Tạo danh sách forecast_points (bao gồm lịch sử với fitted values và dự báo tương lai) ---
        forecast_points = []
        # Lấy 12 tuần lịch sử gần nhất
        history_start_idx = max(0, num_weeks - 12)
        for idx in range(history_start_idx, num_weeks):
            date_str = weekly_ts.index[idx].strftime("%Y-%m-%d")
            actual_rev = float(weekly_ts.values[idx])
            pred_rev = float(fitted_values.iloc[idx])
            
            forecast_points.append(
                ForecastPoint(
                    date=date_str,
                    historical_revenue=actual_rev,
                    predicted_revenue=round(pred_rev, 2)
                )
            )

        last_week_start = weekly_ts.index[-1]
        forecast_revenue_total = 0.0
        for i, pred_val in enumerate(forecast_values, 1):
            pred_y = float(pred_val)
            forecast_date = last_week_start + datetime.timedelta(weeks=i)

            forecast_revenue_total += pred_y
            forecast_points.append(
                ForecastPoint(
                    date=forecast_date.strftime("%Y-%m-%d"),
                    historical_revenue=None,
                    predicted_revenue=round(pred_y, 2)
                )
            )

        # --- Bước 5: LLM tạo tóm tắt nhận xét ---
        hist_total = float(weekly_ts.sum())
        hist_weekly_avg = hist_total / num_weeks if num_weeks > 0 else 0.0
        forecast_weekly_avg = forecast_revenue_total / 4.0

        prompt = (
            f"Here is the weekly sales forecast summary for organization {organization_id}:\n"
            f"- Historical period: Last {num_weeks} weeks (~180 days). "
            f"Total revenue: {hist_total:,.2f} USD (Weekly Average: {hist_weekly_avg:,.2f} USD/week)\n"
            f"- Forecast period: Next 4 weeks. "
            f"Forecasted total revenue: {forecast_revenue_total:,.2f} USD (Weekly Average: {forecast_weekly_avg:,.2f} USD/week)\n"
            f"Note: Compare the WEEKLY AVERAGES ({hist_weekly_avg:,.2f} vs {forecast_weekly_avg:,.2f} USD/week) "
            f"to determine if the sales trend is growing or declining.\n"
            f"Please write a brief business report (3-4 sentences) identifying the sales trend and "
            f"suggesting 3 concrete recommendations to optimize sales strategy. "
            f"Use plain English for employees. Do NOT mention model names, equations, or technical jargon."
        )

        messages = [
            {"role": "system", "content": "You are an ERP enterprise financial analyst. Write concise, realistic, and professional comments in plain English for company employees. Avoid statistical or machine learning jargon."},
            {"role": "user", "content": prompt}
        ]

        try:
            response = await self.openai_client.chat(
                messages=messages,
                response_format=SalesForecastLLMResponse
            )
            llm_resp = response.choices[0].message.parsed
            if not llm_resp:
                raise ValueError("Parsed LLM response is empty")
        except Exception as e:
            print(f"[WARNING] LLM chat failed, using fallback template. Error: {e}")
            is_growing = forecast_weekly_avg > hist_weekly_avg
            if is_growing:
                summary_text = (
                    f"Sales are showing a positive growth trend, with the projected weekly average "
                    f"increasing from ${hist_weekly_avg:,.2f} to ${forecast_weekly_avg:,.2f}. "
                    f"This indicates a steady rise in revenue heading into the next month. "
                    f"We should now focus on scaling our efforts to maintain this momentum."
                )
                insights_list = [
                    "Launch a targeted promotional campaign to capitalize on the current upward momentum.",
                    "Review current inventory levels to ensure enough stock is available to meet the increased demand.",
                    "Identify high-performing products from the last few weeks to prioritize them in marketing efforts."
                ]
            else:
                summary_text = (
                    f"Sales are projected to experience a slight decline or stabilization, with the projected "
                    f"weekly average moving from ${hist_weekly_avg:,.2f} to ${forecast_weekly_avg:,.2f}. "
                    f"It is recommended to run targeted promotions to stimulate demand."
                )
                insights_list = [
                    "Introduce special discount offers to stimulate demand and drive revenue.",
                    "Optimize stock levels to reduce holding costs and free up working capital.",
                    "Review sales performance of key categories to identify gaps or declining products."
                ]
            
            class FallbackLLMResponse:
                def __init__(self, summary, insights):
                    self.summary = summary
                    self.insights = insights
            
            llm_resp = FallbackLLMResponse(summary=summary_text, insights=insights_list)
        
        # Append mathematical and model metrics to insights for the user UI
        metric_insight = (
            f"Evaluation Metrics: Model = {model_info_str}, AIC = {metrics['aic']:.2f}, "
            f"Validation MAE = {metrics['mae']:.2f}, Validation MAPE = {metrics['mape']:.2f}%, "
            f"Ljung-Box p-value = {metrics['p_value']:.4f}."
        )
        if not llm_resp.insights:
            llm_resp.insights = []
        llm_resp.insights.append(metric_insight)

        parsed_resp = SalesForecastResponse(
            summary=llm_resp.summary,
            forecast_30d_total_revenue=round(forecast_revenue_total, 2),
            forecast_points=forecast_points,
            insights=llm_resp.insights
        )

        return parsed_resp

    async def analyze_inventory_abc_xyz(
        self, organization_id: str, warehouses: list, balances: list, sales: list, force_refresh: bool = False
    ) -> InventoryAnalysisResponse:
        """Phân tích ma trận ABC-XYZ, tính toán ROP, EOQ động dựa trên lịch sử mua bán và lượng tồn kho thực tế."""

        if not warehouses:
            return InventoryAnalysisResponse(
                summary="No warehouses found in the organization.",
                abc_xyz_matrix=[],
                critical_stock_count=0,
                recommendations=["Please create a warehouse and import products to start the analysis."]
            )

        # Map lưu trữ tồn kho hiện tại theo productId
        product_stock = {}
        product_names = {}
        
        for bal in balances:
            prod_id = bal.get("productId")
            prod_name = bal.get("productName")
            qty = float(bal.get("quantity") or 0.0)

            if prod_id:
                product_stock[prod_id] = product_stock.get(prod_id, 0.0) + qty
                if prod_name:
                    product_names[prod_id] = prod_name

        # Phân tích lượng bán của từng sản phẩm
        product_daily_sales = {}
        product_revenues = {}

        for item in sales:
            prod_id = item.get("productId")
            prod_name = item.get("productName")
            qty_sold = float(item.get("quantity") or 0.0)
            price = float(item.get("price") or 0.0)
            created_at_str = item.get("date") or datetime.datetime.now().isoformat()
            date_str = created_at_str.split("T")[0]

            if prod_id:
                if prod_name:
                    product_names[prod_id] = prod_name
                product_revenues[prod_id] = product_revenues.get(prod_id, 0.0) + (qty_sold * price)

                if prod_id not in product_daily_sales:
                    product_daily_sales[prod_id] = {}
                product_daily_sales[prod_id][date_str] = product_daily_sales[prod_id].get(date_str, 0.0) + qty_sold

        # ============================================================
        # ECONOMIC PARAMETERS — derived from actual transaction data
        # ============================================================
        #
        # 1. HOLDING COST RATE (I): ti le chi phi luu kho / nam tinh tren gia tri hang
        #    - Chi phi von chiem dung (opportunity cost): ~15%
        #    - Chi phi kho bai (warehouse, insurance): ~5%
        #    - Hao hut, loi thoi (shrinkage, obsolescence): ~5%
        #    => Tong: 25% / nam (chuan nganh: 20-30%)
        #    Nguon: Waters (2003) Inventory Control, Nahmias (2009) Production & Operations
        HOLDING_COST_RATE = 0.25

        # 2. ORDERING COST (S): chi phi xu ly 1 don dat hang (luong nhan vien mua hang, PO)
        #    Cong thuc uoc tinh: S = 1.5% doanh thu trung binh/thang/san pham
        #    Logic: quy mo giao dich tuong quan voi chi phi hanh chinh xu ly
        #    Gia tri toi thieu: 10 don vi (tranh EOQ = 0 khi san pham it giao dich)
        #    Nguon: Chopra & Meindl (2016) Supply Chain Management, 6th Ed.
        num_active_products = max(1, len(product_revenues))
        total_monthly_revenue = sum(product_revenues.values()) / 6.0  # 180 ngay ~ 6 thang
        avg_monthly_revenue_per_product = total_monthly_revenue / num_active_products
        ORDERING_COST = max(10.0, avg_monthly_revenue_per_product * 0.015)
        print(f"[DEBUG] EOQ params: HOLDING_RATE={HOLDING_COST_RATE:.0%}, "
              f"ORDERING_COST={ORDERING_COST:.2f} (avg_monthly_rev/product={avg_monthly_revenue_per_product:.2f})")

        # 3. WEIGHTED AVERAGE UNIT PRICE per product (de tinh H = unit_price x HOLDING_COST_RATE)
        product_weighted_price = {}
        product_total_qty_sold = {}
        for item in sales:
            _pid = item.get("productId")
            _qty = float(item.get("quantity") or 0.0)
            _price = float(item.get("price") or 0.0)
            if _pid and _qty > 0 and _price > 0:
                product_weighted_price[_pid] = product_weighted_price.get(_pid, 0.0) + (_price * _qty)
                product_total_qty_sold[_pid] = product_total_qty_sold.get(_pid, 0.0) + _qty
        for _pid in list(product_weighted_price.keys()):
            _total_qty = product_total_qty_sold.get(_pid, 0.0)
            if _total_qty > 0:
                product_weighted_price[_pid] = product_weighted_price[_pid] / _total_qty
        global_avg_price = (
            sum(product_weighted_price.values()) / len(product_weighted_price)
            if product_weighted_price else 1.0
        )

        # 4. LEAD TIME estimation per product (ngay)
        #    Tinh tu tan suat ban hang lam proxy cho tan suat nhap hang:
        #    LT = 50% khoang cach trung binh giua cac ngay co giao dich
        #    Gioi han: min=3 ngay, max=30 ngay
        #    Default: 7 ngay (chuan supply chain noi dia Viet Nam)
        product_sale_dates = {}
        for item in sales:
            _pid = item.get("productId")
            _date = (item.get("date") or "").split("T")[0]
            if _pid and _date:
                product_sale_dates.setdefault(_pid, set()).add(_date)

        def estimate_lead_time(pid: str) -> float:
            dates = sorted(product_sale_dates.get(pid, set()))
            if len(dates) < 2:
                return 7.0
            date_objs = [datetime.datetime.strptime(d, "%Y-%m-%d") for d in dates]
            gaps = [(date_objs[i+1] - date_objs[i]).days for i in range(len(date_objs)-1)]
            avg_gap = sum(gaps) / len(gaps)
            return max(3.0, min(avg_gap * 0.5, 30.0))

        # ============================================================

        # Tinh toan ABC-XYZ cho tung san pham
        abc_xyz_matrix = []
        critical_count = 0

        # ABC classification by revenue (Pareto 70/20/10)
        # Phuong phap chuan: Flores & Whybark (1987), chuong trinh APICS/CSCP
        sorted_prods_by_rev = sorted(product_revenues.items(), key=lambda x: x[1], reverse=True)
        total_rev_all = sum(product_revenues.values())

        abc_class = {}
        cum_rev = 0.0
        for pid, rev in sorted_prods_by_rev:
            prev_share = cum_rev / total_rev_all if total_rev_all > 0 else 0.0
            cum_rev += rev
            if prev_share < 0.70:
                abc_class[pid] = "A"
            elif prev_share < 0.90:
                abc_class[pid] = "B"
            else:
                abc_class[pid] = "C"

        all_product_ids = set(product_stock.keys()).union(product_names.keys())
        end_date = datetime.datetime.now()

        for pid in all_product_ids:
            if not pid:
                continue
            name = product_names.get(pid, f"Product {pid[:8]}")
            curr_stock = product_stock.get(pid, 0.0)

            # Daily demand statistics (90-day window)
            daily_sales_dict = product_daily_sales.get(pid, {})
            sales_values = [daily_sales_dict.get((end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d"), 0.0) for i in range(90)]
            mu = sum(sales_values) / 90.0        # mean daily demand (units/day)
            variance = sum((x - mu) ** 2 for x in sales_values) / 90.0
            sigma = math.sqrt(variance)           # std dev of daily demand

            # XYZ by Coefficient of Variation (CV = sigma / mu)
            # X: CV < 0.3 (stable demand), Y: 0.3-0.7 (moderate), Z: > 0.7 (erratic)
            cv = sigma / mu if mu > 0 else 9.9
            if cv < 0.3:
                xyz = "X"
            elif cv < 0.7:
                xyz = "Y"
            else:
                xyz = "Z"

            abc = abc_class.get(pid, "C")

            # ---- ROP (Reorder Point) ----
            # ROP = mean_demand * lead_time + safety_stock
            # Safety Stock = z * sigma * sqrt(LT), z=1.65 for 95% service level
            lead_time = estimate_lead_time(pid)
            safety_stock = 1.65 * sigma * math.sqrt(lead_time)
            if safety_stock < 2.0:
                safety_stock = 5.0
            rop = (mu * lead_time) + safety_stock
            if rop < 5.0:
                rop = 10.0

            # ---- EOQ (Economic Order Quantity) ----
            # EOQ = sqrt(2 * D * S / H)
            # D = annual demand (units/year)
            # S = ordering cost per order (derived from avg transaction data)
            # H = holding cost per unit per year = unit_price * HOLDING_COST_RATE
            annual_demand = mu * 365.0
            unit_price = product_weighted_price.get(pid, global_avg_price)
            H = unit_price * HOLDING_COST_RATE
            if annual_demand > 0 and H > 0:
                eoq = math.sqrt((2 * annual_demand * ORDERING_COST) / H)
            else:
                eoq = 30.0
            if eoq < 10.0:
                eoq = 30.0

            # Xác định trạng thái
            if curr_stock < rop * 0.5:
                status = "CRITICAL"
                critical_count += 1
            elif curr_stock < rop:
                status = "WARNING"
            else:
                status = "OK"

            abc_xyz_matrix.append(
                ProductAbcXyz(
                    productId=pid,
                    productName=name,
                    abcClass=abc,
                    xyzClass=xyz,
                    currentStock=curr_stock,
                    rop=round(rop, 1),
                    eoq=round(eoq, 1),
                    status=status
                )
            )

        # LLM tóm tắt
        critical_items = [item for item in abc_xyz_matrix if item.status in ["CRITICAL", "WARNING"]]
        summary_prompt = (
            f"Here is the summary of the organization's actual inventory data:\n"
            f"- Total analyzed items: {len(abc_xyz_matrix)}\n"
            f"- Number of products below reorder point (ROP) (needs restock): {len(critical_items)} (including {critical_count} at CRITICAL level)\n"
            f"- Representative shortage products: {', '.join([f'{x.productName} (Stock: {x.currentStock}/{x.rop} ROP)' for x in critical_items[:5]])}\n"
            f"Please write a brief inventory analysis report (3-4 sentences) pointing out the risk of supply chain disruption and propose 3 solutions to improve inventory management. Write in plain, clear English for warehouse staff."
        )

        messages = [
            {"role": "system", "content": "You are a smart ERP logistics manager. Write professional comments in plain English for warehouse staff. Avoid complex statistics jargon."},
            {"role": "user", "content": summary_prompt}
        ]

        try:
            response = await self.openai_client.chat(
                messages=messages,
                response_format=InventoryLLMResponse
            )
            llm_resp = response.choices[0].message.parsed
            if not llm_resp:
                raise ValueError("Parsed LLM response is empty")
        except Exception as e:
            print(f"[WARNING] LLM inventory analysis chat failed, using fallback template. Error: {e}")
            if len(critical_items) > 0:
                summary_text = (
                    f"There are currently {len(critical_items)} items with stock levels below their reorder points, "
                    f"presenting a risk of supply chain disruption. Immediate action is needed to restock "
                    f"critical items and adjust safety stock configurations."
                )
                recs_list = [
                    "Initiate replenishment orders for critical products immediately.",
                    "Verify warehouse safety stock parameters to prevent future stockouts.",
                    "Coordinate with suppliers to prioritize outstanding purchase orders."
                ]
            else:
                summary_text = (
                    "Inventory levels are currently stable across all items, with no products reported below "
                    "their reorder points. We should continue monitoring stock levels to maintain operational continuity."
                )
                recs_list = [
                    "Continue regular inventory cycle counting to ensure data accuracy.",
                    "Analyze seasonal demand patterns to optimize safety stock limits.",
                    "Maintain standard procurement cycles for stable products."
                ]
            
            class FallbackInventoryLLMResponse:
                def __init__(self, summary, recommendations):
                    self.summary = summary
                    self.recommendations = recommendations
            
            llm_resp = FallbackInventoryLLMResponse(summary=summary_text, recommendations=recs_list)
        
        parsed_resp = InventoryAnalysisResponse(
            summary=llm_resp.summary,
            abc_xyz_matrix=abc_xyz_matrix,
            critical_stock_count=len(critical_items),
            recommendations=llm_resp.recommendations
        )

        return parsed_resp

    async def get_inventory_alerts(self, organization_id: str, warehouses: list, balances: list, sales: list) -> list:
        """Lấy nhanh các mặt hàng cảnh báo hết hàng."""
        analysis = await self.analyze_inventory_abc_xyz(organization_id, warehouses, balances, sales)
        return [item for item in analysis.abc_xyz_matrix if item.status in ["CRITICAL", "WARNING"]]

    async def get_reorder_recommendations(self, organization_id: str, warehouses: list, balances: list, sales: list) -> ReorderRecommendationResponse:
        """Đề xuất lượng nhập tối ưu cho các sản phẩm dưới ROP."""
        analysis = await self.analyze_inventory_abc_xyz(organization_id, warehouses, balances, sales)
        
        default_wh_id = warehouses[0]["id"] if warehouses else ""
        default_wh_name = warehouses[0]["name"] if warehouses else "Kho chính"

        product_wh_map = {}
        for b in balances:
            prod_id = b.get("productId")
            wh_id = b.get("warehouseId")
            wh_name = b.get("warehouseName")
            if prod_id and wh_id:
                product_wh_map[prod_id] = (wh_id, wh_name or "Kho chính")

        reorder_items = []
        for prod in analysis.abc_xyz_matrix:
            if prod.status in ["CRITICAL", "WARNING"]:
                wh_id, wh_name = product_wh_map.get(prod.productId, (default_wh_id, default_wh_name))
                recommended_qty = max(prod.eoq, (prod.rop - prod.currentStock) + prod.eoq)
                urgency = "HIGH" if prod.status == "CRITICAL" or prod.abcClass == "A" else "MEDIUM"
                note = f"Actual stock ({prod.currentStock}) is below the ROP ({prod.rop}). Recommended to order {recommended_qty} units of product group {prod.abcClass}-{prod.xyzClass}."

                reorder_items.append(
                    ReorderItem(
                        productId=prod.productId,
                        productName=prod.productName,
                        warehouseId=wh_id,
                        warehouseName=wh_name,
                        currentStock=prod.currentStock,
                        rop=prod.rop,
                        eoq=prod.eoq,
                        recommendedQuantity=round(recommended_qty, 1),
                        urgency=urgency,
                        notes=note
                    )
                )

        if reorder_items:
            prod_class_map = {p.productId: (p.abcClass, p.xyzClass) for p in analysis.abc_xyz_matrix}
            llm_input_lines = []
            for x in reorder_items[:5]:
                abc, xyz = prod_class_map.get(x.productId, ("C", "Z"))
                llm_input_lines.append(
                    f"- ID: {x.productId}, Name: {x.productName} (Class: {abc}{xyz}, Stock: {x.currentStock}/{x.rop} ROP, Recommended Qty: {x.recommendedQuantity})"
                )
            
            prompt = (
                "Please write a simple restocking reason (notes) in clear business English for the following list of warehouse recommendations. "
                "State why we need to order more, highlighting the importance of the product based on its classification:\n"
                + "\n".join(llm_input_lines)
            )

            messages = [
                {
                    "role": "system",
                    "content": "You are an AI supply chain assistant. Write extremely concise note fields (maximum 15 words) for each product in plain English, clearly stating the reason they need restocking. Return the correct product ID.",
                },
                {"role": "user", "content": prompt}
            ]

            try:
                response = await self.openai_client.chat(
                    messages=messages,
                    response_format=ReorderRecommendationLLMResponse
                )
                parsed_recs = response.choices[0].message.parsed
                notes_map = {item.productId: item.notes for item in parsed_recs.recommendations if item.productId}
                
                for item in reorder_items:
                    if item.productId in notes_map:
                        item.notes = notes_map[item.productId]
            except Exception:
                pass

        return ReorderRecommendationResponse(recommendations=reorder_items)

    async def get_dashboard_summary(self, organization_id: str, history: list, inventory_data: dict) -> DashboardSummaryResponse:
        """Tạo Daily Brief tóm tắt nhanh tình trạng bán hàng và tồn kho."""
        try:
            sales = await self.analyze_sales_forecast(organization_id, history)
            
            whs = inventory_data.get("warehouses", [])
            bals = inventory_data.get("balances", [])
            sles = inventory_data.get("sales", [])
            
            inv = await self.analyze_inventory_abc_xyz(organization_id, whs, bals, sles)
            
            critical_count = inv.critical_stock_count
            predicted_sales = sales.forecast_30d_total_revenue
        except Exception:
            critical_count = 0
            predicted_sales = 0.0

        prompt = (
            f"Please compose a Daily Brief in clear business English, extremely concise (about 3 sentences), for the CEO:\n"
            f"- Forecasted sales for the next 15 days: {predicted_sales:,.2f} USD\n"
            f"- Stock alerts: {critical_count} products are running low below the ROP.\n"
            f"Use an inspiring, concise tone, highlighting the immediate action to take today."
        )

        messages = [
            {"role": "system", "content": "You are the CEO's executive AI assistant. Write a concise, polite summary focusing on immediate actions, written in clear business English."},
            {"role": "user", "content": prompt}
        ]

        try:
            response = await self.openai_client.chat(
                messages=messages,
                response_format=DashboardSummaryResponse
            )
            return response.choices[0].message.parsed
        except Exception as e:
            print(f"[WARNING] LLM dashboard summary chat failed, using fallback template. Error: {e}")
            summary_text = (
                f"Welcome back. Sales for the next month are projected to reach ${predicted_sales:,.2f} USD. "
                f"Currently, there are {critical_count} products with low stock levels below their reorder points. "
                f"Please review pending sales orders and low-stock replenishment requests to optimize operations today."
            )
            alerts_list = [
                f"30-Day Sales Forecast: ${predicted_sales:,.2f} USD."
            ]
            if critical_count > 0:
                alerts_list.append(f"Low Stock Alert: {critical_count} items below ROP.")
            
            return DashboardSummaryResponse(summary=summary_text, alerts=alerts_list)


analysis_service = AnalysisService()
