from ..integrations.erp_clients import erp_client


async def get_organization_users(
    organization_id: str, query: str = None, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách người dùng (users) của một tổ chức, hỗ trợ tìm kiếm theo tên/email và phân trang."""
    params = {"organizationId": organization_id, "page": page, "limit": limit}
    if query:
        params["query"] = query
    response = await erp_client.get(path="/users", params=params)
    if response.status_code != 200:
        return f"Error: Failed to fetch users: {response.text}"
    return response.text


async def get_user_by_id(organization_id: str, user_id: str) -> str:
    """Lấy thông tin chi tiết của một thành viên/người dùng cụ thể trong tổ chức theo ID."""
    params = {"organizationId": organization_id}
    response = await erp_client.get(path=f"/users/{user_id}", params=params)
    if response.status_code != 200:
        return f"Error: Failed to fetch user details: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_ORGANIZATION_USERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_organization_users",
        "description": "Lấy danh sách người dùng (users) của một tổ chức, hỗ trợ tìm kiếm theo tên/email và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "query": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên hoặc email của người dùng (tùy chọn)",
                },
                "page": {
                    "type": "integer",
                    "description": "Số trang cần lấy (bắt đầu từ 1, mặc định: 1)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Số lượng người dùng tối đa trên mỗi trang (mặc định: 100)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_USER_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_user_by_id",
        "description": "Lấy thông tin chi tiết của một thành viên/người dùng cụ thể trong tổ chức theo ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "user_id": {
                    "type": "string",
                    "description": "ID của người dùng cần lấy thông tin (UUID)",
                },
            },
            "required": ["organization_id", "user_id"],
        },
    },
}
