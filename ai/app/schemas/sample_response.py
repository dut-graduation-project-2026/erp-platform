from typing import List, Optional
from pydantic import BaseModel, Field


class ComplexAnalysisResponse(BaseModel):
    summary: str = Field(description="Concise summary of the text content in about 2 sentences.")
    main_topic: str = Field(description="Main topic of the text.")
    tags: List[str] = Field(
        description="List of classification tags/labels (maximum 5 tags)."
    )
    reading_time_minutes: int = Field(
        description="Estimated reading time in minutes."
    )

    # Optional fields
    warning_flag: Optional[str] = Field(
        None,
        description="Warning flag if the content violates policy or is sensitive; null if none.",
    )


class UserBaseSchema(BaseModel):
    id: str = Field(description="Unique identifier of the user (UUID).")
    email: str = Field(description="Email address of the user.")
    firstName: Optional[str] = Field(None, description="First name of the user.")
    lastName: Optional[str] = Field(None, description="Last name of the user.")



class PaginationSchema(BaseModel):
    page: int = Field(description="Current page number.")
    limit: int = Field(description="Maximum number of items per page.")
    totalItems: int = Field(description="Total number of items.")
    totalPages: int = Field(description="Total number of pages.")
    hasNext: bool = Field(description="Whether there is a next page.")
    hasPrev: bool = Field(description="Whether there is a previous page.")


class PagedUserListSchema(BaseModel):
    data: List[UserBaseSchema] = Field(description="List of users.")
    pagination: PaginationSchema = Field(description="Pagination details.")


class UserAnalysisResponse(BaseModel):
    summary: str = Field(description="Overview summary of the organization's personnel team in about 2-3 sentences.")
    total_users: int = Field(description="Total number of users in the organization.")
    departments_or_groups: List[str] = Field(description="Predicted or suggested departments/working groups based on roles or email domains (e.g., Sales, Warehouse, Admin).")
    email_domains: List[str] = Field(description="List of unique email domains used in the organization.")
    top_email_domains_analysis: str = Field(description="Detailed analysis of email domains used (e.g., company internal domain vs. public domain like gmail).")
    recommendations: List[str] = Field(description="Suggestions/recommendations for optimizing personnel management or permissions in the organization (e.g., standardizing emails, setting up departmental groups).")


class OrderBaseSchema(BaseModel):
    id: str = Field(description="Unique identifier of the order (UUID).")
    orderNumber: str = Field(description="Order number.")
    status: str = Field(description="Order status.")
    totalAmount: float = Field(description="Total amount of the order.")
    createdAt: Optional[str] = Field(None, description="Creation timestamp of the order (ISO string).")
    deliveryDate: Optional[str] = Field(None, description="Delivery timestamp of the order (ISO string).")


class PagedOrderListSchema(BaseModel):
    data: List[OrderBaseSchema] = Field(description="List of orders.")
    pagination: PaginationSchema = Field(description="Pagination details.")


class ProductBaseSchema(BaseModel):
    id: str = Field(description="Unique identifier of the product.")
    name: str = Field(description="Product name.")
    price: float = Field(description="Unit price of the product.")
    description: Optional[str] = Field(None, description="Product description.")


class OrderItemDetailSchema(BaseModel):
    id: str = Field(description="Unique identifier of the order item detail.")
    product: ProductBaseSchema = Field(description="Product details.")
    quantity: float = Field(description="Quantity of the product.")
    unitPrice: float = Field(description="Unit price at the time of purchase.")
    subtotal: float = Field(description="Subtotal amount of the product (quantity * unitPrice).")


class OrderDetailSchema(BaseModel):
    id: str = Field(description="Unique identifier of the order.")
    orderNumber: str = Field(description="Order number.")
    status: str = Field(description="Order status.")
    totalAmount: float = Field(description="Total amount of the order.")
    items: List[OrderItemDetailSchema] = Field(description="List of items in the order.")
    createdAt: str = Field(description="Creation timestamp of the order (ISO string).")


class ProductSalesSummary(BaseModel):
    product_id: str = Field(description="Product identifier.")
    product_name: str = Field(description="Product name.")
    quantity_sold: float = Field(description="Quantity sold.")
    revenue: float = Field(description="Revenue generated from this product.")


