# TÀI LIỆU LÝ THUYẾT & THIẾT KẾ HỆ THỐNG DỰ BÁO DOANH THU (SARIMA ENGINE)

Tài liệu này trình bày chi tiết về mặt toán học, cấu trúc dữ liệu và phương pháp kiểm thử của module dự báo doanh số bán hàng trong hệ thống ERP Platform, phục vụ báo cáo Đồ án Tốt nghiệp.

---

## 1. KHÁI QUÁT BÀI TOÁN DỰ BÁO DOANH THU TRONG ERP
Trong một hệ thống ERP, việc dự báo doanh thu không chỉ đơn thuần là đưa ra một con số ước lượng cho tương lai, mà là cơ sở để đưa ra các quyết định vận hành cốt lõi:
*   **Hoạch định nhu cầu nguyên vật liệu (MRP):** Biết trước lượng hàng bán ra để chuẩn bị mua sắm.
*   **Quản lý chuỗi cung ứng (SCM):** Tránh hiện tượng "Hiệu ứng chiếc roi da" (Bullwhip Effect).
*   **Quản trị dòng tiền (Cashflow Management):** Ước tính dòng doanh thu sẽ thu về trong tháng tới.

---

## 2. QUY TRÌNH TIỀN XỬ LÝ CHUỖI THỜI GIAN (DATA PREPROCESSING)

### 2.1. Gom cụm theo tuần (Weekly Aggregation)
Dữ liệu giao dịch bán hàng thực tế (Invoices/Orders) được lưu trữ theo từng giây/phút trong cơ sở dữ liệu. Trước khi đưa vào mô hình SARIMA, chuỗi doanh thu được gom cụm (resample) theo tần suất tuần (`W-MON` - Thứ Hai hàng tuần):
*   **Lý do khoa học:** Doanh số hàng ngày thường chứa độ nhiễu cực kỳ lớn (ngày cuối tuần tăng vọt, ngày thường giảm sâu) và tỷ lệ ngày có doanh số bằng 0 (Zero-Demand) cao. Gom cụm theo tuần giúp làm mịn bớt nhiễu ngẫu nhiên ngắn hạn và làm lộ rõ các chu kỳ tiêu dùng định kỳ của khách hàng.

### 2.2. Kiểm tra điều kiện tối thiểu của Chuỗi thời gian
Trước khi chạy mô hình, hệ thống kiểm tra các điều kiện để đảm bảo chuỗi dữ liệu có thể dự báo được:
*   **Độ dài tối thiểu:** Chuỗi phải có tối thiểu 12 tuần dữ liệu lịch sử.
*   **Độ biến động (Standard Deviation):** Độ lệch chuẩn của chuỗi phải khác 0 (tránh trường hợp doanh thu tất cả các tuần đều bằng một hằng số phẳng).

---

## 3. MÔ HÌNH TOÁN HỌC SARIMA (SEASONAL ARIMA)

Mô hình được sử dụng là **$\text{SARIMA}(p, d, q) \times (P, D, Q)_s$**, mở mở rộng của mô hình ARIMA truyền thống nhằm tích hợp thêm yếu tố mùa vụ (chu kỳ).

### 3.1. Phương trình tổng quát
Phương trình toán học tổng quát của mô hình $\text{SARIMA}$ được biểu diễn thông qua toán tử trễ (Lag Operator $B$, với $B^k X_t = X_{t-k}$):

$$\Phi_P(B^s) \phi_p(B) (1 - B)^d (1 - B^s)^D Y_t = \Theta_Q(B^s) \theta_q(B) \epsilon_t$$

Trong đó:
*   **Y_t:** Doanh thu thực tế tại tuần $t$.
*   **\epsilon_t:** Sai số ngẫu nhiên (nhiễu trắng - White Noise) tại tuần $t$, tuân theo phân phối chuẩn $N(0, \sigma^2)$.
*   **Toán tử tự hồi quy phi mùa vụ (Non-seasonal AR polynomial):**
    $$\phi_p(B) = 1 - \phi_1 B - \phi_2 B^2 - \dots - \phi_p B^p$$
*   **Toán tử trung bình trượt phi mùa vụ (Non-seasonal MA polynomial):**
    $$\theta_q(B) = 1 + \theta_1 B + \theta_2 B^2 + \dots + \theta_q B^q$$
*   **Toán tử sai phân phi mùa vụ:** $(1 - B)^d$ với $d$ là bậc sai phân để chuyển chuỗi không dừng thành chuỗi dừng.
*   **Toán tử mùa vụ (Seasonal components):**
    *   $\Phi_P(B^s) = 1 - \Phi_1 B^s - \dots - \Phi_P B^{Ps}$ (Seasonal AR)
    *   $\Theta_Q(B^s) = 1 + \Theta_1 B^s + \dots + \Theta_Q B^{Qs}$ (Seasonal MA)
    *   $(1 - B^s)^D$ (Seasonal Differencing) với $D$ là bậc sai phân mùa vụ.
