from typing import List, Optional
from pydantic import BaseModel, Field


class ComplexAnalysisResponse(BaseModel):
    summary: str = Field(description="Tóm tắt ngắn gọn nội dung bài viết khoảng 2 câu")
    main_topic: str = Field(description="Chủ đề chính của bài viết")
    tags: List[str] = Field(
        description="Danh sách các thẻ/nhãn phân loại (tối đa 5 thẻ)"
    )
    reading_time_minutes: int = Field(
        description="Thời gian đọc ước tính tính bằng phút"
    )

    # Trường dữ liệu có thể có hoặc không (Optional)
    warning_flag: Optional[str] = Field(
        None,
        description="Cảnh báo nếu nội dung vi phạm chính sách hoặc nhạy cảm, nếu không có thì để null",
    )


class UserBaseSchema(BaseModel):
    id: str = Field(description="Mã định danh duy nhất của người dùng (UUID)")
    email: str = Field(description="Địa chỉ email của người dùng")
    firstName: Optional[str] = Field(None, description="Tên của người dùng")
    lastName: Optional[str] = Field(None, description="Họ của người dùng")



class PaginationSchema(BaseModel):
    page: int = Field(description="Trang hiện tại")
    limit: int = Field(description="Số lượng phần tử tối đa trên mỗi trang")
    totalItems: int = Field(description="Tổng số lượng phần tử")
    totalPages: int = Field(description="Tổng số trang")
    hasNext: bool = Field(description="Có trang tiếp theo hay không")
    hasPrev: bool = Field(description="Có trang trước đó hay không")


class PagedUserListSchema(BaseModel):
    data: List[UserBaseSchema] = Field(description="Danh sách người dùng")
    pagination: PaginationSchema = Field(description="Thông tin phân trang")


class UserAnalysisResponse(BaseModel):
    summary: str = Field(description="Tóm tắt tổng quan về đội ngũ nhân sự của tổ chức khoảng 2-3 câu")
    total_users: int = Field(description="Tổng số lượng người dùng trong tổ chức")
    departments_or_groups: List[str] = Field(description="Dự đoán hoặc gợi ý các phòng ban/nhóm làm việc dựa trên vai trò hoặc email (ví dụ: Sales, Warehouse, Admin)")
    email_domains: List[str] = Field(description="Danh sách các tên miền email duy nhất được sử dụng trong tổ chức")
    top_email_domains_analysis: str = Field(description="Phân tích chi tiết về các tên miền email được sử dụng (ví dụ: tên miền nội bộ công ty so với tên miền công cộng như gmail)")
    recommendations: List[str] = Field(description="Gợi ý/khuyến nghị tối ưu hóa quản lý nhân sự hoặc phân quyền trong tổ chức (ví dụ: chuẩn hóa email, thiết lập nhóm phòng ban)")


class OrderBaseSchema(BaseModel):
    id: str = Field(description="Mã định danh duy nhất của đơn hàng (UUID)")
    orderNumber: str = Field(description="Mã số đơn hàng")
    status: str = Field(description="Trạng thái đơn hàng")
    totalAmount: float = Field(description="Tổng tiền đơn hàng")


class PagedOrderListSchema(BaseModel):
    data: List[OrderBaseSchema] = Field(description="Danh sách các đơn hàng")
    pagination: PaginationSchema = Field(description="Thông tin phân trang")


class ProductBaseSchema(BaseModel):
    id: str = Field(description="Mã định danh duy nhất của sản phẩm")
    name: str = Field(description="Tên sản phẩm")
    price: float = Field(description="Đơn giá sản phẩm")
    description: Optional[str] = Field(None, description="Mô tả sản phẩm")


class OrderItemDetailSchema(BaseModel):
    id: str = Field(description="Mã định danh duy nhất của chi tiết đơn hàng")
    product: ProductBaseSchema = Field(description="Thông tin sản phẩm")
    quantity: float = Field(description="Số lượng sản phẩm")
    unitPrice: float = Field(description="Đơn giá tại thời điểm mua")
    subtotal: float = Field(description="Thành tiền của sản phẩm")


class OrderDetailSchema(BaseModel):
    id: str = Field(description="Mã định danh duy nhất của đơn hàng")
    orderNumber: str = Field(description="Mã số đơn hàng")
    status: str = Field(description="Trạng thái đơn hàng")
    totalAmount: float = Field(description="Tổng tiền đơn hàng")
    items: List[OrderItemDetailSchema] = Field(description="Danh sách các mặt hàng trong đơn")
    createdAt: str = Field(description="Thời điểm tạo đơn hàng (ISO string)")


class ProductSalesSummary(BaseModel):
    product_id: str = Field(description="Mã sản phẩm")
    product_name: str = Field(description="Tên sản phẩm")
    quantity_sold: float = Field(description="Số lượng đã bán")
    revenue: float = Field(description="Doanh thu từ sản phẩm này")


