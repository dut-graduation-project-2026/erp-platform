from ..integrations.erp_clients import erp_client


async def get_sales_summary(
    organization_id: str, periodType: str = "YEAR", year: int = None
) -> str:
    """Lấy tóm tắt doanh số bán hàng của tổ chức (bao gồm tổng doanh thu, số lượng đơn hàng, số đơn nháp, số đơn bị hủy) theo chu kỳ thời gian và năm chỉ định."""
    params = {"periodType": periodType}
    if year is not None:
        params["year"] = year
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/sales/summary",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch sales summary: {response.text}"
    return response.text


async def get_revenue_trend(
    organization_id: str, months: int = 6, year: int = None
) -> str:
    """Lấy biểu đồ xu hướng doanh thu của tổ chức theo số tháng gần nhất hoặc theo năm chỉ định."""
    params = {"months": months}
    if year is not None:
        params["year"] = year
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/sales/revenue-trend",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch revenue trend: {response.text}"
    return response.text


async def get_conversion_funnel(
    organization_id: str, periodType: str = "YEAR", year: int = None
) -> str:
    """Lấy phễu chuyển đổi đơn hàng của tổ chức dựa trên số lượng đơn hàng ở từng trạng thái trong chu kỳ thời gian được chọn."""
    params = {"periodType": periodType}
    if year is not None:
        params["year"] = year
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/sales/conversion-funnel",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch conversion funnel: {response.text}"
    return response.text


async def get_top_performing_products(
    organization_id: str, limit: int = 10, startDate: str = None, endDate: str = None
) -> str:
    """Lấy danh sách các sản phẩm bán chạy nhất kèm tổng doanh số và doanh thu trong khoảng thời gian chỉ định."""
    params = {"limit": limit}
    if startDate:
        params["startDate"] = startDate
    if endDate:
        params["endDate"] = endDate
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/sales/top-products",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch top performing products: {response.text}"
    return response.text


async def get_category_sales_distribution(
    organization_id: str, startDate: str = None, endDate: str = None
) -> str:
    """Lấy phân bố doanh thu theo từng danh mục sản phẩm của tổ chức trong khoảng thời gian chỉ định."""
    params = {}
    if startDate:
        params["startDate"] = startDate
    if endDate:
        params["endDate"] = endDate
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/sales/category-distribution",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch category sales distribution: {response.text}"
    return response.text


async def get_lead_stage_funnel(organization_id: str) -> str:
    """Lấy số lượng khách hàng tiềm năng (leads) ở mỗi trạng thái trong phễu bán hàng hiện tại."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/pipeline/lead-funnel"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch lead stage funnel: {response.text}"
    return response.text


async def get_pipeline_summary(organization_id: str) -> str:
    """Lấy tóm tắt cơ hội kinh doanh/đường ống bán hàng bao gồm tổng giá trị dự kiến ở từng trạng thái."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/pipeline/summary"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch pipeline summary: {response.text}"
    return response.text


async def get_stock_valuation_trend(
    organization_id: str, months: int = 12, year: int = None
) -> str:
    """Lấy xu hướng biến động giá trị tài sản hàng tồn kho của tổ chức theo số tháng gần nhất hoặc năm chỉ định."""
    params = {"months": months}
    if year is not None:
        params["year"] = year
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/inventory/valuation-trend",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch stock valuation trend: {response.text}"
    return response.text


