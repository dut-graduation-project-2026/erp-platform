from ..integrations.erp_clients import erp_client


async def get_organization_orders(
    organization_id: str,
    search: str = None,
    status: str = None,
    partnerId: str = None,
    salePersonId: str = None,
    saleTeamId: str = None,
    startDate: str = None,
    endDate: str = None,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách đơn hàng (orders) của một tổ chức với các bộ lọc (status, partner, sale person, sale team, date range) và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    if status:
        params["status"] = status
    if partnerId:
        params["partnerId"] = partnerId
    if salePersonId:
        params["salePersonId"] = salePersonId
    if saleTeamId:
        params["saleTeamId"] = saleTeamId
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


async def get_confirmed_orders(
    organization_id: str, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách các đơn hàng đã được xác nhận (status = CONFIRMED) của một tổ chức, hỗ trợ phân trang."""
    params = {"page": page, "limit": limit}
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/confirmed", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch confirmed orders: {response.text}"
    return response.text


async def get_order_details(organization_id: str, order_id: str) -> str:
    """Lấy thông tin chi tiết của một đơn hàng cụ thể theo ID, bao gồm cả danh sách các mặt hàng (items)."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/{order_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch order details: {response.text}"
    return response.text


async def get_order_items(organization_id: str, order_id: str) -> str:
    """Lấy danh sách tất cả các mặt hàng (items) thuộc về một đơn hàng cụ thể."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/{order_id}/items"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch order items: {response.text}"
    return response.text


async def get_order_item_by_id(
    organization_id: str, order_id: str, id: str
) -> str:
    """Lấy thông tin chi tiết của một mặt hàng cụ thể trong đơn hàng dựa trên ID mặt hàng."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/{order_id}/items/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch order item: {response.text}"
    return response.text


async def get_quotations(
    organization_id: str, search: str = None, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách các báo giá (quotations - đơn hàng ở trạng thái DRAFT) của một tổ chức, hỗ trợ tìm kiếm và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/quotations", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch quotations: {response.text}"
    return response.text


async def get_quotation_by_id(organization_id: str, id: str) -> str:
    """Lấy thông tin chi tiết của một báo giá cụ thể dựa trên ID báo giá."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/quotations/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch quotation details: {response.text}"
    return response.text


# Schemas khai báo cho OpenAI/Gemini

GET_ORGANIZATION_ORDERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_organization_orders",
        "description": "Lấy danh sách đơn hàng (orders) của một tổ chức với các bộ lọc (status, partner, sale person, sale team, date range) và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo mã đơn hàng hoặc tên đối tác (tùy chọn)",
                },
                "status": {
                    "type": "string",
                    "enum": ["DRAFT", "SENT", "CONFIRMED", "SHIPPED", "DONE", "CANCELLED"],
                    "description": "Trạng thái đơn hàng (tùy chọn)",
                },
                "partnerId": {
                    "type": "string",
                    "description": "ID của đối tác/khách hàng (UUID) (tùy chọn)",
                },
                "salePersonId": {
                    "type": "string",
                    "description": "ID của nhân viên kinh doanh phụ trách (UUID) (tùy chọn)",
                },
                "saleTeamId": {
                    "type": "string",
                    "description": "ID của đội ngũ bán hàng phụ trách (UUID) (tùy chọn)",
                },
                "startDate": {
                    "type": "string",
                    "description": "Thời điểm bắt đầu lọc đơn hàng (ISO 8601 string, ví dụ: 2026-05-14T08:00:00Z)",
                },
                "endDate": {
                    "type": "string",
                    "description": "Thời điểm kết thúc lọc đơn hàng (ISO 8601 string, ví dụ: 2026-06-13T08:00:00Z)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng đơn hàng tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_CONFIRMED_ORDERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_confirmed_orders",
        "description": "Lấy danh sách các đơn hàng đã được xác nhận (status = CONFIRMED) của một tổ chức, hỗ trợ phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng đơn hàng tối đa trên mỗi trang (mặc định: 100)",
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
        "description": "Lấy thông tin chi tiết của một đơn hàng cụ thể theo ID, bao gồm cả danh sách các mặt hàng (items).",
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

GET_ORDER_ITEMS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_items",
        "description": "Lấy danh sách tất cả các mặt hàng (items) thuộc về một đơn hàng cụ thể.",
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

GET_ORDER_ITEM_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_item_by_id",
        "description": "Lấy thông tin chi tiết của một mặt hàng cụ thể trong đơn hàng dựa trên ID mặt hàng.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "order_id": {"type": "string", "description": "ID của đơn hàng (UUID)"},
                "id": {
                    "type": "string",
                    "description": "ID của mặt hàng cần lấy (UUID)",
                },
            },
            "required": ["organization_id", "order_id", "id"],
        },
    },
}

GET_QUOTATIONS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_quotations",
        "description": "Lấy danh sách các báo giá (quotations - đơn hàng ở trạng thái DRAFT) của một tổ chức, hỗ trợ tìm kiếm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên hoặc thông tin báo giá (tùy chọn)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng báo giá tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_QUOTATION_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_quotation_by_id",
        "description": "Lấy thông tin chi tiết của một báo giá cụ thể dựa trên ID báo giá.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của báo giá cần lấy (UUID)",
                },
            },
            "required": ["organization_id", "id"],
        },
    },
}
