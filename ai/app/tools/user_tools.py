from ..integrations.erp_clients import erp_client


async def get_organization_users(organization_id: str) -> str:
    """Lấy danh sách toàn bộ người dùng (users) của một tổ chức dựa trên ID tổ chức."""
    response = await erp_client.get(
        path="/users", params={"organizationId": organization_id}
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch users: {response.text}"
    return response.text


# Schema của tool để khai báo với OpenAI
GET_ORGANIZATION_USERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_organization_users",
        "description": "Lấy danh sách toàn bộ người dùng (users) của một tổ chức dựa trên ID tổ chức.",
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
