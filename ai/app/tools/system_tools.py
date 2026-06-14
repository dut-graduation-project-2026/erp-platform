from ..integrations.erp_clients import erp_client


async def get_product_categories(
    organization_id: str, search: str = None, page: int = 1, limit: int = 100
) -> str:
    """Lấy danh sách các danh mục sản phẩm (product categories) của tổ chức, hỗ trợ tìm kiếm và phân trang."""
    params = {"page": page, "limit": limit}
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/product-categories",
        params=params,
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch product categories: {response.text}"
    return response.text


async def get_product_category_by_id(organization_id: str, id: str) -> str:
    """Lấy thông tin chi tiết của một danh mục sản phẩm cụ thể theo ID."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/product-categories/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch product category details: {response.text}"
    return response.text


async def get_taxes(
    organization_id: str,
    search: str = None,
    isArchived: bool = False,
    page: int = 1,
    limit: int = 100,
) -> str:
    """Lấy danh sách các mức thuế suất (taxes) của tổ chức, hỗ trợ lọc, tìm kiếm và phân trang."""
    params = {
        "isArchived": str(isArchived).lower(),
        "page": page,
        "limit": limit,
    }
    if search:
        params["search"] = search
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/taxes", params=params
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch taxes: {response.text}"
    return response.text


async def get_tax_by_id(organization_id: str, id: str) -> str:
    """Lấy thông tin chi tiết của một mã thuế suất cụ thể dựa trên ID thuế."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/taxes/{id}"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch tax details: {response.text}"
    return response.text


async def get_my_organizations() -> str:
    """Lấy danh sách tất cả các tổ chức (organizations) mà tài khoản người dùng hiện tại tham gia."""
    response = await erp_client.get(path="/organizations/me")
    if response.status_code != 200:
        return f"Error: Failed to fetch user's organizations: {response.text}"
    return response.text


async def get_organization_by_id(organization_id: str) -> str:
    """Lấy thông tin chi tiết của một tổ chức cụ thể theo ID tổ chức."""
    response = await erp_client.get(path=f"/organizations/{organization_id}")
    if response.status_code != 200:
        return f"Error: Failed to fetch organization details: {response.text}"
    return response.text


async def get_my_permissions(organization_id: str) -> str:
    """Lấy danh sách các mã quyền hạn (permissions) của người dùng hiện tại trong tổ chức chỉ định."""
    response = await erp_client.get(
        path=f"/organizations/{organization_id}/my-permissions"
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch permissions: {response.text}"
    return response.text


async def get_my_erp_modules_by_organization(organization_id: str) -> str:
    """Lấy danh sách các phân hệ ERP (modules) mà người dùng hiện tại được phép truy cập trong tổ chức."""
    response = await erp_client.get(
        path="/erp-modules/me", params={"organizationId": organization_id}
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch user's ERP modules: {response.text}"
    return response.text


async def get_erp_modules_by_organization(organization_id: str) -> str:
    """Lấy danh sách toàn bộ các phân hệ ERP (modules) hiện có/đang kích hoạt trong tổ chức."""
    response = await erp_client.get(
        path="/erp-modules", params={"organizationId": organization_id}
    )
    if response.status_code != 200:
        return f"Error: Failed to fetch organization's ERP modules: {response.text}"
    return response.text


# Schemas của các tool để khai báo với OpenAI/Gemini

GET_PRODUCT_CATEGORIES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_product_categories",
        "description": "Lấy danh sách các danh mục sản phẩm (product categories) của tổ chức, hỗ trợ tìm kiếm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm tên danh mục sản phẩm (tùy chọn)",
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

GET_PRODUCT_CATEGORY_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_product_category_by_id",
        "description": "Lấy thông tin chi tiết của một danh mục sản phẩm cụ thể theo ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của danh mục sản phẩm (UUID)",
                },
            },
            "required": ["organization_id", "id"],
        },
    },
}

GET_TAXES_TOOL = {
    "type": "function",
    "function": {
        "name": "get_taxes",
        "description": "Lấy danh sách các mức thuế suất (taxes) của tổ chức, hỗ trợ lọc, tìm kiếm và phân trang.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "search": {
                    "type": "string",
                    "description": "Từ khóa tìm kiếm theo tên hoặc mã thuế (tùy chọn)",
                },
                "isArchived": {
                    "type": "boolean",
                    "description": "Lọc các bản ghi thuế đã lưu trữ (mặc định: false)",
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

GET_TAX_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_tax_by_id",
        "description": "Lấy thông tin chi tiết của một mã thuế suất cụ thể dựa trên ID thuế.",
        "parameters": {
            "type": "object",
            "properties": {
                "organization_id": {
                    "type": "string",
                    "description": "ID của tổ chức (UUID)",
                },
                "id": {
                    "type": "string",
                    "description": "ID của thuế (UUID)",
                },
            },
            "required": ["organization_id", "id"],
        },
    },
}

GET_MY_ORGANIZATIONS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_organizations",
        "description": "Lấy danh sách tất cả các tổ chức (organizations) mà tài khoản người dùng hiện tại tham gia.",
        "parameters": {
            "type": "object",
            "properties": {},
        },
    },
}

GET_ORGANIZATION_BY_ID_TOOL = {
    "type": "function",
    "function": {
        "name": "get_organization_by_id",
        "description": "Lấy thông tin chi tiết của một tổ chức cụ thể theo ID tổ chức.",
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

GET_MY_PERMISSIONS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_permissions",
        "description": "Lấy danh sách các mã quyền hạn (permissions) của người dùng hiện tại trong tổ chức chỉ định.",
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

GET_MY_ERP_MODULES_BY_ORGANIZATION_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_erp_modules_by_organization",
        "description": "Lấy danh sách các phân hệ ERP (modules) mà người dùng hiện tại được phép truy cập trong tổ chức.",
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

GET_ERP_MODULES_BY_ORGANIZATION_TOOL = {
    "type": "function",
    "function": {
        "name": "get_erp_modules_by_organization",
        "description": "Lấy danh sách toàn bộ các phân hệ ERP (modules) hiện có/đang kích hoạt trong tổ chức.",
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
