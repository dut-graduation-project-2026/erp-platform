from typing import List, Optional
from pydantic import BaseModel, Field


class ForecastPoint(BaseModel):
    date: str = Field(description="Date formatted as YYYY-MM-DD.")
    historical_revenue: Optional[float] = Field(None, description="Actual historical revenue (if available).")
    predicted_revenue: float = Field(description="Predicted forecasted revenue.")


class SalesForecastResponse(BaseModel):
    summary: str = Field(description="AI forecast summary comments in English.")
    forecast_30d_total_revenue: float = Field(description="Forecasted total revenue for the next 30 days.")
    forecast_points: List[ForecastPoint] = Field(description="List of chart points for actual and forecasted revenue.")
    insights: List[str] = Field(description="In-depth insights and notes from the AI in English.")


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
    summary: str = Field(description="AI inventory analysis summary comments in English.")
    abc_xyz_matrix: List[ProductAbcXyz] = Field(description="ABC-XYZ classification matrix and inventory metrics.")
    critical_stock_count: int = Field(description="Number of products in CRITICAL status (stock below ROP).")
    recommendations: List[str] = Field(description="Stock optimization recommendations in English.")


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
    notes: str = Field(description="Restocking reason or notes in English.")


class ReorderRecommendationResponse(BaseModel):
    recommendations: List[ReorderItem] = Field(description="List of restocking recommendations.")


class DashboardSummaryResponse(BaseModel):
    summary: str = Field(description="Daily brief summary by the AI in English.")
    alerts: List[str] = Field(description="List of quick alerts in English.")


# ─── LIGHTWEIGHT SCHEMAS FOR LLM RESPONSES (TO PREVENT TRUNCATION) ───
class SalesForecastLLMResponse(BaseModel):
    summary: str = Field(description="AI forecast summary comments in English.")
    insights: List[str] = Field(description="In-depth insights and notes from the AI in English.")


class InventoryLLMResponse(BaseModel):
    summary: str = Field(description="AI inventory analysis summary comments in English.")
    recommendations: List[str] = Field(description="Stock optimization recommendations in English.")


class ReorderItemLLM(BaseModel):
    productId: str = Field(description="Product identifier.")
    notes: str = Field(description="Short restocking reason notes rewritten in English (maximum 15 words).")


class ReorderRecommendationLLMResponse(BaseModel):
    recommendations: List[ReorderItemLLM] = Field(description="List of restocking recommendation notes.")
