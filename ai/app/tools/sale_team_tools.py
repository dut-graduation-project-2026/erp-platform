from ..integrations.erp_clients import erp_client


async def get_sale_teams(
    organization_id: str,
    search: str = None,
    isArchived: bool = False,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách các nhóm bán hàng (sale teams) của tổ chức, hỗ trợ tìm kiếm, lọc theo trạng thái lưu trữ và phân trang."""
    params = {
        "isArchived": str(isArchived).lower(),
        "page": page,
        "limit": limit,
    }
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/sale-teams", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch sale teams: {response.text}"
    return response.text


async def get_sale_team_by_id(organization_id: str, id: str) -> str:
    """Lấy thông tin chi tiết của một nhóm bán hàng cụ thể dựa trên ID nhóm."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/sale-teams/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch sale team details: {response.text}"
    return response.text


async def get_my_sale_teams(organization_id: str) -> str:
    """Lấy danh sách các nhóm bán hàng mà người dùng hiện tại đang tham gia trong tổ chức."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/sale-teams/me"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch current user's sale teams: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_SALE_TEAMS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_sale_teams",
        "description": "Lấy danh sách các nhóm bán hàng (sale teams) của tổ chức, hỗ trợ tìm kiếm, lọc theo trạng thái lưu trữ và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên đội ngũ bán hàng (tùy chọn)",
                },
                "isArchived": {
                    "type": "boolean",
                    "description": "Lọc các đội ngũ đã lưu trữ (mặc định: false)",
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

GET_SALE_TEAM_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_sale_team_by_id",
        "description": "Lấy thông tin chi tiết của một nhóm bán hàng cụ thể dựa trên ID nhóm.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của đội ngũ bán hàng (UUID)",
                },
            },
            "required": ["organization_id", "id"],
        },
    },
}

GET_MY_SALE_TEAMS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_sale_teams",
        "description": "Lấy danh sách các nhóm bán hàng mà người dùng hiện tại đang tham gia trong tổ chức.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                }
            },
            "required": ["organization_id"],
        },
    },
}
