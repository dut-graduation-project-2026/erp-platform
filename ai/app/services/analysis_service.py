import json
import datetime
import asyncio
import math
from ..integrations.erp_clients import erp_client, ERPClient
from ..integrations.openai_clients import openai_client
from ..schemas.sample_response import (
    UserAnalysisResponse,
    SalesAnalysisResponse,
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
from ..tools import order_tools, user_tools, inventory_tools
from ..tools.tools import AVAILABLE_TOOLS

# In-memory cache to optimize performance and prevent repeated DB queries
_analysis_cache = {}

class AnalysisService:
    def __init__(self, erp_client: ERPClient):
        self.erp_client = erp_client
        self.openai_client = openai_client

    async def _run_tool_calling_loop(
        self, prompt: str, tools: list, final_response_format: type
    ):
        messages = [{"role": "user", "content": prompt}]

        while True:
            response = await self.openai_client.chat(
                messages=messages, tools=tools if tools else None
            )

            message = response.choices[0].message
            messages.append(message)

            if not message.tool_calls:
                break

            async def execute_tool(tool_call):
                tool_name = tool_call.function.name
                tool_args = json.loads(tool_call.function.arguments)

                if tool_name in AVAILABLE_TOOLS:
                    result = await AVAILABLE_TOOLS[tool_name](**tool_args)
                else:
                    result = f"Error: Unknown tool {tool_name}"

                return {
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "content": str(result)
                }

            # Run all tools in parallel for speed
            tool_results = await asyncio.gather(*(execute_tool(tc) for tc in message.tool_calls))
            messages.extend(tool_results)

        messages.append(
            {
                "role": "user",
                "content": "Dựa trên các dữ liệu đã thu thập được ở trên, hãy tổng hợp và trả về kết quả phân tích theo đúng định dạng được yêu cầu.",
            }
        )

        final_response = await self.openai_client.chat(
            messages=messages, response_format=final_response_format
        )

        return final_response.choices[0].message.parsed

    async def analyze_users_of_organization(self, organization_id: str):
        prompt = (
            f"Hãy phân tích cơ cấu nhân sự của tổ chức có ID: {organization_id}. "
            f"Bạn có thể sử dụng công cụ get_organization_users để lấy danh sách người dùng."
        )
        return await self._run_tool_calling_loop(
            prompt=prompt,
            tools=[user_tools.GET_ORGANIZATION_USERS_TOOL],
            final_response_format=UserAnalysisResponse,
        )

    async def analyze_sales_of_last_month(self, organization_id: str):
        end_date = datetime.datetime.now(datetime.timezone.utc)
        start_date = end_date - datetime.timedelta(days=30)

        start_date_str = start_date.isoformat().replace("+00:00", "Z")
        end_date_str = end_date.isoformat().replace("+00:00", "Z")

        prompt = (
            f"Hãy phân tích tình hình và lượng hàng bán ra trong 30 ngày qua của tổ chức có ID: {organization_id}.\n"
            f"Thời gian: từ {start_date_str} đến {end_date_str}.\n"
            f"Bạn có thể sử dụng công cụ get_organization_orders để lấy danh sách đơn hàng trong khoảng thời gian này, "
            f"và sử dụng get_order_details để lấy thông tin chi tiết của từng đơn hàng (bao gồm sản phẩm và số lượng) nhằm tính toán. "
            f"Lưu ý: Chỉ tính lượng hàng bán ra từ các đơn hàng có trạng thái xác nhận, giao hàng hoặc hoàn thành (bỏ qua các đơn hàng DRAFT hoặc CANCELLED)."
        )

        return await self._run_tool_calling_loop(
            prompt=prompt,
            tools=[
                order_tools.GET_ORGANIZATION_ORDERS_TOOL,
                order_tools.GET_ORDER_DETAILS_TOOL,
            ],
            final_response_format=SalesAnalysisResponse,
        )

    async def analyze_sales_forecast(self, organization_id: str, period: str = "30d") -> SalesForecastResponse:
        """Dự báo doanh số 30 ngày tiếp theo bằng thuật toán tuyến tính kết hợp Moving Average + Nhận xét từ LLM."""
        cache_key = f"{organization_id}_sales_forecast"
        # Trả về từ cache nếu có
        if cache_key in _analysis_cache:
            return _analysis_cache[cache_key]

        # Lấy đơn hàng trong 90 ngày để có đủ dữ liệu dự báo
        end_date = datetime.datetime.now(datetime.timezone.utc)
        start_date = end_date - datetime.timedelta(days=90)
        start_date_str = start_date.isoformat().replace("+00:00", "Z")
        end_date_str = end_date.isoformat().replace("+00:00", "Z")

        orders_text = await order_tools.get_organization_orders(organization_id, start_date_str, end_date_str)
        try:
            orders_data = json.loads(orders_text)
            orders_list = orders_data.get("data", [])
        except Exception:
            orders_list = []

        # Chỉ lấy đơn hàng hợp lệ
        valid_orders = [o for o in orders_list if o.get("status") not in ["DRAFT", "CANCELLED"]]

        # Nhóm doanh thu theo ngày
        daily_revenue = {}
        for day_idx in range(90):
            d = (end_date - datetime.timedelta(days=day_idx)).strftime("%Y-%m-%d")
            daily_revenue[d] = 0.0

        for o in valid_orders:
            # Lấy ngày tạo
            created_at_str = o.get("createdAt") or o.get("deliveryDate")
            if created_at_str:
                d = created_at_str.split("T")[0]
                if d in daily_revenue:
                    daily_revenue[d] += float(o.get("totalAmount") or 0.0)

        # Chuyển thành danh sách sắp xếp theo thời gian
        sorted_dates = sorted(daily_revenue.keys())
        y_hist = [daily_revenue[d] for d in sorted_dates]
        x_hist = list(range(len(y_hist)))

        # Tính toán hồi quy tuyến tính: y = m * x + c
        n = len(x_hist)
        if n >= 2:
            sum_x = sum(x_hist)
            sum_y = sum(y_hist)
            sum_xx = sum(x*x for x in x_hist)
            sum_xy = sum(x*y for x, y in zip(x_hist, y_hist))
            
            denom = (n * sum_xx - sum_x**2)
            slope = (n * sum_xy - sum_x * sum_y) / denom if denom != 0 else 0.0
            intercept = (sum_y - slope * sum_x) / n
        else:
            slope = 0.0
            intercept = 0.0

        # Dự báo 30 ngày tiếp theo
        forecast_points = []
        forecast_revenue_total = 0.0

        # Thêm 30 điểm lịch sử gần nhất vào kết quả
        for idx in range(max(0, n - 30), n):
            d = sorted_dates[idx]
            forecast_points.append(
                ForecastPoint(
                    date=d,
                    historical_revenue=y_hist[idx],
                    predicted_revenue=y_hist[idx]
                )
            )

        # Sinh 30 điểm dự báo tiếp theo
        last_date = datetime.datetime.strptime(sorted_dates[-1], "%Y-%m-%d") if sorted_dates else datetime.datetime.now()
        for i in range(1, 31):
            next_day = last_date + datetime.timedelta(days=i)
            next_day_str = next_day.strftime("%Y-%m-%d")
            # Dự báo tuyến tính
            pred_y = max(0.0, slope * (n + i) + intercept)
            # Thêm yếu tố nhiễu nhẹ / sóng tuần
            weekday = next_day.weekday()
            seasonality = 1.0 + (0.1 if weekday < 5 else -0.2)  # Cuối tuần ít bán hơn
            pred_y *= seasonality

            forecast_revenue_total += pred_y
            forecast_points.append(
                ForecastPoint(
                    date=next_day_str,
                    historical_revenue=None,
                    predicted_revenue=round(pred_y, 2)
                )
            )

        # Gọi LLM viết nhận xét dựa trên số liệu tóm tắt
        prompt = (
            f"Dưới đây là tóm tắt kết quả dự báo doanh thu 30 ngày tới của tổ chức {organization_id}:\n"
            f"- Tổng doanh thu thực tế 90 ngày qua: {sum(y_hist):,.2f} USD\n"
            f"- Dự báo tổng doanh thu 30 ngày tới: {forecast_revenue_total:,.2f} USD\n"
            f"- Hệ số xu hướng ngày (Linear Slope): {slope:,.2f} (nếu dương là tăng trưởng, âm là sụt giảm)\n"
            f"Hãy viết một báo cáo nhận xét tiếng Việt ngắn gọn (khoảng 3-4 câu) chỉ ra xu hướng doanh số và đưa ra 3 khuyến nghị tối ưu hóa chiến lược bán hàng."
        )

        messages = [
            {"role": "system", "content": "Bạn là chuyên gia phân tích tài chính doanh nghiệp của ERP. Hãy viết nhận xét ngắn gọn, thực tế, chuyên nghiệp bằng tiếng Việt."},
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

        # Lưu cache
        _analysis_cache[cache_key] = parsed_resp
        return parsed_resp

    async def analyze_inventory_abc_xyz(self, organization_id: str, force_refresh: bool = False) -> InventoryAnalysisResponse:
        """Phân tích ma trận ABC-XYZ, tính toán ROP, EOQ động dựa trên lịch sử mua bán và lượng tồn kho thực tế."""
        cache_key = f"{organization_id}_inventory_analysis"
        if not force_refresh and cache_key in _analysis_cache:
            return _analysis_cache[cache_key]

        # 1. Lấy danh sách kho
        warehouses_text = await inventory_tools.get_warehouses(organization_id)
        try:
            warehouses_data = json.loads(warehouses_text)
            warehouses = warehouses_data.get("data", [])
        except Exception:
            warehouses = []

        if not warehouses:
            # Trả về kết quả rỗng nếu không có kho hàng
            return InventoryAnalysisResponse(
                summary="Không tìm thấy kho hàng nào trong tổ chức.",
                abc_xyz_matrix=[],
                critical_stock_count=0,
                recommendations=["Hãy tạo kho hàng và nhập sản phẩm để bắt đầu phân tích."]
            )

        # 2. Lấy số dư tồn kho từ tất cả các kho
        balances_tasks = [inventory_tools.get_warehouse_balances(organization_id, wh["id"]) for wh in warehouses]
        balances_responses = await asyncio.gather(*balances_tasks)

        # Map lưu trữ tồn kho hiện tại theo productId (cộng dồn từ các kho) và thông tin kho cụ thể
        product_stock = {}
        product_names = {}
        product_warehouse_map = {} # productId -> (warehouseId, warehouseName)
        
        for wh, resp_text in zip(warehouses, balances_responses):
            try:
                resp_data = json.loads(resp_text)
                balances_list = resp_data.get("data", [])
            except Exception:
                balances_list = []

            for bal in balances_list:
                prod = bal.get("product")
                if not prod:
                    continue
                prod_id = prod["id"]
                prod_name = prod["name"]
                qty = float(bal.get("quantity") or 0.0)

                product_stock[prod_id] = product_stock.get(prod_id, 0.0) + qty
                product_names[prod_id] = prod_name
                product_warehouse_map[prod_id] = (wh["id"], wh["name"])

        # Chuẩn hóa định dạng product_stock
        norm_product_stock = product_stock


        # 3. Lấy dữ liệu bán hàng 90 ngày qua để tính toán nhu cầu động
        end_date = datetime.datetime.now(datetime.timezone.utc)
        start_date = end_date - datetime.timedelta(days=90)
        start_date_str = start_date.isoformat().replace("+00:00", "Z")
        end_date_str = end_date.isoformat().replace("+00:00", "Z")

        orders_text = await order_tools.get_organization_orders(organization_id, start_date_str, end_date_str)
        try:
            orders_data = json.loads(orders_text)
            orders_list = orders_data.get("data", [])
        except Exception:
            orders_list = []

        valid_orders = [o for o in orders_list if o.get("status") not in ["DRAFT", "CANCELLED"]]

        # Lấy chi tiết từng đơn hàng song song để tính doanh số theo sản phẩm
        tasks = [order_tools.get_order_details(organization_id, o["id"]) for o in valid_orders]
        details_responses = await asyncio.gather(*tasks)

        # Phân tích lượng bán của từng sản phẩm theo từng ngày
        product_daily_sales = {} # productId -> dict(date_str -> qty)
        product_revenues = {}    # productId -> total revenue

        for order_detail_text in details_responses:
            try:
                detail = json.loads(order_detail_text)
                created_at_str = detail.get("createdAt") or datetime.datetime.now().isoformat()
                date_str = created_at_str.split("T")[0]
                
                for item in detail.get("items", []):
                    prod = item.get("product")
                    if not prod:
                        continue
                    prod_id = prod["id"]
                    qty_sold = float(item.get("quantity") or 0.0)
                    price = float(item.get("unitPrice") or prod.get("price") or 0.0)

                    product_names[prod_id] = prod["name"]
                    product_revenues[prod_id] = product_revenues.get(prod_id, 0.0) + (qty_sold * price)

                    if prod_id not in product_daily_sales:
                        product_daily_sales[prod_id] = {}
                    product_daily_sales[prod_id][date_str] = product_daily_sales[prod_id].get(date_str, 0.0) + qty_sold
            except Exception:
                continue

        # 4. Tính toán thống kê động (ROP, EOQ, ABC, XYZ) cho từng sản phẩm
        abc_xyz_matrix = []
        critical_count = 0

        # Phân loại ABC dựa trên Doanh thu
        sorted_prods_by_rev = sorted(product_revenues.items(), key=lambda x: x[1], reverse=True)
        total_rev_all = sum(product_revenues.values())

        abc_class = {}
        cum_rev = 0.0
        for pid, rev in sorted_prods_by_rev:
            cum_rev += rev
            share = cum_rev / total_rev_all if total_rev_all > 0 else 1.0
            if share <= 0.70:
                abc_class[pid] = "A"
            elif share <= 0.90:
                abc_class[pid] = "B"
            else:
                abc_class[pid] = "C"

        # Duyệt qua toàn bộ sản phẩm đang có tồn kho hoặc có doanh số để lập bảng
        all_product_ids = set(norm_product_stock.keys()).union(product_names.keys())

        for pid in all_product_ids:
            if not pid:
                continue
            name = product_names.get(pid, f"Sản phẩm {pid[:8]}")
            curr_stock = norm_product_stock.get(pid, 0.0)

            # Phân tích nhu cầu hàng ngày
            daily_sales_dict = product_daily_sales.get(pid, {})
            # Điền các ngày không bán được là 0
            sales_values = [daily_sales_dict.get((end_date - datetime.timedelta(days=i)).strftime("%Y-%m-%d"), 0.0) for i in range(90)]
            
            # Tính trung bình ngày (mu) và độ lệch chuẩn (sigma)
            mu = sum(sales_values) / 90.0
            variance = sum((x - mu) ** 2 for x in sales_values) / 90.0
            sigma = math.sqrt(variance)

            # Tính toán phân loại XYZ dựa trên hệ số biến thiên CV = sigma / mu
            cv = sigma / mu if mu > 0 else 9.9
            if cv < 0.3:
                xyz = "X"
            elif cv < 0.7:
                xyz = "Y"
            else:
                xyz = "Z"

            abc = abc_class.get(pid, "C")

            # Công thức chuỗi cung ứng chuẩn:
            # Lead Time (L) = 5 ngày
            # Safety Stock (SS) = 1.65 * sigma * sqrt(L)
            # ROP = (mu * L) + SS
            lead_time = 5.0
            safety_stock = 1.65 * sigma * math.sqrt(lead_time)
            
            # Thiết lập biên an toàn tối thiểu
            if safety_stock < 2.0:
                safety_stock = 5.0
            
            rop = (mu * lead_time) + safety_stock
            if rop < 5.0:
                rop = 10.0 # Ngưỡng đặt hàng tối thiểu mặc định

            # Lượng đặt tối ưu EOQ: sqrt(2 * D * S / H)
            # Giả định Chi phí đặt S = 50, Chi phí giữ kho H = 2.0 hàng năm
            annual_demand = mu * 365.0
            eoq = math.sqrt((2 * annual_demand * 50.0) / 2.0) if annual_demand > 0 else 30.0
            if eoq < 10.0:
                eoq = 30.0

            # Xác định trạng thái cảnh báo
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

        # 5. Gọi LLM sinh nhận xét tổng quan
        critical_items = [item for item in abc_xyz_matrix if item.status in ["CRITICAL", "WARNING"]]
        summary_prompt = (
            f"Dưới đây là tóm tắt số liệu tồn kho thực tế của tổ chức:\n"
            f"- Tổng số mặt hàng phân tích: {len(abc_xyz_matrix)}\n"
            f"- Số sản phẩm dưới điểm đặt hàng lại ROP (Cần nhập): {len(critical_items)} (trong đó {critical_count} ở mức CRITICAL nguy cấp)\n"
            f"- Danh sách sản phẩm thiếu hụt tiêu biểu: {', '.join([f'{x.productName} (Tồn: {x.currentStock}/{x.rop} ROP)' for x in critical_items[:5]])}\n"
            f"Hãy viết một báo cáo phân tích kho tiếng Việt ngắn gọn (3-4 câu) chỉ ra mức độ rủi ro đứt gãy chuỗi cung ứng và đề xuất 3 giải pháp cải thiện tồn kho."
        )

        messages = [
            {"role": "system", "content": "Bạn là giám đốc logistics thông minh của ERP. Hãy viết nhận xét chuyên nghiệp bằng tiếng Việt."},
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

        # Cập nhật cache
        _analysis_cache[cache_key] = parsed_resp
        return parsed_resp

    async def get_inventory_alerts(self, organization_id: str) -> list:
        """Lấy nhanh các mặt hàng cảnh báo hết hàng."""
        analysis = await self.analyze_inventory_abc_xyz(organization_id)
        return [item for item in analysis.abc_xyz_matrix if item.status in ["CRITICAL", "WARNING"]]

    async def get_reorder_recommendations(self, organization_id: str) -> ReorderRecommendationResponse:
        """Đề xuất lượng nhập tối ưu (Reorder Recommendations) cho các sản phẩm dưới ROP."""
        # Chạy phân tích để có số liệu ROP, EOQ và tồn thực tế
        analysis = await self.analyze_inventory_abc_xyz(organization_id)
        
        # 1. Lấy danh sách kho để biết tên kho gán cho đề xuất
        warehouses_text = await inventory_tools.get_warehouses(organization_id)
        try:
            warehouses_data = json.loads(warehouses_text)
            warehouses = warehouses_data.get("data", [])
        except Exception:
            warehouses = []
        
        default_wh_id = warehouses[0]["id"] if warehouses else ""
        default_wh_name = warehouses[0]["name"] if warehouses else "Kho chính"

        # Lấy map kho của từng sản phẩm từ balances (để đề xuất đúng kho)
        # Để đơn giản, ta sẽ lấy warehouseId đầu tiên tìm thấy của sản phẩm đó
        product_wh_map = {}
        for wh in warehouses:
            try:
                bal_text = await inventory_tools.get_warehouse_balances(organization_id, wh["id"])
                bal_data = json.loads(bal_text).get("data", [])
                for b in bal_data:
                    prod = b.get("product")
                    if prod:
                        product_wh_map[prod["id"]] = (wh["id"], wh["name"])
            except Exception:
                continue

        reorder_items = []
        for prod in analysis.abc_xyz_matrix:
            if prod.status in ["CRITICAL", "WARNING"]:
                wh_id, wh_name = product_wh_map.get(prod.productId, (default_wh_id, default_wh_name))
                
                # Số lượng khuyên dùng nhập = lượng thiếu hụt đưa về ROP + lượng đặt tối ưu EOQ
                recommended_qty = max(prod.eoq, (prod.rop - prod.currentStock) + prod.eoq)
                
                # Mức độ ưu tiên
                urgency = "HIGH" if prod.status == "CRITICAL" or prod.abcClass == "A" else "MEDIUM"
                
                note = f"Tồn kho thực tế ({prod.currentStock}) thấp hơn điểm ROP ({prod.rop}). Khuyên dùng nhập {recommended_qty} sản phẩm nhóm {prod.abcClass}-{prod.xyzClass}."

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

        # Sử dụng LLM viết lại lý do (notes) sinh động và khoa học cho 5 mặt hàng khẩn cấp nhất
        if reorder_items:
            prompt = (
                f"Hãy viết lại trường lý do nhập hàng (notes) bằng tiếng Việt thật chuyên nghiệp cho danh sách đề xuất nhập kho sau đây. "
                f"Chỉ ra tầm quan trọng của sản phẩm dựa trên phân loại nhóm ABC-XYZ của nó:\n"
                + "\n".join([f"- ID: {x.productId}, Tên: {x.productName} (Nhóm: {analysis.abc_xyz_matrix[0].abcClass}{analysis.abc_xyz_matrix[0].xyzClass}, Tồn: {x.currentStock}/{x.rop} ROP, Lượng khuyên dùng: {x.recommendedQuantity})" for x in reorder_items[:5]])
            )

            messages = [
                {"role": "system", "content": "Bạn là trợ lý chuỗi cung ứng AI. Hãy viết trường notes cực kỳ ngắn gọn (tối đa 15 từ) cho mỗi sản phẩm, nêu rõ lý do ROP/nhóm sản phẩm. Trả về đúng ID của sản phẩm."},
                {"role": "user", "content": prompt}
            ]

            try:
                # Tránh lỗi nếu LLM không phản hồi hoặc lỗi định dạng
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

    async def get_dashboard_summary(self, organization_id: str) -> DashboardSummaryResponse:
        """Tạo Daily Brief tóm tắt nhanh tình trạng bán hàng và tồn kho cho giám đốc."""
        # Lấy dữ liệu nhanh từ cache hoặc chạy tính toán nhẹ
        try:
            sales_task = self.analyze_sales_forecast(organization_id)
            inv_task = self.analyze_inventory_abc_xyz(organization_id)
            sales, inv = await asyncio.gather(sales_task, inv_task)
            
            critical_count = inv.critical_stock_count
            predicted_sales = sales.forecast_30d_total_revenue
        except Exception:
            critical_count = 0
            predicted_sales = 0.0

        prompt = (
            f"Hãy soạn một bản tin tóm tắt khởi đầu ngày mới (Daily Brief) bằng tiếng Việt cực kỳ ngắn gọn (khoảng 3 câu) cho CEO:\n"
            f"- Dự báo doanh thu 30 ngày tới: {predicted_sales:,.2f} USD\n"
            f"- Cảnh báo tồn kho: {critical_count} sản phẩm đang cạn kiệt dưới điểm ROP.\n"
            f"Văn phong truyền cảm hứng, ngắn gọn, chỉ ra hành động cần làm ngay trong ngày hôm nay."
        )

        messages = [
            {"role": "system", "content": "Bạn là trợ lý điều hành AI của CEO. Hãy viết bản tóm tắt ngắn gọn, lịch sự, tập trung vào hành động."},
            {"role": "user", "content": prompt}
        ]

        response = await self.openai_client.chat(
            messages=messages,
            response_format=DashboardSummaryResponse
        )
        return response.choices[0].message.parsed

analysis_service = AnalysisService(erp_client)
