from ..integrations.erp_clients import erp_client


async def get_leads(
    organization_id: str, search: str = None, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách khách hàng tiềm năng (leads) của tổ chức, hỗ trợ tìm kiếm và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/leads", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch leads: {response.text}"
    return response.text


async def get_lead_by_id(organization_id: str, lead_id: str) -> str:
    """Lấy thông tin chi tiết của một khách hàng tiềm năng (lead) cụ thể theo ID."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/leads/{lead_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch lead details: {response.text}"
    return response.text


async def get_partners(organization_id: str) -> str:
    """Lấy danh sách toàn bộ đối tác (khách hàng/nhà cung cấp - partners) của tổ chức."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/partners"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch partners: {response.text}"
    return response.text


async def get_partner_by_id(organization_id: str, partner_id: str) -> str:
    """Lấy thông tin chi tiết của một đối tác cụ thể dựa trên ID đối tác."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/partners/{partner_id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch partner details: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_LEADS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_leads",
        "description": "Lấy danh sách khách hàng tiềm năng (leads) của tổ chức, hỗ trợ tìm kiếm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên, email, hoặc số điện thoại lead (tùy chọn)",
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

GET_LEAD_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_lead_by_id",
        "description": "Lấy thông tin chi tiết của một khách hàng tiềm năng (lead) cụ thể theo ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "lead_id": {
                    "type": "string",
                    "description": "ID của lead (UUID)",
                },
            },
            "required": ["organization_id", "lead_id"],
        },
    },
}

GET_PARTNERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_partners",
        "description": "Lấy danh sách toàn bộ đối tác (khách hàng/nhà cung cấp - partners) của tổ chức.",
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

GET_PARTNER_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_partner_by_id",
        "description": "Lấy thông tin chi tiết của một đối tác cụ thể dựa trên ID đối tác.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "partner_id": {
                    "type": "string",
                    "description": "ID của đối tác (UUID)",
                },
            },
            "required": ["organization_id", "partner_id"],
        },
    },
}
