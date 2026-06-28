from typing import List, Dict, Any, Optional
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter()


class SalesForecastRequest(BaseModel):
    organizationId: str
    history: List[Dict[str, Any]]


class InventoryDataRequest(BaseModel):
    organizationId: str
    warehouses: List[Dict[str, Any]]
    balances: List[Dict[str, Any]]
    sales: List[Dict[str, Any]]
    force_refresh: Optional[bool] = False


class DashboardSummaryRequest(BaseModel):
    organizationId: str
    history: List[Dict[str, Any]]
    inventory_data: InventoryDataRequest


@router.get("/")
def root():
    return {"message": "Welcome to the ERP AI API!"}


@router.get("/health")
def health_check():
    return {"status": "ok"}


@router.post("/analysis/sales-forecast")
async def get_sales_forecast(req: SalesForecastRequest):
    from ..services.analysis_service import analysis_service
    return await analysis_service.analyze_sales_forecast(
        organization_id=req.organizationId,
        history=req.history
    )


@router.post("/analysis/inventory")
async def get_inventory_analysis(req: InventoryDataRequest):
    from ..services.analysis_service import analysis_service
    return await analysis_service.analyze_inventory_abc_xyz(
        organization_id=req.organizationId,
        warehouses=req.warehouses,
        balances=req.balances,
        sales=req.sales,
        force_refresh=req.force_refresh
    )


@router.post("/analysis/inventory-alerts")
async def get_inventory_alerts(req: InventoryDataRequest):
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_inventory_alerts(
        organization_id=req.organizationId,
        warehouses=req.warehouses,
        balances=req.balances,
        sales=req.sales
    )


@router.post("/analysis/reorder")
async def get_reorder_recommendations(req: InventoryDataRequest):
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_reorder_recommendations(
        organization_id=req.organizationId,
        warehouses=req.warehouses,
        balances=req.balances,
        sales=req.sales
    )


@router.post("/analysis/dashboard")
async def get_dashboard_summary(req: DashboardSummaryRequest):
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_dashboard_summary(
        organization_id=req.organizationId,
        history=req.history,
        inventory_data=req.inventory_data.model_dump()
    )
