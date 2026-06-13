import json
import datetime
import asyncio
from ..integrations.erp_clients import erp_client, ERPClient
from ..integrations.openai_clients import openai_client
from ..schemas.sample_response import (
    UserAnalysisResponse,
    SalesAnalysisResponse,
)
from ..tools import order_tools, user_tools
from ..tools.tools import AVAILABLE_TOOLS


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

            # Chạy tất cả các tool song song để tăng tốc
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
        # Lấy khoảng thời gian trong vòng 30 ngày gần nhất
        end_date = datetime.datetime.now(datetime.timezone.utc)
        start_date = end_date - datetime.timedelta(days=30)

        # Định dạng thời gian theo chuẩn ISO 8601 (Spring Boot Instant)
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


analysis_service = AnalysisService(erp_client)
