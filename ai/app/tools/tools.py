from .order_tools import (
    get_organization_orders,
    get_confirmed_orders,
    get_order_details,
    get_order_items,
    get_order_item_by_id,
    get_quotations,
    get_quotation_by_id,
)
from .user_tools import get_organization_users, get_user_by_id
from .analytics_tools import (
    get_sales_summary,
    get_revenue_trend,
    get_conversion_funnel,
    get_top_performing_products,
    get_category_sales_distribution,
    get_lead_stage_funnel,
    get_pipeline_summary,
    get_stock_valuation_trend,
    get_asset_category_distribution,
)
from .cogs_tools import get_order_cogs
from .inventory_tools import (
    get_warehouses,
    get_warehouse_by_id,
    get_warehouse_balances,
    get_warehouse_balance_by_id,
    get_inventory_documents,
    get_inventory_document_by_id,
    get_replenishment_requests,
)
from .product_tools import get_products, get_product_details
from .invoice_tools import get_invoices, get_invoice_by_id, get_invoice_by_order_id
from .sale_team_tools import get_sale_teams, get_sale_team_by_id, get_my_sale_teams
from .crm_tools import get_leads, get_lead_by_id, get_partners, get_partner_by_id
from .system_tools import (
    get_product_categories,
    get_product_category_by_id,
    get_taxes,
    get_tax_by_id,
    get_my_organizations,
    get_organization_by_id,
    get_my_permissions,
    get_my_erp_modules_by_organization,
    get_erp_modules_by_organization,
)

AVAILABLE_TOOLS = {
    # Order & Quotation tools
    "get_organization_orders": get_organization_orders,
    "get_confirmed_orders": get_confirmed_orders,
    "get_order_details": get_order_details,
    "get_order_items": get_order_items,
    "get_order_item_by_id": get_order_item_by_id,
    "get_quotations": get_quotations,
    "get_quotation_by_id": get_quotation_by_id,
    
    # User tools
    "get_organization_users": get_organization_users,
    "get_user_by_id": get_user_by_id,
    
    # Analytics tools
    "get_sales_summary": get_sales_summary,
    "get_revenue_trend": get_revenue_trend,
    "get_conversion_funnel": get_conversion_funnel,
    "get_top_performing_products": get_top_performing_products,
    "get_category_sales_distribution": get_category_sales_distribution,
    "get_lead_stage_funnel": get_lead_stage_funnel,
    "get_pipeline_summary": get_pipeline_summary,
    "get_stock_valuation_trend": get_stock_valuation_trend,
    "get_asset_category_distribution": get_asset_category_distribution,
    
    # COGS tools
    "get_order_cogs": get_order_cogs,
    
    # Inventory tools
    "get_warehouses": get_warehouses,
    "get_warehouse_by_id": get_warehouse_by_id,
    "get_warehouse_balances": get_warehouse_balances,
    "get_warehouse_balance_by_id": get_warehouse_balance_by_id,
    "get_inventory_documents": get_inventory_documents,
    "get_inventory_document_by_id": get_inventory_document_by_id,
    "get_replenishment_requests": get_replenishment_requests,
    
    # Product tools
    "get_products": get_products,
    "get_product_details": get_product_details,
    
    # Invoice tools
    "get_invoices": get_invoices,
    "get_invoice_by_id": get_invoice_by_id,
    "get_invoice_by_order_id": get_invoice_by_order_id,
    
    # Sale Team tools
    "get_sale_teams": get_sale_teams,
    "get_sale_team_by_id": get_sale_team_by_id,
    "get_my_sale_teams": get_my_sale_teams,
    
    # CRM tools
    "get_leads": get_leads,
    "get_lead_by_id": get_lead_by_id,
    "get_partners": get_partners,
    "get_partner_by_id": get_partner_by_id,
    
    # System & Metadata tools
    "get_product_categories": get_product_categories,
    "get_product_category_by_id": get_product_category_by_id,
    "get_taxes": get_taxes,
    "get_tax_by_id": get_tax_by_id,
    "get_my_organizations": get_my_organizations,
    "get_organization_by_id": get_organization_by_id,
    "get_my_permissions": get_my_permissions,
    "get_my_erp_modules_by_organization": get_my_erp_modules_by_organization,
    "get_erp_modules_by_organization": get_erp_modules_by_organization,
}