class SalesAnalysisResponse(BaseModel):
    summary: str = Field(description="Tóm tắt tổng quan về tình hình bán hàng của tổ chức trong tháng qua khoảng 3-4 câu")
    total_revenue: float = Field(description="Tổng doanh thu trong tháng qua")
    total_items_sold: float = Field(description="Tổng số lượng sản phẩm bán ra")
    top_selling_products: List[ProductSalesSummary] = Field(description="Danh sách sản phẩm bán chạy nhất kèm chi tiết số lượng và doanh thu")
    sales_trends_and_insights: str = Field(description="Phân tích chi tiết về xu hướng bán hàng, sản phẩm tiềm năng hoặc các điểm đáng chú ý trong tháng")
    inventory_recommendations: List[str] = Field(description="Khuyến nghị nhập hàng/tối ưu tồn kho dựa trên lượng bán ra")


class ForecastPoint(BaseModel):
    date: str = Field(description="Ngày định dạng YYYY-MM-DD")
    historical_revenue: Optional[float] = Field(None, description="Doanh thu thực tế (nếu có)")
    predicted_revenue: float = Field(description="Doanh thu dự báo")


class SalesForecastResponse(BaseModel):
    summary: str = Field(description="Nhận xét dự báo của AI bằng tiếng Việt")
    forecast_30d_total_revenue: float = Field(description="Dự báo tổng doanh thu trong 30 ngày tới")
    forecast_points: List[ForecastPoint] = Field(description="Danh sách các điểm biểu đồ doanh số thực tế và dự báo")
    insights: List[str] = Field(description="Các nhận xét chuyên sâu từ AI")


class ProductAbcXyz(BaseModel):
    productId: str = Field(description="Mã sản phẩm")
    productName: str = Field(description="Tên sản phẩm")
    abcClass: str = Field(description="Nhóm ABC (A, B, C)")
    xyzClass: str = Field(description="Nhóm XYZ (X, Y, Z)")
    currentStock: float = Field(description="Tồn kho thực tế")
    rop: float = Field(description="Điểm đặt hàng lại (Reorder Point)")
    eoq: float = Field(description="Lượng đặt hàng kinh tế (EOQ)")
    status: str = Field(description="Trạng thái tồn kho: OK, WARNING, CRITICAL")


class InventoryAnalysisResponse(BaseModel):
    summary: str = Field(description="Tóm tắt phân tích tồn kho của AI")
    abc_xyz_matrix: List[ProductAbcXyz] = Field(description="Ma trận phân loại ABC-XYZ và các chỉ số tồn kho")
    critical_stock_count: int = Field(description="Số lượng sản phẩm ở trạng thái CRITICAL (tồn kho dưới ROP)")
    recommendations: List[str] = Field(description="Đề xuất tối ưu hóa tồn kho")


class ReorderItem(BaseModel):
    productId: str = Field(description="Mã sản phẩm")
    productName: str = Field(description="Tên sản phẩm")
    warehouseId: str = Field(description="Mã kho")
    warehouseName: str = Field(description="Tên kho")
    currentStock: float = Field(description="Tồn kho hiện tại")
    rop: float = Field(description="Điểm đặt hàng lại ROP")
    eoq: float = Field(description="Lượng đặt hàng tối ưu EOQ")
    recommendedQuantity: float = Field(description="Số lượng đề xuất nhập")
    urgency: str = Field(description="Mức độ khẩn cấp (HIGH, MEDIUM, LOW)")
    notes: str = Field(description="Lý do đề xuất (ví dụ: Tồn kho < ROP)")


class ReorderRecommendationResponse(BaseModel):
    recommendations: List[ReorderItem] = Field(description="Danh sách các đề xuất nhập hàng")


class DashboardSummaryResponse(BaseModel):
    summary: str = Field(description="Bản tin tóm tắt hàng ngày của AI bằng tiếng Việt")
    alerts: List[str] = Field(description="Danh sách cảnh báo nhanh")


# ─── LIGHTWEIGHT SCHEMAS FOR LLM RESPONSES (TO PREVENT TRUNCATION) ───
class SalesForecastLLMResponse(BaseModel):
    summary: str = Field(description="Nhận xét dự báo của AI bằng tiếng Việt")
    insights: List[str] = Field(description="Các nhận xét chuyên sâu từ AI")


class InventoryLLMResponse(BaseModel):
    summary: str = Field(description="Tóm tắt phân tích tồn kho của AI")
    recommendations: List[str] = Field(description="Đề xuất tối ưu hóa tồn kho")


class ReorderItemLLM(BaseModel):
    productId: str = Field(description="Mã sản phẩm")
    notes: str = Field(description="Lý do nhập hàng ngắn gọn viết lại bằng tiếng Việt (tối đa 15 từ)")


class ReorderRecommendationLLMResponse(BaseModel):
    recommendations: List[ReorderItemLLM] = Field(description="Danh sách lý do đề xuất nhập hàng")



