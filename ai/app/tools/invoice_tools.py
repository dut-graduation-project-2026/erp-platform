from ..integrations.erp_clients import erp_client


async def get_invoices(
    organization_id: str, search: str = None, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách hóa đơn (invoices) của tổ chức, hỗ trợ tìm kiếm và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/invoices", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch invoices: {response.text}"
    return response.text


async def get_invoice_by_id(organization_id: str, id: str) -> str:
    """Lấy thông tin chi tiết của một hóa đơn cụ thể dựa trên ID hóa đơn."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/invoices/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch invoice details: {response.text}"
    return response.text


async def get_invoice_by_order_id(organization_id: str, order_id: str) -> str:
    """Lấy thông tin hóa đơn được tạo cho một đơn hàng cụ thể."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/invoices/order/{order_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch invoice by order: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_INVOICES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_invoices",
        "description": "Lấy danh sách hóa đơn (invoices) của tổ chức, hỗ trợ tìm kiếm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm hóa đơn (tùy chọn)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng hóa đơn tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_INVOICE_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_invoice_by_id",
        "description": "Lấy thông tin chi tiết của một hóa đơn cụ thể dựa trên ID hóa đơn.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của hóa đơn (UUID)",
                },
            },
            "required": ["organization_id", "id"],
        },
    },
}

GET_INVOICE_BY_ORDER_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_invoice_by_order_id",
        "description": "Lấy thông tin hóa đơn được tạo cho một đơn hàng cụ thể.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "order_id": {
                    "type": "string",
                    "description": "ID của đơn hàng (UUID)",
                },
            },
            "required": ["organization_id", "order_id"],
        },
    },
}
