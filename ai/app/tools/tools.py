from .order_tools import get_organization_orders, get_order_details
from .user_tools import get_organization_users
from .inventory_tools import get_warehouses, get_warehouse_balances, get_inventory_documents

AVAILABLE_TOOLS = {
    "get_organization_orders": get_organization_orders,
    "get_order_details": get_order_details,
    "get_organization_users": get_organization_users,
    "get_warehouses": get_warehouses,
    "get_warehouse_balances": get_warehouse_balances,
    "get_inventory_documents": get_inventory_documents,
}