*   **s (Seasonal Period):** Chu kỳ mùa vụ. Trong hệ thống này, ta đặt $s = 4$ (tương đương chu kỳ lặp lại theo tháng - 4 tuần).

### 3.2. Thuật toán tự động tối ưu tham số (Auto-ARIMA)
Thay vì chọn thủ công các tham số $(p,d,q)$ và $(P,D,Q)$, hệ thống sử dụng thuật toán **Auto-ARIMA** (dựa trên tiêu chuẩn AIC - Akaike Information Criterion):
$$\text{AIC} = 2k - 2\ln(\hat{L})$$
*   $k$: Số lượng tham số của mô hình.
*   $\hat{L}$: Hàm hợp lý cực đại (Likelihood function).
*   **Nguyên tắc:** Thuật toán duyệt qua nhiều bộ tham số và chọn bộ tham số có **AIC nhỏ nhất** (cân bằng tốt nhất giữa độ chính xác và tính đơn giản của mô hình, tránh Overfitting).

---

## 4. QUY TRÌNH HUẤN LUYỆN & KIỂM ĐỊNH TỊNH TIẾN (WALK-FORWARD VALIDATION)

Để đánh giá chất lượng mô hình một cách khách quan nhất, hệ thống không áp dụng cách chia Train/Test tĩnh mà sử dụng phương pháp **Walk-Forward Validation** (Expanding Window):

```text
Giả sử có 25 tuần dữ liệu lịch sử. Chọn val_size = 4 tuần để làm tập Test cuộn.

Vòng 1 (Dự báo tuần 22):
- Train: Tuần 1 -> Tuần 21.
- Predict: Tuần 22. So sánh thực tế -> Tính Sai số 1.

Vòng 2 (Dự báo tuần 23):
- Train: Tuần 1 -> Tuần 22 (mở rộng thêm dữ liệu thực tế tuần 22).
- Predict: Tuần 23. So sánh thực tế -> Tính Sai số 2.

Vòng 3 (Dự báo tuần 24):
- Train: Tuần 1 -> Tuần 23.
- Predict: Tuần 24. So sánh thực tế -> Tính Sai số 3.

Vòng 4 (Dự báo tuần 25):
- Train: Tuần 1 -> Tuần 24.
- Predict: Tuần 25. So sánh thực tế -> Tính Sai số 4.
```

### Cách tính các chỉ số sai số (Metrics)
Sau khi hoàn thành 4 vòng cuộn, mô hình tổng hợp các sai số thành hai chỉ số cốt lõi:
1.  **MAE (Mean Absolute Error):**
    $$\text{MAE} = \frac{1}{N} \sum_{i=1}^{N} |Actual_i - Predicted_i|$$
2.  **MAPE (Mean Absolute Percentage Error):**
    $$\text{MAPE} = \frac{1}{N} \sum_{i=1}^{N} \left| \frac{Actual_i - Predicted_i}{Actual_i} \right| \times 100\%$$

---

## 5. CÁC CHỈ SỐ KIỂM ĐỊNH THỐNG KÊ (MODEL VALIDATION)

Một mô hình được coi là đạt tiêu chuẩn khoa học để đưa vào sử dụng khi thỏa mãn các kiểm định sau:

### 5.1. Kiểm định Ljung-Box (Kiểm định độc lập của phần dư)
*   **Mục đích:** Kiểm tra xem phần dư (Residuals = Thực tế - Dự báo) có thực sự là nhiễu trắng ngẫu nhiên không, hay vẫn còn sót lại quy luật chưa được học.
*   **Giả thuyết:**
    *   $H_0$: Các phần dư là độc lập (không có mối quan hệ tự tương quan - White Noise).
    *   $H_1$: Các phần dư không độc lập (vẫn còn quy luật chưa học hết).
*   **Quy tắc quyết định:** Nếu **$p$-value $> 0.05$**, ta **chấp nhận giả thuyết $H_0$**. Điều này có nghĩa phần dư hoàn toàn là ngẫu nhiên, mô hình đã khai thác tối đa quy luật của dữ liệu.

---

## 6. Ý NGHĨA KHI BIỂU DIỄN TRÊN GIAO DIỆN ĐỒ THỊ
*   **Đường màu xanh (Actual):** Thể hiện doanh số bán hàng biến động mạnh hàng tuần.
*   **Đường màu cam (Fitted + Forecast):** 
    *   *Phần lịch sử:* Là giá trị kỳ vọng toán học mà mô hình học được. Do triệt tiêu nhiễu ngẫu nhiên $\epsilon_t$, đường cam sẽ **mượt hơn** và đi qua trục trung bình của đường xanh.
    *   *Phần tương lai:* Kéo dài sau điểm dữ liệu thực tế cuối cùng, uốn lượn uốn khúc thể hiện xu hướng tăng trưởng và tính chu kỳ lặp lại đã được dự đoán cho 4 tuần tới.
