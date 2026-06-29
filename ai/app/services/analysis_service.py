import json
import datetime
import asyncio
import math
import pandas as pd
import numpy as np
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

    def _select_best_forecast(self, ts: pd.Series, val_size: int = 4, periods: int = 4) -> tuple:
        """
        Forecast sales using Classical Decomposition (ACCA Method).
        """
        is_valid, non_zero, std_dev = self._validate_time_series(ts)
        if not is_valid or len(ts) < val_size + 8:
            print(f"[WARNING] Series too short/invalid for ACCA. len={len(ts)}, non_zero={non_zero}")
            mean_val = max(0.0, float(ts.mean())) if len(ts) > 0 else 0.0
            return [mean_val] * periods, "Naive Mean"

        try:
            forecast_values = self._forecast_classical_decomposition(ts, periods)
            model_info = "Classical Decomposition (ACCA)"
            print(f"[FORECAST] Chosen: {model_info} (Generated forecast for next {periods} weeks)")
            return forecast_values, model_info
        except Exception as e:
            print(f"[ERROR] Classical Decomposition final forecast failed: {e}")
            last_val = max(0.0, float(ts.iloc[-1])) if len(ts) > 0 else 0.0
            return [last_val] * periods, "Naive Forecast (Last Observed Value)"

    def _validate_model_accuracy(self, ts: pd.Series, val_size: int = 4) -> dict:
        """
        Evaluate forecast accuracy using out-of-sample backtesting (validation split).
        """
        n = len(ts)
        if n < val_size + 8:
            return {
                "model_name": "Naive Mean (Fallback)",
                "mape": 0.0,
                "mae": 0.0,
                "r2": 0.0
            }

        try:
            # 1. Split into training and validation
            train_ts = ts.iloc[:-val_size]
            val_ts = ts.iloc[-val_size:]

            # 2. Run forecasting on training set
            val_preds = self._forecast_classical_decomposition(train_ts, val_size)
            actuals = val_ts.values
            preds = np.array(val_preds)

            # 3. Calculate metrics
            mae = float(np.mean(np.abs(actuals - preds)))
            
            # Avoid division by zero for MAPE
            non_zero_mask = actuals > 0
            if np.sum(non_zero_mask) > 0:
                mape = float(np.mean(np.abs((actuals[non_zero_mask] - preds[non_zero_mask]) / actuals[non_zero_mask])) * 100)
            else:
                mape = 0.0

            # R-squared (coefficient of determination)
            ss_res = np.sum((actuals - preds) ** 2)
            ss_tot = np.sum((actuals - np.mean(actuals)) ** 2)
            r2 = float(1.0 - (ss_res / ss_tot)) if ss_tot > 0 else 1.0

            return {
                "model_name": "ACCA Classical Decomposition",
                "mape": min(100.0, max(0.0, mape)),
                "mae": mae,
                "r2": min(1.0, max(0.0, r2))
            }
        except Exception as e:
            print(f"[ERROR] Accuracy evaluation failed: {e}")
            return {
                "model_name": "Naive Mean (Fallback)",
                "mape": 0.0,
                "mae": 0.0,
                "r2": 0.0
            }

    def _forecast_classical_decomposition(
        self, ts: pd.Series, periods: int, seasonal_period: int = 4
    ) -> list:
        """
        Classical multiplicative decomposition forecasting using OLS regression and moving average.
        """
        n = len(ts)
        if n < seasonal_period * 2:
            # Không đủ dữ liệu để tính toán mùa vụ, chỉ dùng Hồi quy tuyến tính đơn giản
            x = np.arange(n)
            y = ts.values
            try:
                slope, intercept = np.polyfit(x, y, 1)
                future_x = np.arange(n, n + periods)
                preds = slope * future_x + intercept
                return [max(0.0, float(v)) for v in preds]
            except Exception:
                return [max(0.0, float(ts.mean()))] * periods

        # 1. Tính toán Xu hướng bằng Trung bình trượt (Moving Average)
        ma = ts.rolling(window=seasonal_period, center=True).mean()
        # Đắp bù các phần đầu và cuối bị NaN bằng nội suy tuyến tính/ngoại suy
        ma = ma.interpolate(method="linear", limit_direction="both").fillna(ts.mean())

        # 2. Tách chỉ số mùa vụ dạng nhân tử (Multiplicative Seasonality: Y_t / Trend_t)
        seasonal_ratios = ts / ma
        
        seasonal_factors = [0.0] * seasonal_period
        for i in range(seasonal_period):
            indices = [idx for idx in range(i, n, seasonal_period)]
            ratios_for_week = [float(seasonal_ratios.iloc[idx]) for idx in indices if not pd.isna(seasonal_ratios.iloc[idx])]
            if ratios_for_week:
                seasonal_factors[i] = np.mean(ratios_for_week)
            else:
                seasonal_factors[i] = 1.0

        # Chuẩn hóa để trung bình các nhân tử mùa vụ = 1.0
        sum_factors = sum(seasonal_factors)
        if sum_factors > 0:
            seasonal_factors = [f * (seasonal_period / sum_factors) for f in seasonal_factors]
        else:
            seasonal_factors = [1.0] * seasonal_period

        # 3. Khử mùa vụ (Deseasonalize) để thực hiện Hồi quy tuyến tính
        deseasonalized = ts.copy()
        for idx in range(n):
            sf = seasonal_factors[idx % seasonal_period]
            deseasonalized.iloc[idx] = ts.iloc[idx] / sf if sf > 0 else ts.iloc[idx]

        # Khớp đường xu hướng tuyến tính (Linear Regression) trên chuỗi đã khử mùa vụ
        t_indices = np.arange(n)
        try:
            slope, intercept = np.polyfit(t_indices, deseasonalized.values, 1)
        except Exception:
            slope, intercept = 0.0, float(deseasonalized.mean())

        # 4. Dự báo các tuần tương lai t + h
        forecast_values = []
        for h in range(1, periods + 1):
            future_t = n + h - 1
            trend_val = slope * future_t + intercept
            seasonal_factor = seasonal_factors[future_t % seasonal_period]
            pred_val = trend_val * seasonal_factor
            forecast_values.append(max(0.0, float(pred_val)))

        return forecast_values


    async def analyze_sales_forecast(self, organization_id: str, history: list) -> SalesForecastResponse:
        """
        Forecast weekly sales for the next 4 weeks using the ACCA Classical Decomposition method.
        """
        print(f"[DEBUG] analyze_sales_forecast received history size: {len(history)}")

        # --- Step 1: Collect daily revenue for the past 180 days ---
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

        # --- Step 2: Create daily time series, then resample to weekly ---
        sorted_dates = sorted(daily_revenue.keys())
        y_hist = [daily_revenue[d] for d in sorted_dates]

        df_daily = pd.DataFrame({"revenue": y_hist}, index=pd.to_datetime(sorted_dates))
        df_daily = df_daily.asfreq("D", fill_value=0.0)
        daily_ts = df_daily["revenue"]

        # Resample to weekly starting on Monday (W-MON)
        weekly_ts = daily_ts.resample("W-MON", closed="left", label="left").sum()

        # Remove current week if it is incomplete
        today = pd.Timestamp.now(tz=None).normalize()
        current_week_start = today - pd.Timedelta(days=today.weekday())
        if len(weekly_ts) > 0 and weekly_ts.index[-1] >= current_week_start:
            weekly_ts = weekly_ts.iloc[:-1]

        num_weeks = len(weekly_ts)
        print(f"[DEBUG] Weekly series: {num_weeks} complete weeks available.")

        # --- Step 3: Get last 12 weeks of history as forecast points ---
        forecast_points = []
        for idx in range(max(0, num_weeks - 12), num_weeks):
            forecast_points.append(
                ForecastPoint(
                    date=weekly_ts.index[idx].strftime("%Y-%m-%d"),
                    historical_revenue=float(weekly_ts.values[idx]),
                    predicted_revenue=float(weekly_ts.values[idx])
                )
            )

        # --- Step 4: Forecast sales for the next 4 weeks ---
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
            f"Here is the weekly sales forecast summary for our organization:\n"
            f"- Historical period: Last {num_weeks} weeks (~180 days). "
            f"Total revenue: {hist_total:,.2f} USD (Weekly Average: {hist_weekly_avg:,.2f} USD/week)\n"
            f"- Forecast period: Next 4 weeks. "
            f"Forecasted total revenue: {forecast_revenue_total:,.2f} USD (Weekly Average: {forecast_weekly_avg:,.2f} USD/week)\n"
            f"Note: Compare the WEEKLY AVERAGES ({hist_weekly_avg:,.2f} vs {forecast_weekly_avg:,.2f} USD/week) "
            f"to determine if the sales trend is growing or declining.\n"
            f"Please write a brief business report (3-4 sentences) identifying the sales trend and "
            f"suggesting 3 concrete recommendations to optimize sales strategy. "
            f"Use plain English for employees. Do NOT mention model names, equations, or technical jargon. "
            f"Do NOT output any raw organization ID or UUID in your response. Refer to the organization simply as 'the organization' or 'the company'."
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

        # --- Bước 6: Đánh giá độ chính xác mô hình và đưa vào insights ---
        val_metrics = self._validate_model_accuracy(weekly_ts)
        insights_list = list(llm_resp.insights or [])
        insights_list.append(f"📊 [Accuracy] Algorithm: {val_metrics['model_name']}")
        insights_list.append(f"📊 [Accuracy] MAPE: {val_metrics['mape']:.2f}%")
        insights_list.append(f"📊 [Accuracy] R2: {val_metrics['r2']:.2f}")

        parsed_resp = SalesForecastResponse(
            summary=llm_resp.summary,
            forecast_30d_total_revenue=round(forecast_revenue_total, 2),
            forecast_points=forecast_points,
            insights=insights_list
        )

        return parsed_resp

    async def analyze_inventory_abc_xyz(
        self, organization_id: str, warehouses: list, balances: list, sales: list, force_refresh: bool = False
    ) -> InventoryAnalysisResponse:
        """
        Perform ABC-XYZ analysis, and calculate dynamic ROP and EOQ based on actual inventory balances and transaction history.
        """

        if not warehouses:
            return InventoryAnalysisResponse(
                summary="No warehouses found in the organization.",
                abc_xyz_matrix=[],
                critical_stock_count=0,
                recommendations=["Please create a warehouse and import products to start the analysis."]
            )

        # Map to store current product quantities and prices
        product_stock = {}
        product_names = {}
        product_purchase_prices = {}
        product_sales_prices = {}
        
        for bal in balances:
            prod_id = bal.get("productId")
            prod_name = bal.get("productName")
            qty = float(bal.get("quantity") or 0.0)

            if prod_id:
                product_stock[prod_id] = product_stock.get(prod_id, 0.0) + qty
                if prod_name:
                    product_names[prod_id] = prod_name
                # Retrieve purchase and sales prices from backend payload
                product_purchase_prices[prod_id] = float(bal.get("purchasePrice") or 0.0) if bal.get("purchasePrice") is not None else 10.0
                product_sales_prices[prod_id] = float(bal.get("salesPrice") or 0.0) if bal.get("salesPrice") is not None else 15.0

        # Analyze quantity sold and transaction intervals for each product
        product_daily_sales = {}
        product_revenues = {}
        product_sales_dates = {}

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

                if qty_sold > 0:
                    if prod_id not in product_sales_dates:
                        product_sales_dates[prod_id] = set()
                    product_sales_dates[prod_id].add(date_str)

        # Compute ABC-XYZ classes for each product
        abc_xyz_matrix = []
        critical_count = 0

        # Classify ABC based on revenue share
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

            # Analyze daily sales demand
            daily_sales_dict = product_daily_sales.get(pid, {})
            sales_values = [daily_sales_dict.get((end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d"), 0.0) for i in range(90)]
            
            mu = sum(sales_values) / 90.0
            variance = sum((x - mu) ** 2 for x in sales_values) / 90.0
            sigma = math.sqrt(variance)

            # --- Connect AI Sales Forecast with Demand-Driven Inventory Optimization ---
            # Get 180 days of product sales history resampled to weekly for ACCA
            history_days = 180
            product_daily_series = [daily_sales_dict.get((end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d"), 0.0) for i in range(history_days)]
            product_daily_series.reverse()  # Sắp xếp từ cũ nhất đến mới nhất
            
            dates_series = [(end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d") for i in range(history_days)]
            dates_series.reverse()
            
            df_prod = pd.DataFrame({"qty": product_daily_series}, index=pd.to_datetime(dates_series))
            weekly_prod_ts = df_prod["qty"].resample("W-MON", closed="left", label="left").sum()
            
            mu_adjusted = mu
            demand_ratio = 1.0
            # Perform forecast adjustment only if sufficient history exists (min 12 weeks)
            if len(weekly_prod_ts) >= 12 and weekly_prod_ts.sum() > 0:
                try:
                    forecast_vals = self._forecast_classical_decomposition(weekly_prod_ts, periods=4)
                    historical_weekly_avg = weekly_prod_ts.iloc[-8:].mean()  # average of last 8 weeks
                    forecasted_weekly_avg = np.mean(forecast_vals)
                    
                    if historical_weekly_avg > 0:
                        demand_ratio = forecasted_weekly_avg / historical_weekly_avg
                        # Clamp ratio between 0.5 and 2.0 to prevent extreme volatility
                        demand_ratio = max(0.5, min(2.0, demand_ratio))
                        mu_adjusted = mu * demand_ratio
                        print(f"[DEMAND ADJUSTMENT] Product: {name}, Hist Weekly Avg: {historical_weekly_avg:.2f}, Forecast Weekly Avg: {forecasted_weekly_avg:.2f}, Ratio: {demand_ratio:.2f}, Adjusted mu: {mu_adjusted:.4f}")
                except Exception as e:
                    print(f"[WARNING] Failed to calculate demand adjustment for product {name}: {e}")

            # Classify XYZ based on coefficient of variation
            cv = sigma / mu if mu > 0 else 9.9
            if cv < 0.3:
                xyz = "X"
            elif cv < 0.7:
                xyz = "Y"
            else:
                xyz = "Z"

            abc = abc_class.get(pid, "C")

            # 1. Compute dynamic Lead Time (clamped between 3 and 30 days, default 7 days)
            dates_list = sorted(list(product_sales_dates.get(pid, [])))
            if len(dates_list) >= 2:
                try:
                    date_objs = [datetime.datetime.strptime(d, "%Y-%m-%d") for d in dates_list]
                    gaps = [(date_objs[i] - date_objs[i-1]).days for i in range(1, len(date_objs))]
                    avg_gap = sum(gaps) / len(gaps)
                    lead_time = max(3.0, min(30.0, avg_gap * 0.5))
                except Exception:
                    lead_time = 7.0
            else:
                lead_time = 7.0

            # 2. Dynamic Service Z-score based on ABC classification (A: 98% -> Z=2.05, B: 95% -> Z=1.65, C: 90% -> Z=1.28)
            if abc == "A":
                z_score = 2.05
            elif abc == "B":
                z_score = 1.65
            else:
                z_score = 1.28

            # 3. Calculate Safety Stock (no hardcoded floor)
            safety_stock = z_score * sigma * math.sqrt(lead_time)
            if safety_stock < 0.0:
                safety_stock = 0.0

            # 4. Calculate ROP (uses mu_adjusted for forward-looking demand)
            rop = (mu_adjusted * lead_time) + safety_stock
            if mu_adjusted > 0 and rop < 1.0:
                rop = 1.0
            elif mu_adjusted == 0:
                rop = 0.0

            # 5. Compute dynamic EOQ based on actual purchasePrice and dynamic ordering cost S
            annual_demand = mu_adjusted * 365.0
            
            # S: Dynamic ordering cost proxy (1.5% of average monthly revenue, floor of 10.0)
            avg_monthly_rev = product_revenues.get(pid, 0.0) / 3.0  # 90 days = 3 months
            ordering_cost_S = max(10.0, avg_monthly_rev * 0.015)
            
            # H: Dynamic holding cost = unit purchase price * 25% holding rate
            unit_cost = product_purchase_prices.get(pid, 10.0)
            holding_cost_H = unit_cost * 0.25
            
            if annual_demand > 0 and holding_cost_H > 0:
                eoq = math.sqrt((2 * annual_demand * ordering_cost_S) / holding_cost_H)
            else:
                eoq = 0.0

            # Set minimum EOQ to 1.0 if sales exist
            if annual_demand > 0 and eoq < 1.0:
                eoq = 1.0

            # Determine replenishment warning status
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

        # LLM summary
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
        """
        Get list of products running low on stock (below ROP).
        """
        analysis = await self.analyze_inventory_abc_xyz(organization_id, warehouses, balances, sales)
        return [item for item in analysis.abc_xyz_matrix if item.status in ["CRITICAL", "WARNING"]]

    async def get_reorder_recommendations(self, organization_id: str, warehouses: list, balances: list, sales: list) -> ReorderRecommendationResponse:
        """
        Recommend optimal reorder quantities for products below ROP.
        """
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
        """
        Generate a concise Daily Brief summarizing sales forecast and inventory alerts for the CEO.
        """
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
            f"- Forecasted sales for the next 30 days: {predicted_sales:,.2f} USD\n"
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
