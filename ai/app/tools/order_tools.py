from ..integrations.erp_clients import erp_client


async def get_organization_orders(
    organization_id: str, startDate: str = None, endDate: str = None
) -> str:
    """Lấy danh sách đơn hàng (orders) của một tổ chức trong một khoảng thời gian nhất định."""
    params = {"limit": 100}
    if startDate:
        params["startDate"] = startDate
    if endDate:
        params["endDate"] = endDate
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch orders: {response.text}"
    return response.text


async def get_order_details(organization_id: str, order_id: str) -> str:
    """Lấy thông tin chi tiết của một đơn hàng cụ thể, bao gồm danh sách các mặt hàng (items), số lượng và đơn giá."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/{order_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch order details: {response.text}"
    return response.text


# Schemas để khai báo với OpenAI
GET_ORGANIZATION_ORDERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_organization_orders",
        "description": "Lấy danh sách đơn hàng (orders) của một tổ chức trong một khoảng thời gian nhất định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "startDate": {
                    "type": "string",
                    "description": "Thời điểm bắt đầu lọc đơn hàng (ISO 8601 string, ví dụ: 2026-05-14T08:00:00Z)",
                },
                "endDate": {
                    "type": "string",
                    "description": "Thời điểm kết thúc lọc đơn hàng (ISO 8601 string, ví dụ: 2026-06-13T08:00:00Z)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_ORDER_DETAILS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_details",
        "description": "Lấy thông tin chi tiết của một đơn hàng cụ thể, bao gồm danh sách các mặt hàng (items), số lượng và đơn giá.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "order_id": {"type": "string", "description": "ID của đơn hàng (UUID)"},
            },
            "required": ["organization_id", "order_id"],
        },
    },
}