class SalesAnalysisResponse(BaseModel):
    summary: str = Field(description="Overview summary of the organization's sales status in the past month in about 3-4 sentences.")
    total_revenue: float = Field(description="Total revenue in the past month.")
    total_items_sold: float = Field(description="Total quantity of products sold.")
    top_selling_products: List[ProductSalesSummary] = Field(description="List of top selling products with details on quantity sold and revenue.")
    sales_trends_and_insights: str = Field(description="Detailed analysis of sales trends, potential products, or notable highlights during the month.")
    inventory_recommendations: List[str] = Field(description="Inventory/stocking recommendations based on sales volume.")


class ForecastPoint(BaseModel):
    date: str = Field(description="Date formatted as YYYY-MM-DD.")
    historical_revenue: Optional[float] = Field(None, description="Actual historical revenue (if available).")
    predicted_revenue: float = Field(description="Predicted forecasted revenue.")


class SalesForecastResponse(BaseModel):
    summary: str = Field(description="AI forecast summary comments (must be in Vietnamese).")
    forecast_30d_total_revenue: float = Field(description="Forecasted total revenue for the next 30 days.")
    forecast_points: List[ForecastPoint] = Field(description="List of chart points for actual and forecasted revenue.")
    insights: List[str] = Field(description="In-depth insights and notes from the AI (must be in Vietnamese).")


class ProductAbcXyz(BaseModel):
    productId: str = Field(description="Product identifier.")
    productName: str = Field(description="Product name.")
    abcClass: str = Field(description="ABC classification class (A, B, or C).")
    xyzClass: str = Field(description="XYZ classification class (X, Y, or Z).")
    currentStock: float = Field(description="Actual physical stock level.")
    rop: float = Field(description="Reorder Point (ROP) level.")
    eoq: float = Field(description="Economic Order Quantity (EOQ).")
    status: str = Field(description="Inventory status: OK, WARNING, or CRITICAL.")


class InventoryAnalysisResponse(BaseModel):
    summary: str = Field(description="AI inventory analysis summary comments (must be in Vietnamese).")
    abc_xyz_matrix: List[ProductAbcXyz] = Field(description="ABC-XYZ classification matrix and inventory metrics.")
    critical_stock_count: int = Field(description="Number of products in CRITICAL status (stock below ROP).")
    recommendations: List[str] = Field(description="Stock optimization recommendations (must be in Vietnamese).")


class ReorderItem(BaseModel):
    productId: str = Field(description="Product identifier.")
    productName: str = Field(description="Product name.")
    warehouseId: str = Field(description="Warehouse identifier.")
    warehouseName: str = Field(description="Warehouse name.")
    currentStock: float = Field(description="Current physical stock level.")
    rop: float = Field(description="Reorder Point (ROP).")
    eoq: float = Field(description="Economic Order Quantity (EOQ).")
    recommendedQuantity: float = Field(description="Recommended restocking quantity.")
    urgency: str = Field(description="Urgency level (HIGH, MEDIUM, or LOW).")
    notes: str = Field(description="Restocking reason or notes (must be in Vietnamese).")


class ReorderRecommendationResponse(BaseModel):
    recommendations: List[ReorderItem] = Field(description="List of restocking recommendations.")


class DashboardSummaryResponse(BaseModel):
    summary: str = Field(description="Daily brief summary by the AI (must be in Vietnamese).")
    alerts: List[str] = Field(description="List of quick alerts (must be in Vietnamese).")


# ─── LIGHTWEIGHT SCHEMAS FOR LLM RESPONSES (TO PREVENT TRUNCATION) ───
class SalesForecastLLMResponse(BaseModel):
    summary: str = Field(description="AI forecast summary comments (must be in Vietnamese).")
    insights: List[str] = Field(description="In-depth insights and notes from the AI (must be in Vietnamese).")


class InventoryLLMResponse(BaseModel):
    summary: str = Field(description="AI inventory analysis summary comments (must be in Vietnamese).")
    recommendations: List[str] = Field(description="Stock optimization recommendations (must be in Vietnamese).")


class ReorderItemLLM(BaseModel):
    productId: str = Field(description="Product identifier.")
    notes: str = Field(description="Short restocking reason notes rewritten (must be in Vietnamese, maximum 15 words).")


class ReorderRecommendationLLMResponse(BaseModel):
    recommendations: List[ReorderItemLLM] = Field(description="List of restocking recommendation notes.")
