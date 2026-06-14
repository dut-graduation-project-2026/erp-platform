from ..integrations.erp_clients import erp_client


async def get_order_cogs(organization_id: str, order_id: str) -> str:
    """Lấy chi phí vốn hàng bán (COGS - Cost of Goods Sold) của một đơn hàng cụ thể."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/orders/{order_id}/cogs"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch order COGS: {response.text}"
    return response.text


# Schema của tool để khai báo với OpenAI/Gemini

GET_ORDER_COGS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_cogs",
        "description": "Lấy chi phí vốn hàng bán (COGS - Cost of Goods Sold) của một đơn hàng cụ thể.",
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
