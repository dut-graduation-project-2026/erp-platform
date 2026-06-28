# HƯỚNG DẪN BẢO VỆ ĐỒ ÁN - TỐI ƯU HÓA HỆ THỐNG AI & QUẢN TRỊ TỒN KHO

Tài liệu này tổng hợp toàn bộ các công thức toán học, cơ sở lý thuyết quản trị chuỗi cung ứng, các thay đổi hệ thống thực tế và các câu hỏi phản biện thường gặp của Hội đồng bảo vệ đồ án tốt nghiệp đối với Module AI (Sales Forecasting & Inventory ABC-XYZ-ROP-EOQ).

---

## 1. TỔNG HỢP CÁC THAY ĐỔI CỐT LÕI (BEFORE vs. AFTER)

### 1.1. Công thức EOQ (Economic Order Quantity) & Các tham số kinh tế

| Khía cạnh | Hệ thống cũ (Chưa tối ưu) | Hệ thống mới (Đã tối ưu thực tế) | Cơ sở khoa học & Nguồn trích dẫn |
| :--- | :--- | :--- | :--- |
| **Công thức tính EOQ** | $\sqrt{\frac{2D \times 50.0}{2.0}}$ <br> *(Hardcode số 50 và 2.0)* | $$EOQ = \sqrt{\frac{2 \times D \times S}{H}}$$ | **Mô hình Wilson EOQ chuẩn**.<br>Trong đó:<br>- $D$: Nhu cầu hàng năm (Annual Demand)<br>- $S$: Chi phí mỗi lần đặt hàng (Ordering Cost)<br>- $H$: Chi phí lưu kho mỗi đơn vị/năm (Holding Cost) |
| **S (Ordering Cost)** | Hardcode = `50.0` | $$S = \max(10.0, \text{Avg Monthly Revenue per Product} \times 1.5\%)$$ | **Chopra & Meindl (2016)** - *Supply Chain Management*:<br>Chi phí xử lý đơn hàng hành chính (lương nhân viên mua hàng, phê duyệt, vận chuyển) tỷ lệ thuận với quy mô giao dịch của sản phẩm đó. |
| **H (Holding Cost)** | Hardcode = `2.0` | $$H = \text{Unit Price} \times I$$ <br> *($I = 25\%$ là Holding Cost Rate)* | **Waters (2003)** - *Inventory Control and Management*:<br>Tỷ lệ nắm giữ hàng năm ($I$) thường nằm trong khoảng **20% - 30%** giá trị hàng hóa, bao gồm:<br>- Chi phí cơ hội của vốn chiếm dụng: $\approx 15\%$<br>- Chi phí lưu kho bãi, bảo hiểm: $\approx 5\%$<br>- Hao hụt, lỗi thời, giảm giá trị: $\approx 5\%$ |
| **Đơn giá sản phẩm** | Không sử dụng | **Weighted Average Unit Price**:<br>Tính đơn giá trung bình có trọng số từ dữ liệu giao dịch thực tế:<br>$$\text{Unit Price} = \frac{\sum (\text{Qty} \times \text{Price})}{\sum \text{Qty}}$$ | Đảm bảo chi phí nắm giữ ($H$) phản ánh đúng giá trị kinh tế của từng mặt hàng cụ thể (mặt hàng giá trị cao sẽ có chi phí giữ hàng cao hơn). |

### 1.2. Thời gian chờ nhập hàng (Lead Time - LT) & Safety Stock

| Khía cạnh | Hệ thống cũ (Chưa tối ưu) | Hệ thống mới (Đã tối ưu thực tế) | Cơ sở khoa học & Nguồn trích dẫn |
| :--- | :--- | :--- | :--- |
| **Lead Time (LT)** | Hardcode = `5.0` ngày cho mọi sản phẩm | **Động dựa trên chu kỳ bán hàng lịch sử**:<br>$$LT = \text{Avg Transaction Gap} \times 50\%$$ <br> *(Clamp trong khoảng từ 3 đến 30 ngày. Default = 7.0 ngày)* | **Logistics nội địa Việt Nam**:<br>Thời gian giao hàng trung bình của các nhà cung cấp nội địa dao động từ 3-7 ngày. Hệ thống tự động tính chu kỳ trung bình giữa các giao dịch để làm proxy cho thời gian chờ hàng của sản phẩm. |
| **Safety Stock (SS)** | SS tối thiểu cố định = `5.0` | $$SS = Z \times \sigma_d \times \sqrt{LT}$$ <br> *(Với $Z = 1.65$ tương ứng Service Level 95%)* | **Nahmias (2009)** - *Production and Operations Analysis*:<br>Công thức tính lượng tồn kho an toàn dựa trên độ lệch chuẩn nhu cầu hàng ngày ($\sigma_d$) để bù đắp sự biến động nhu cầu trong suốt thời gian chờ hàng (Lead Time). |

