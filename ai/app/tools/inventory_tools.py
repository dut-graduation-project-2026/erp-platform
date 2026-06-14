from ..integrations.erp_clients import erp_client


async def get_warehouses(
    organization_id: str, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách các kho hàng (warehouses) của một tổ chức, hỗ trợ phân trang."""
    params = {"page": page, "limit": limit}
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch warehouses: {response.text}"
    return response.text


async def get_warehouse_by_id(organization_id: str, warehouse_id: str) -> str:
    """Lấy thông tin chi tiết của một kho hàng cụ thể trong tổ chức dựa trên ID kho."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch warehouse details: {response.text}"
    return response.text


async def get_warehouse_balances(
    organization_id: str,
    warehouse_id: str,
    search: str = None,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách số dư tồn kho (inventory balances) của một kho hàng cụ thể, hỗ trợ tìm kiếm sản phẩm và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/balances",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch warehouse balances: {response.text}"
    return response.text


async def get_warehouse_balance_by_id(
    organization_id: str, warehouse_id: str, id: str
) -> str:
    """Lấy chi tiết của một bản ghi số dư tồn kho cụ thể trong kho hàng theo ID."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/balances/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch inventory balance details: {response.text}"
    return response.text


async def get_inventory_documents(
    organization_id: str,
    warehouse_id: str,
    search: str = None,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách tài liệu kho (nhập/xuất/điều chuyển kho - inventory documents) của một kho hàng cụ thể."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/documents",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch inventory documents: {response.text}"
    return response.text


async def get_inventory_document_by_id(
    organization_id: str, warehouse_id: str, document_id: str
) -> str:
    """Lấy chi tiết một tài liệu kho cụ thể dựa trên ID tài liệu kho."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/documents/{document_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch inventory document details: {response.text}"
    return response.text


async def get_replenishment_requests(
    organization_id: str, warehouse_id: str, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách các yêu cầu bổ sung hàng tồn kho (replenishment requests) của một kho hàng cụ thể."""
    params = {"page": page, "limit": limit}
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/replenishment-requests",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch replenishment requests: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_WAREHOUSES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouses",
        "description": "Lấy danh sách các kho hàng (warehouses) của một tổ chức, hỗ trợ phân trang.",
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
                    "description": "Số lượng phần tử tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_WAREHOUSE_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouse_by_id",
        "description": "Lấy thông tin chi tiết của một kho hàng cụ thể trong tổ chức dựa trên ID kho.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}

GET_WAREHOUSE_BALANCES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouse_balances",
        "description": "Lấy danh sách số dư tồn kho (inventory balances) của một kho hàng cụ thể, hỗ trợ tìm kiếm sản phẩm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên sản phẩm (tùy chọn)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng phần tử tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}

GET_WAREHOUSE_BALANCE_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouse_balance_by_id",
        "description": "Lấy chi tiết của một bản ghi số dư tồn kho cụ thể trong kho hàng theo ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của bản ghi số dư tồn kho (UUID)",
                },
            },
            "required": ["organization_id", "warehouse_id", "id"],
        },
    },
}

GET_INVENTORY_DOCUMENTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_inventory_documents",
        "description": "Lấy danh sách tài liệu kho (nhập/xuất/điều chuyển kho - inventory documents) của một kho hàng cụ thể.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm tài liệu kho (tùy chọn)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng tài liệu tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}

GET_INVENTORY_DOCUMENT_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_inventory_document_by_id",
        "description": "Lấy chi tiết một tài liệu kho cụ thể dựa trên ID tài liệu kho.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
                "document_id": {
                    "type": "string",
                    "description": "ID của tài liệu kho (UUID)",
                },
            },
            "required": ["organization_id", "warehouse_id", "document_id"],
        },
    },
}

GET_REPLENISHMENT_REQUESTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_replenishment_requests",
        "description": "Lấy danh sách các yêu cầu bổ sung hàng tồn kho (replenishment requests) của một kho hàng cụ thể.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "ID của kho hàng (UUID)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng yêu cầu tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}
