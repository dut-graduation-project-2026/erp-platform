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
        Tìm tham số ARIMA(p,d,q) không seasonal bằng auto_arima.
        Seasonal bị tắt vì lượng dữ liệu (~25 tuần) chưa đủ để học mùa vụ tin cậy.

        Returns:
            (order, seasonal_order, success)
        """
        try:
            model = auto_arima(
                train,
                seasonal=False,          # không seasonal
                max_p=3, max_q=3,
                max_d=2,
                error_action="ignore",
                suppress_warnings=True,
                stepwise=True
            )
            order = model.order
            seasonal_order = (0, 0, 0, 0)  # không có thành phần seasonal
            print(f"[DEBUG] ARIMA params selected: ARIMA{order} AIC={model.aic():.2f}")
            return order, seasonal_order, True
        except Exception as e:
            print(f"[DEBUG] auto_arima failed: {e}")
            return None, None, False

    def _walk_forward_arima(
        self, ts: pd.Series, initial_split: int, n_steps: int,
        order: tuple, seasonal_order: tuple
    ) -> float:
        """
        Walk-forward (expanding-window) validation cho ARIMA.
        T\u1ea1i m\u1ed7i b\u01b0\u1edbc t, train tr\u00ean ts[:initial_split+t], d\u1ef1 b\u00e1o 1 b\u01b0\u1edbc, \u0111o l\u01b0\u1eddng abs error.
        ARIMA params (order, seasonal_order) \u0111\u01b0\u1ee3c gi\u1eef c\u1ed1 \u0111\u1ecbnh (fitted m\u1ed9t l\u1ea7n tr\u01b0\u1edbc khi v\u00f2ng l\u1eb7p).

        Returns: mean MAE qua n_steps b\u01b0\u1edbc.
        """
        errors = []
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
                pred = max(0.0, float(r.forecast(steps=1)[0]))
                errors.append(abs(actual - pred))
            except Exception as e:
                print(f"[DEBUG] ARIMA WF step {step} failed: {e}")
                errors.append(float("inf"))

        mae = float(np.mean(errors)) if errors else float("inf")
        print(f"[DEBUG] ARIMA{order} WF-MAE (avg {n_steps} steps) = {mae:.4f}")
        return mae

    def _walk_forward_ets(
        self, ts: pd.Series, initial_split: int, n_steps: int, cfg: dict
    ) -> float:
        """
        Walk-forward (expanding-window) validation cho m\u1ed9t c\u1ea5u h\u00ecnh ETS.
        T\u1ea1i m\u1ed7i b\u01b0\u1edbc t, refit ExponentialSmoothing tr\u00ean ts[:initial_split+t],
        d\u1ef1 b\u00e1o 1 b\u01b0\u1edbc, \u0111o l\u01b0\u1eddng abs error.

        Returns: mean MAE qua n_steps b\u01b0\u1edbc.
        """
        errors = []
        for step in range(n_steps):
            train_wf = ts.iloc[:initial_split + step]
            actual   = float(ts.iloc[initial_split + step])
            try:
                kwargs = {
                    "trend": cfg["trend"],
                    "seasonal": cfg["seasonal"],
                    "initialization_method": "estimated",
                }
                if cfg["trend"] is not None:
                    kwargs["damped_trend"] = cfg["damped_trend"]
                hw = ExponentialSmoothing(train_wf, **kwargs).fit(optimized=True)
                pred = max(0.0, float(hw.forecast(steps=1)[0]))
                errors.append(abs(actual - pred))
            except Exception as e:
                print(f"[DEBUG] ETS {cfg['label']} WF step {step} failed: {e}")
                errors.append(float("inf"))

        mae = float(np.mean(errors)) if errors else float("inf")
        print(f"[DEBUG] ETS {cfg['label']} WF-MAE (avg {n_steps} steps) = {mae:.4f}")
        return mae

    def _select_best_forecast(self, ts: pd.Series, val_size: int = 4, periods: int = 4) -> tuple:
        """
        Ch\u1ecdn model t\u1ed1t nh\u1ea5t gi\u1eefa ARIMA v\u00e0 ETS b\u1eb1ng Walk-forward Validation.

        Ph\u01b0\u01a1ng ph\u00e1p:
          - initial_split = len(ts) - val_size
          - V\u1edbi m\u1ed7i step t \u2208 [0, val_size):
              \u2022 Train tr\u00ean ts[:initial_split + t]   (expanding window)
              \u2022 D\u1ef1 b\u00e1o ts[initial_split + t]        (1 b\u01b0\u1edbc ti\u1ebfp theo)
              \u2022 Ghi l\u1ea1i |actual \u2212 pred|
          - MAE trung b\u00ecnh qua val_size b\u01b0\u1edbc l\u00e0 ti\u00eau ch\u00ed ch\u1ecdn model.
          - Model th\u1eafng \u0111\u01b0\u1ee3c retrain tr\u00ean to\u00e0n b\u1ed9 ts \u0111\u1ec3 t\u1ea1o forecast cu\u1ed1i.

        Args:
            ts:       Weekly time series (pd.Series)
            val_size: S\u1ed1 b\u01b0\u1edbc walk-forward (= forecast horizon = 4 tu\u1ea7n)
            periods:  S\u1ed1 b\u01b0\u1edbc d\u1ef1 b\u00e1o cu\u1ed1i c\u00f9ng

        Returns:
            (forecast_values, model_info_str)
        """
        is_valid, non_zero, std_dev = self._validate_time_series(ts)
        if not is_valid or len(ts) < val_size + 8:
            print(f"[WARNING] Series too short/invalid. len={len(ts)}, non_zero={non_zero}")
            mean_val = max(0.0, float(ts.mean())) if len(ts) > 0 else 0.0
            return [mean_val] * periods, "Naive Mean"

        initial_split = len(ts) - val_size   # training window grows from here
        train_initial = ts.iloc[:initial_split]

        # ── ARIMA: t\u00ecm params m\u1ed9t l\u1ea7n, d\u00f9ng c\u1ed1 \u0111\u1ecbnh trong walk-forward ──
        arima_wf_mae  = float("inf")
        arima_order   = None
        arima_s_order = None

        order, seasonal_order, arima_ok = self._fit_sarima_params(train_initial)
        if arima_ok:
            arima_order   = order
            arima_s_order = seasonal_order
            arima_wf_mae  = self._walk_forward_arima(
                ts, initial_split, val_size, order, seasonal_order
            )

        # ── ETS: th\u1eed t\u1ea5t c\u1ea3 configs, ch\u1ecdn config c\u00f3 walk-forward MAE th\u1ea5p nh\u1ea5t ──
        ets_configs = [
            {"trend": "add", "damped_trend": False, "seasonal": None, "label": "Holt-Linear", "sp": None},
            {"trend": "add", "damped_trend": True,  "seasonal": None, "label": "Holt-Damped", "sp": None},
            {"trend": None,  "damped_trend": False, "seasonal": None, "label": "SES",         "sp": None},
        ]

        best_ets_mae = float("inf")
        best_ets_cfg = None
        for cfg in ets_configs:
            mae = self._walk_forward_ets(ts, initial_split, val_size, cfg)
            if mae < best_ets_mae:
                best_ets_mae = mae
                best_ets_cfg = cfg

        print(
            f"[MODEL SELECTION] ARIMA WF-MAE={arima_wf_mae:.4f} | "
            f"Best ETS ({best_ets_cfg['label'] if best_ets_cfg else 'none'}) WF-MAE={best_ets_mae:.4f}"
        )

        # ── Retrain th\u1eafng tr\u00ean to\u00e0n b\u1ed9 ts ──
        if arima_ok and arima_wf_mae <= best_ets_mae:
            try:
                m_full = SARIMAX(
                    ts,
                    order=arima_order,
                    seasonal_order=arima_s_order,
                    enforce_stationarity=False,
                    enforce_invertibility=False
                )
                r_full = m_full.fit(disp=False, maxiter=200)
                future = r_full.forecast(steps=periods)
                forecast_values = [max(0.0, float(v)) for v in future.values]
                model_info = f"ARIMA{arima_order}"
                print(f"[MODEL SELECTION] \u2192 Chosen: {model_info} (WF-MAE={arima_wf_mae:.4f})")
                return forecast_values, model_info
            except Exception as e:
                print(f"[ERROR] ARIMA final forecast failed: {e}")
                # fall through to ETS

        if best_ets_cfg is not None:
            try:
                kwargs_full = {
                    "trend": best_ets_cfg["trend"],
                    "seasonal": None,
                    "initialization_method": "estimated",
                }
                if best_ets_cfg["trend"] is not None:
                    kwargs_full["damped_trend"] = best_ets_cfg["damped_trend"]
                hw_full = ExponentialSmoothing(ts, **kwargs_full).fit(optimized=True)
                future  = hw_full.forecast(steps=periods)
                forecast_values = [max(0.0, float(v)) for v in future.values]
                model_info = best_ets_cfg["label"]
                print(f"[MODEL SELECTION] \u2192 Chosen: {model_info} (WF-MAE={best_ets_mae:.4f})")
                return forecast_values, model_info
            except Exception as e:
                print(f"[ERROR] ETS final forecast failed: {e}")

        # C\u1ea3 hai th\u1ea5t b\u1ea1i \u2192 Naive rolling mean 4 tu\u1ea7n
        print("[MODEL SELECTION] \u2192 Both models failed. Using Naive 4-week rolling mean.")
        mean_val = max(0.0, float(ts.tail(4).mean()) if len(ts) >= 4 else float(ts.mean()))
        return [mean_val] * periods, "Naive 4-Week Rolling Mean"


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

        # --- Bước 3: Lấy 12 tuần lịch sử gần nhất làm forecast_points ---
        forecast_points = []
        for idx in range(max(0, num_weeks - 12), num_weeks):
            forecast_points.append(
                ForecastPoint(
                    date=weekly_ts.index[idx].strftime("%Y-%m-%d"),
                    historical_revenue=float(weekly_ts.values[idx]),
                    predicted_revenue=float(weekly_ts.values[idx])
                )
            )

        # --- Bước 4: Dự báo 4 tuần tiếp theo ---
        # Đánh giá ARIMA và ETS (Holt-Winters) trên cùng validation set 4 tuần
        forecast_revenue_total = 0.0
        forecast_values, model_info_str = self._select_best_forecast(
            weekly_ts, val_size=4, periods=4
        )
        print(f"[INFO] Selected model: {model_info_str}")

        last_week_start = weekly_ts.index[-1]
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

        response = await self.openai_client.chat(
            messages=messages,
            response_format=SalesForecastLLMResponse
        )

        llm_resp = response.choices[0].message.parsed

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

        # Tính toán ABC-XYZ cho từng sản phẩm
        abc_xyz_matrix = []
        critical_count = 0

        # Phân loại ABC dựa trên Doanh thu
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

            # Phân tích nhu cầu hàng ngày
            daily_sales_dict = product_daily_sales.get(pid, {})
            sales_values = [daily_sales_dict.get((end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d"), 0.0) for i in range(90)]
            
            mu = sum(sales_values) / 90.0
            variance = sum((x - mu) ** 2 for x in sales_values) / 90.0
            sigma = math.sqrt(variance)

            # Phân loại XYZ
            cv = sigma / mu if mu > 0 else 9.9
            if cv < 0.3:
                xyz = "X"
            elif cv < 0.7:
                xyz = "Y"
            else:
                xyz = "Z"

            abc = abc_class.get(pid, "C")

            # Tính ROP, EOQ, Safety Stock
            lead_time = 5.0
            safety_stock = 1.65 * sigma * math.sqrt(lead_time)
            
            if safety_stock < 2.0:
                safety_stock = 5.0
            
            rop = (mu * lead_time) + safety_stock
            if rop < 5.0:
                rop = 10.0

            annual_demand = mu * 365.0
            eoq = math.sqrt((2 * annual_demand * 50.0) / 2.0) if annual_demand > 0 else 30.0
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

        response = await self.openai_client.chat(
            messages=messages,
            response_format=InventoryLLMResponse
        )

        llm_resp = response.choices[0].message.parsed
        
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

        response = await self.openai_client.chat(
            messages=messages,
            response_format=DashboardSummaryResponse
        )
        return response.choices[0].message.parsed


analysis_service = AnalysisService()