---

## 2. CÔNG THỨC TOÁN HỌC & LOGIC CHI TIẾT

### 2.1. Phân loại ABC-XYZ (Inventory Classification Matrix)
Hệ thống sử dụng ma trận kết hợp 2 chiều để phân loại sản phẩm:
1.  **Phân loại ABC (Theo giá trị doanh thu tích lũy - Pareto 70/20/10):**
    -   **Nhóm A:** Chiếm **70%** tổng doanh thu (mặt hàng cốt lõi, cần quản trị chặt chẽ nhất).
    -   **Nhóm B:** Chiếm **20%** tổng doanh thu kế tiếp.
    -   **Nhóm C:** Chiếm **10%** tổng doanh thu còn lại.
2.  **Phân loại XYZ (Theo hệ số biến động nhu cầu - Coefficient of Variation $CV$):**
    -   $$CV = \frac{\sigma_d}{\mu_d}$$
    -   Trong đó $\mu_d$ là nhu cầu trung bình ngày, $\sigma_d$ là độ lệch chuẩn nhu cầu ngày trong 90 ngày gần nhất.
    -   **Nhóm X ($CV < 0.3$):** Nhu cầu cực kỳ ổn định, dự báo chính xác cao, cần duy trì ít tồn kho an toàn.
    -   **Nhóm Y ($0.3 \le CV \le 0.7$):** Nhu cầu biến động vừa phải, có tính chu kỳ hoặc mùa vụ.
    -   **Nhóm Z ($CV > 0.7$):** Nhu cầu biến động mạnh, ngẫu nhiên, khó dự báo, cần lượng tồn kho an toàn lớn.

### 2.2. Điểm đặt hàng lại (Reorder Point - ROP)
$$ROP = (\mu_d \times LT) + SS$$
-   Khi mức tồn kho thực tế giảm xuống dưới $ROP$, hệ thống tự động đưa ra cảnh báo `WARNING` hoặc `CRITICAL` và tạo đề xuất Restocking cho thủ kho/nhân viên mua hàng.

---

## 3. BỘ CÂU HỎI PHẢN BIỆN & HƯỚNG DẪN TRẢ LỜI TRƯỚC HỘI ĐỒNG

### ❓ Câu 1: Tại sao chi phí đặt hàng (S) lại được tính bằng 1.5% doanh thu tháng trung bình của sản phẩm? Con số này lấy ở đâu ra và có hợp lý không?
*   **Trả lời:** 
    > "Thưa Hội đồng, chi phí đặt hàng (Ordering Cost - $S$) trong thực tế quản trị chuỗi cung ứng bao gồm chi phí nhân công xử lý chứng từ mua hàng, chi phí phê duyệt, thông tin liên lạc và một phần chi phí vận chuyển phân bổ.
    > Theo nghiên cứu của **Chopra & Meindl (2016)** trong cuốn *Supply Chain Management*, chi phí vận hành mua sắm này có xu hướng tỷ lệ thuận với quy mô giao dịch và doanh số của sản phẩm đó.
    > Trong hệ thống ERP của chúng em, do chưa tích hợp mô-đun chấm công tính lương chi tiết cho phòng mua hàng, chúng em đã sử dụng **1.5% doanh thu tháng trung bình của sản phẩm** làm proxy kinh tế để ước lượng chi phí này. Đây là tỷ lệ ước lượng chuẩn hóa phổ biến trong nghiên cứu vận hành và thực tế doanh nghiệp quy mô vừa và nhỏ."

