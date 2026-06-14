from ..integrations.erp_clients import erp_client


async def get_products(
    organization_id: str,
    search: str = None,
    isArchived: bool = False,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách các sản phẩm (products) của một tổ chức, hỗ trợ tìm kiếm, lọc theo trạng thái lưu trữ (archived) và phân trang."""
    params = {
        "isArchived": str(isArchived).lower(),
        "page": page,
        "limit": limit,
    }
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/products", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch products: {response.text}"
    return response.text


async def get_product_details(organization_id: str, product_id: str) -> str:
    """Lấy thông tin chi tiết của một sản phẩm cụ thể theo ID."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/products/{product_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch product details: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_PRODUCTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_products",
        "description": "Lấy danh sách các sản phẩm (products) của một tổ chức, hỗ trợ tìm kiếm, lọc theo trạng thái lưu trữ (archived) và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên hoặc mã sản phẩm (tùy chọn)",
                },
                "isArchived": {
                    "type": "boolean",
                    "description": "Lọc sản phẩm đã lưu trữ hay chưa (mặc định: false)",
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

GET_PRODUCT_DETAILS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_product_details",
        "description": "Lấy thông tin chi tiết của một sản phẩm cụ thể theo ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "product_id": {
                    "type": "string",
                    "description": "ID của sản phẩm (UUID)",
                },
            },
            "required": ["organization_id", "product_id"],
        },
    },
}
