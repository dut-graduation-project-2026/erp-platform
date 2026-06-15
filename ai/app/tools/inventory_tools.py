from ..integrations.erp_clients import erp_client


async def get_warehouses(organization_id: str) -> str:
    """Lấy danh sách các kho hàng (warehouses) của một tổ chức."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses",
        params={"limit": 100}
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch warehouses: {response.text}"
    return response.text


async def get_warehouse_balances(organization_id: str, warehouse_id: str) -> str:
    """Lấy số dư tồn kho (balances) của tất cả sản phẩm trong một kho hàng cụ thể."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/balances",
        params={"limit": 100}
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch warehouse balances: {response.text}"
    return response.text


async def get_inventory_documents(
    organization_id: str, warehouse_id: str, search: str = None
) -> str:
    """Lấy lịch sử phiếu xuất nhập kho (inventory documents) của một kho hàng cụ thể."""
    params = {"limit": 100}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/warehouses/{warehouse_id}/documents",
        params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch inventory documents: {response.text}"
    return response.text


# Schemas để khai báo với OpenAI
GET_WAREHOUSES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouses",
        "description": "Retrieve the list of warehouses of an organization.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "The unique identifier of the organization (UUID).",
                }
            },
            "required": ["organization_id"],
        },
    },
}

GET_WAREHOUSE_BALANCES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_warehouse_balances",
        "description": "Retrieve the inventory balances of all products in a specific warehouse.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "The unique identifier of the organization (UUID).",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "The unique identifier of the warehouse (UUID).",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}

GET_INVENTORY_DOCUMENTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_inventory_documents",
        "description": "Retrieve the history of inventory import/export documents of a specific warehouse.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "The unique identifier of the organization (UUID).",
                },
                "warehouse_id": {
                    "type": "string",
                    "description": "The unique identifier of the warehouse (UUID).",
                },
                "search": {
                    "type": "string",
                    "description": "Optional keyword to search/filter documents.",
                },
            },
            "required": ["organization_id", "warehouse_id"],
        },
    },
}