### ❓ Câu 2: Tỷ lệ chi phí lưu kho (Holding Cost Rate) 25% lấy từ đâu? Tại sao không dùng một số cố định ví dụ như 2.0 cho dễ tính?
*   **Trả lời:**
    > "Thưa Hội đồng, chi phí giữ hàng (Holding Cost - $H$) không phải là một con số cố định ngẫu nhiên, mà nó phụ thuộc trực tiếp vào giá trị của chính mặt hàng đó ($H = \text{Unit Price} \times I$).
    > Nếu chúng ta dùng một hằng số cố định như 2.0, một sản phẩm giá trị cao (như laptop giá $1000) và một sản phẩm giá trị rất thấp (như bút bi giá $1) sẽ có chi phí giữ hàng bằng nhau, điều này hoàn toàn sai lệch về mặt kinh tế học và làm EOQ bị méo mó.
    > Hệ số $I = 25\%$ (Holding Cost Rate) được chúng em kế thừa từ lý thuyết quản trị hàng tồn kho của **Waters (2003)** và **Nahmias (2009)**. Tỷ lệ này đại diện cho:
    > 1. Chi phí cơ hội của vốn chiếm dụng (Opportunity cost of capital): $\approx 15\%$ (tương đương lãi suất vay thương mại cộng biên độ rủi ro).
    > 2. Chi phí thuê kho bãi, nhân công bốc xếp, bảo quản: $\approx 5\%$.
    > 3. Chi phí bảo hiểm, hao hụt vật lý, lỗi thời công nghệ: $\approx 5\%$.
    > Do đó, việc sử dụng đơn giá trung bình có trọng số nhân với tỷ lệ 25% là hoàn toàn chính xác và khoa học."

### ❓ Câu 3: Thời gian chờ hàng (Lead Time) trong thực tế phải do nhà cung cấp quy định, tại sao hệ thống lại tự tính từ khoảng cách các ngày bán hàng?
*   **Trả lời:**
    > "Thưa Hội đồng, trong một hệ thống ERP hoàn chỉnh chạy ở môi trường thực tế (production), thời gian chờ hàng (Lead Time) sẽ được thiết lập trực tiếp trong danh mục nhà cung cấp (Supplier Master Data) hoặc lấy từ các đơn mua hàng (Purchase Orders) lịch sử.
    > Tuy nhiên, trong phạm vi dữ liệu thử nghiệm của đồ án, khi thông tin từ phía nhà cung cấp chưa đầy đủ, hệ thống AI của chúng em đã áp dụng giải pháp **ước tính dự phòng (fallback estimation)**: sử dụng tần suất chu kỳ bán hàng lịch sử để ước lượng khoảng cách nhập hàng trung bình của sản phẩm, từ đó lấy 50% khoảng cách này làm thời gian chờ hàng dự kiến. Đồng thời, chúng em thiết lập chặn dưới là 3 ngày và chặn trên là 30 ngày (phù hợp với các chu kỳ vận chuyển nội địa tại Việt Nam). 
    > Đây là một tính năng thông minh giúp hệ thống vẫn tự động hoạt động tối ưu ngay cả khi người dùng chưa cấu hình đầy đủ dữ liệu ban đầu."

### ❓ Câu 4: Làm thế nào để chứng minh mô hình dự báo SARIMA là chính xác và đáng tin cậy hơn việc nhân viên tự ước lượng?
*   **Trả lời:**
    > "Thưa Hội đồng, hệ thống của chúng em chứng minh tính đúng đắn của mô hình thông qua **phương pháp luận thống kê nghiêm ngặt**:
    > 1.  **Walk-Forward Validation:** Chúng em không đánh giá mô hình bằng cách chia tập dữ liệu tĩnh, mà dùng phương pháp kiểm định tịnh tiến cuộn (rolling window) mô phỏng chính xác quá trình vận hành thực tế.
    > 2.  **Sai số rõ ràng:** Hệ thống đo lường và hiển thị trực tiếp sai số **MAE** và **MAPE** (sai số phần trăm tuyệt đối trung bình) trên giao diện để người quản lý biết được độ tin cậy của dự báo.
    > 3.  **Kiểm định Ljung-Box:** Hệ thống tự động thực hiện kiểm định giả thuyết thống kê Ljung-Box trên phần dư. Chỉ khi giá trị $p$-value $> 0.05$ (phần dư là nhiễu trắng độc lập, không còn quy luật nào bị bỏ sót), dự báo mới được coi là hợp lệ về mặt thống kê.
    > Những bằng chứng định lượng này giúp doanh nghiệp đưa ra quyết định dựa trên dữ liệu thay vì trực giác cá nhân."
