from typing import Optional
from fastapi import APIRouter, Query, HTTPException
from ..integrations.openai_clients import openai_client

router = APIRouter()


@router.get("/")
def root():
    return {"message": "Welcome to the ERP AI API!"}


@router.get("/health")
def health_check():
    return {"status": "ok"}


@router.get("/hello")
def say_hello():
    return {"message": "Hello from the ERP AI API!"}


@router.get("/sample")
async def sample_response():
    from ..schemas.sample_response import ComplexAnalysisResponse

    sample_text = """
        Hôm nay công ty công nghệ X vừa ra mắt mẫu điện thoại thông minh mới tích hợp AI thế hệ mới. 
        Sản phẩm nhận được rất nhiều lời khen ngợi về thiết kế đột phá và thời lượng pin trâu. Tuy nhiên, 
        mức giá khởi điểm lên tới 2.000 USD đang khiến nhiều người tiêu dùng đắn đo, một số ý kiến cho rằng 
        mức giá này quá ảo tưởng trong tình hình kinh tế hiện tại.
    """

    messages = [
        {
            "role": "system",
            "content": "You are a text data analysis expert. Analyze the provided text. Output your summary, topics, and warnings in Vietnamese.",
        },
        {"role": "user", "content": sample_text},
    ]

    response = await openai_client.chat(
        messages=messages,
        response_format=ComplexAnalysisResponse,
    )

    return response.choices[0].message.parsed


@router.get("/analysis/users")
async def analyze_organization_users(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )

    from ..services.analysis_service import analysis_service

    return await analysis_service.analyze_users_of_organization(actual_org_id)


@router.get("/analysis/sales")
async def analyze_organization_sales(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )

    from ..services.analysis_service import analysis_service

    return await analysis_service.analyze_sales_of_last_month(actual_org_id)


@router.get("/analysis/sales-forecast")
async def get_sales_forecast(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )
    from ..services.analysis_service import analysis_service
    return await analysis_service.analyze_sales_forecast(actual_org_id)


@router.get("/analysis/inventory")
async def get_inventory_analysis(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None),
    force_refresh: bool = Query(False)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )
    from ..services.analysis_service import analysis_service
    return await analysis_service.analyze_inventory_abc_xyz(actual_org_id, force_refresh)


@router.get("/analysis/inventory-alerts")
async def get_inventory_alerts(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_inventory_alerts(actual_org_id)


@router.get("/analysis/reorder")
async def get_reorder_recommendations(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_reorder_recommendations(actual_org_id)


@router.get("/analysis/dashboard")
async def get_dashboard_summary(
    organization_id: Optional[str] = Query(None),
    organizationId: Optional[str] = Query(None)
):
    actual_org_id = organization_id or organizationId
    if not actual_org_id:
        raise HTTPException(
            status_code=400,
            detail="Either organization_id or organizationId query parameter must be provided"
        )
    from ..services.analysis_service import analysis_service
    return await analysis_service.get_dashboard_summary(actual_org_id)