async def get_asset_category_distribution(organization_id: str) -> str:
    """Lấy phân bổ giá trị tài sản hàng tồn kho của tổ chức theo các danh mục sản phẩm khác nhau."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/analytics/inventory/asset-distribution"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch asset category distribution: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_SALES_SUMMARY_TOOL = {
    "type": "function",
    "function": {
        "name": "get_sales_summary",
        "description": "Lấy tóm tắt doanh số bán hàng của tổ chức (bao gồm tổng doanh thu, số lượng đơn hàng, số đơn nháp, số đơn bị hủy) theo chu kỳ thời gian và năm chỉ định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "periodType": {
                    "type": "string",
                    "enum": ["YEAR", "MONTH", "QUARTER", "WEEK"],
                    "description": "Chu kỳ thời gian lọc báo cáo (mặc định: YEAR)",
                },
                "year": {
                    "type": "integer",
                    "description": "Năm lọc dữ liệu (nếu không truyền sẽ lấy dữ liệu hiện tại)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_REVENUE_TREND_TOOL = {
    "type": "function",
    "function": {
        "name": "get_revenue_trend",
        "description": "Lấy biểu đồ xu hướng doanh thu của tổ chức theo số tháng gần nhất hoặc theo năm chỉ định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "months": {
                    "type": "integer",
                    "description": "Số tháng gần nhất muốn lấy xu hướng (mặc định: 6)",
                },
                "year": {
                    "type": "integer",
                    "description": "Năm cụ thể muốn lấy xu hướng (tùy chọn)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_CONVERSION_FUNNEL_TOOL = {
    "type": "function",
    "function": {
        "name": "get_conversion_funnel",
        "description": "Lấy phễu chuyển đổi đơn hàng của tổ chức dựa trên số lượng đơn hàng ở từng trạng thái trong chu kỳ thời gian được chọn.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "periodType": {
                    "type": "string",
                    "enum": ["YEAR", "MONTH", "QUARTER", "WEEK"],
                    "description": "Chu kỳ thời gian lọc báo cáo (mặc định: YEAR)",
                },
                "year": {
                    "type": "integer",
                    "description": "Năm lọc dữ liệu (nếu không truyền sẽ lấy dữ liệu hiện tại)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_TOP_PERFORMING_PRODUCTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_top_performing_products",
        "description": "Lấy danh sách các sản phẩm bán chạy nhất kèm tổng doanh số và doanh thu trong khoảng thời gian chỉ định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "limit": {
                    "type": "integer",
                    "description": "Giới hạn số lượng sản phẩm bán chạy nhất muốn lấy (mặc định: 10)",
                },
                "startDate": {
                    "type": "string",
                    "description": "Thời điểm bắt đầu (ISO 8601 string, ví dụ: 2026-05-14T08:00:00Z)",
                },
                "endDate": {
                    "type": "string",
                    "description": "Thời điểm kết thúc (ISO 8601 string, ví dụ: 2026-06-13T08:00:00Z)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_CATEGORY_SALES_DISTRIBUTION_TOOL = {
    "type": "function",
    "function": {
        "name": "get_category_sales_distribution",
        "description": "Lấy phân bố doanh thu theo từng danh mục sản phẩm của tổ chức trong khoảng thời gian chỉ định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "startDate": {
                    "type": "string",
                    "description": "Thời điểm bắt đầu (ISO 8601 string, ví dụ: 2026-05-14T08:00:00Z)",
                },
                "endDate": {
                    "type": "string",
                    "description": "Thời điểm kết thúc (ISO 8601 string, ví dụ: 2026-06-13T08:00:00Z)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_LEAD_STAGE_FUNNEL_TOOL = {
    "type": "function",
    "function": {
        "name": "get_lead_stage_funnel",
        "description": "Lấy số lượng khách hàng tiềm năng (leads) ở mỗi trạng thái trong phễu bán hàng hiện tại.",
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

GET_PIPELINE_SUMMARY_TOOL = {
    "type": "function",
    "function": {
        "name": "get_pipeline_summary",
        "description": "Lấy tóm tắt cơ hội kinh doanh/đường ống bán hàng bao gồm tổng giá trị dự kiến ở từng trạng thái.",
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

GET_STOCK_VALUATION_TREND_TOOL = {
    "type": "function",
    "function": {
        "name": "get_stock_valuation_trend",
        "description": "Lấy xu hướng biến động giá trị tài sản hàng tồn kho của tổ chức theo số tháng gần nhất hoặc năm chỉ định.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "months": {
                    "type": "integer",
                    "description": "Số tháng gần nhất muốn lấy xu hướng (mặc định: 12)",
                },
                "year": {
                    "type": "integer",
                    "description": "Năm cụ thể muốn lấy xu hướng (tùy chọn)",
                },
            },
            "required": ["organization_id"],
        },
    },
}

GET_ASSET_CATEGORY_DISTRIBUTION_TOOL = {
    "type": "function",
    "function": {
        "name": "get_asset_category_distribution",
        "description": "Lấy phân bổ giá trị tài sản hàng tồn kho của tổ chức theo các danh mục sản phẩm khác nhau.",
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
