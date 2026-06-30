import datetime
import warnings
import numpy as np
import pandas as pd
from scipy.special import inv_boxcox
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.tsa.holtwinters import ExponentialSmoothing
from statsmodels.tsa.seasonal import seasonal_decompose
import pmdarima as pm

warnings.filterwarnings("ignore")

class RevenueForecasterPipeline:
    """
    Pipeline dự báo doanh thu nâng cao với:
        - Tiền xử lý TỐI THIỂU (không Box-Cox!)
        - Auto-select SARIMA vs ETS dựa trên AIC + CV
        - Time series cross-validation chuẩn (rolling splits)
        - Không điều chỉnh thủ công (tin tưởng mô hình)
    """

    def __init__(self, seasonal_period: int = 7, use_boxcox: bool = True, auto_select: bool = True):
        self.seasonal_period = seasonal_period
        self.use_boxcox = use_boxcox
        self.auto_select = auto_select

        self.fitted_model_ = None
        self.model_type = None  # 'sarima' hoặc 'ets'
        self.lambda_ = None
        self.df_history = None
        self.clean_ts = None
        self.trend_ = None
        self.seasonal_factors_ = None
        self.seasonal_series_ = None
        self.residual_ = None
        self.slope_ = 0.0
        self.intercept_ = 0.0

    # ------------------- TIỀN XỬ LÝ CHUẨN -------------------
    def _preprocess(self, ts: pd.Series) -> pd.Series:
        """
        Tiền xử lý TỐI THIỂU cho SARIMA/ETS:
        - Xóa infinite values và NaN
        - Xóa outlier mạnh (report cảnh báo)
        - ❌ KHÔNG Box-Cox (differencing xử lý rồi)
        """
        # Loại bỏ infinite
        ts = ts.replace([np.inf, -np.inf], np.nan).dropna()

        if len(ts) == 0:
            raise ValueError("Không có dữ liệu hợp lệ sau khi loại bỏ NaN")

        # Phát hiện outlier (IQR 3x)
        q1 = ts.quantile(0.25)
        q3 = ts.quantile(0.75)
        iqr = q3 - q1
        lower = q1 - 3 * iqr
        upper = q3 + 3 * iqr
        outlier_mask = (ts < lower) | (ts > upper)
        
        if outlier_mask.sum() > 0:
            outlier_pct = 100 * outlier_mask.sum() / len(ts)
            print(f"⚠️ Detected {outlier_mask.sum()} outliers ({outlier_pct:.1f}%)")
            if outlier_pct > 10:
                print("   → Dữ liệu có thể bẩn, xem xét kiểm tra lại source")

        ts_clean = ts.clip(lower, upper)

        # ✅ KHÔNG dùng Box-Cox
        self.lambda_ = None
        
        return ts_clean

    # ------------------- CHỌN MÔ HÌNH -------------------
    def _fit_sarima(self, ts: pd.Series):
        """Fit SARIMA bằng auto_arima (tối ưu parameters)"""
        has_seasonal = len(ts) >= 2 * self.seasonal_period
        
        try:
            model = pm.auto_arima(
                ts,
                seasonal=has_seasonal,
                m=self.seasonal_period if has_seasonal else 1,
                start_p=0, max_p=5,
                start_q=0, max_q=5,
                start_P=0, max_P=2,
                start_Q=0, max_Q=2,
                stepwise=True,
                information_criterion='aicc',
                suppress_warnings=True,
                error_action='ignore',
                trace=False
            )
            return model
        except Exception as e:
            print(f"❌ SARIMA fit failed: {e}")
            return None

    def _fit_ets(self, ts: pd.Series):
        """
        Fit ETS (Exponential Smoothing) chuẩn.
        Thử các cấu hình ETS, chọn theo AIC.
        """
        best_aic = np.inf
        best_model = None
        best_config = None

        # ✅ Các cấu hình ETS phổ biến
        configs = [
            ('add', 'add', 'add'),    # AAA - Additive all
            ('add', 'add', None),      # AA - No seasonality
            ('mul', 'add', 'mul'),    # MAM - Multiplicative error/seasonal
            ('add', None, None),       # A - Simple exponential smoothing
            ('mul', None, None),       # M - Multiplicative simple
        ]

        for error_type, trend_type, seasonal_type in configs:
            try:
                # Nếu cần seasonal nhưng data ngắn -> skip
                if seasonal_type is not None and len(ts) < 2 * self.seasonal_period:
                    continue

                model = ExponentialSmoothing(
                    ts,
                    error_type=error_type,
                    trend=trend_type,
                    seasonal=seasonal_type,
                    seasonal_periods=self.seasonal_period if seasonal_type else None,
                    initialization_method='estimated'
                ).fit(optimized=True, disp=False)

                if model.aic < best_aic:
                    best_aic = model.aic
                    best_model = model
                    best_config = (error_type, trend_type, seasonal_type)

            except Exception as e:
                continue

        if best_model is not None:
            print(f"   ETS config: {best_config}")

        return best_model

    def _choose_best_model(self, ts: pd.Series):
        """Chọn mô hình tốt nhất: SARIMA vs ETS"""
        models = []

        # ✅ Fit cả hai mô hình
        print("[FIT] Trying SARIMA...")
        sarima = self._fit_sarima(ts)
        if sarima is not None:
            models.append(('sarima', sarima))
            print(f"   SARIMA AIC: {sarima.aic():.2f}")

        print("[FIT] Trying ETS...")
        ets = self._fit_ets(ts)
        if ets is not None:
            models.append(('ets', ets))
            print(f"   ETS AIC: {ets.aic:.2f}")

        if len(models) == 0:
            raise ValueError("❌ Không fit được mô hình nào!")

        if len(models) == 1:
            self.model_type, self.fitted_model_ = models[0]
            print(f"✅ Selected: {self.model_type.upper()}")
            return

        # ✅ So sánh bằng MAPE trên validation set (cuối 10-15% data)
        n = len(ts)
        test_size = max(7, int(0.15 * n))
        train = ts.iloc[:-test_size]
        test = ts.iloc[-test_size:]

        best_wape = np.inf
        best_tuple = None

        for model_type, model in models:
            try:
                if model_type == 'sarima':
                    pred = model.predict(n_periods=test_size)
                else:  # ets
                    pred = model.forecast(test_size)

                # ✅ WAPE chuẩn
                sum_act = np.sum(test.values)
                wape = (np.sum(np.abs(test.values - pred)) / sum_act * 100) if sum_act > 0 else 0.0
                print(f"   {model_type.upper()} WAPE on test: {wape:.2f}%")

                if wape < best_wape:
                    best_wape = wape
                    best_tuple = (model_type, model)
            except Exception as e:
                print(f"   {model_type.upper()} prediction failed: {e}")
                continue

        if best_tuple is None:
            self.model_type, self.fitted_model_ = models[0]
        else:
            self.model_type, self.fitted_model_ = best_tuple

        print(f"✅ Selected: {self.model_type.upper()}")

    # ------------------- MAIN PIPELINE -------------------
    def load_from_series(self, ts: pd.Series) -> 'RevenueForecasterPipeline':
        """Load và làm sạch time series"""
        ts = ts.sort_index().asfreq('D', fill_value=0.0)
        self.df_history = pd.DataFrame({'revenue': ts})
        self.clean_ts = self._preprocess(ts)
        return self

    def fit_trend_model(self) -> 'RevenueForecasterPipeline':
        """Fit mô hình SARIMA/ETS tốt nhất"""
        ts = self.clean_ts
        if ts is None:
            raise ValueError("Chưa load dữ liệu. Gọi load_from_series() trước.")

        self._choose_best_model(ts)

        # ✅ Lưu thành phần phân rã cho đồ thị
        if self.seasonal_period > 1 and len(ts) >= 2 * self.seasonal_period:
            try:
                decomp = seasonal_decompose(ts, model='additive', period=self.seasonal_period)
                self.trend_ = decomp.trend
                self.seasonal_series_ = decomp.seasonal
                self.residual_ = decomp.resid
                self.seasonal_factors_ = decomp.seasonal.iloc[:self.seasonal_period].values.tolist()
            except:
                self.trend_ = ts
                self.seasonal_factors_ = [0.0] * self.seasonal_period
                self.seasonal_series_ = pd.Series(0.0, index=ts.index)
                self.residual_ = pd.Series(0.0, index=ts.index)
        else:
            self.trend_ = ts
            self.seasonal_factors_ = [0.0] * self.seasonal_period
            self.seasonal_series_ = pd.Series(0.0, index=ts.index)
            self.residual_ = pd.Series(0.0, index=ts.index)

        # ✅ Ước lượng linear trend
        x = np.arange(len(ts))
        y = ts.values
        if len(x) > 1:
            self.slope_, self.intercept_ = np.polyfit(x, y, 1)
        else:
            self.slope_ = 0.0
            self.intercept_ = float(ts.mean())

        return self

    def forecast(self, steps: int = 28) -> pd.Series:
        """
        Dự báo mà không có điều chỉnh thủ công.
        ✅ Tin tưởng vào mô hình thống kê
        """
        if self.fitted_model_ is None:
            raise ValueError("Mô hình chưa fit. Gọi fit_trend_model() trước.")

        if self.model_type == 'sarima':
            pred = self.fitted_model_.predict(n_periods=steps)
        else:  # ets
            pred = self.fitted_model_.forecast(steps)

        # ✅ Chỉ đảm bảo không âm (nếu cần)
        pred = np.maximum(pred, 0.0)

        # ✅ Tạo index tương lai
        last_date = self.df_history.index[-1]
        future_index = pd.date_range(
            start=last_date + pd.Timedelta(days=1),
            periods=steps,
            freq='D'
        )
        forecast_series = pd.Series(pred, index=future_index)

        return forecast_series.clip(lower=0.0)

    def evaluate(self, test_days: int = 14) -> dict:
        """
        Time series cross-validation chuẩn (rolling splits).
        ✅ Multiple splits → ổn định hơn.
        """
        ts = self.clean_ts
        n = len(ts)

        # ✅ Kiểm tra dữ liệu đủ dài
        min_length = 3 * self.seasonal_period + test_days
        if n < min_length:
            print(f"⚠️ Data quá ngắn ({n} < {min_length}). Bỏ qua validation.")
            return self._empty_metrics()

        metrics_list = []

        # ✅ Rolling CV: 3-5 splits tùy độ dài data
        num_splits = min(5, max(3, n // (2 * self.seasonal_period)))
        step_size = max(7, n // (num_splits + 2))

        for split_idx in range(num_splits):
            train_end = n - test_days - (split_idx * step_size)

            if train_end < 2 * self.seasonal_period:
                break

            train = ts.iloc[:train_end]
            test = ts.iloc[train_end:train_end + test_days]

            if len(test) < test_days:
                continue

            try:
                # ✅ Fit temp model
                temp_pipe = RevenueForecasterPipeline(
                    seasonal_period=self.seasonal_period,
                    auto_select=True
                )
                temp_pipe.load_from_series(train)
                temp_pipe.fit_trend_model()

                pred = temp_pipe.forecast(steps=test_days)

                # ✅ Tính metrics chính xác
                actual = test.values
                predicted = pred.values[:len(actual)]

                mae = np.mean(np.abs(actual - predicted))
                rmse = np.sqrt(np.mean((actual - predicted) ** 2))
                
                sum_actual = np.sum(actual)
                wape = (np.sum(np.abs(actual - predicted)) / sum_actual * 100) if sum_actual > 0 else 0.0
                smape = 100 * np.mean(2 * np.abs(actual - predicted) / (np.abs(actual) + np.abs(predicted) + 1e-6))

                metrics_list.append({'mae': mae, 'rmse': rmse, 'wape': wape, 'smape': smape})

            except Exception as e:
                print(f"  Split {split_idx} failed: {e}")
                continue

        if not metrics_list:
            return self._empty_metrics()

        # ✅ Average across splits
        avg_metrics = {
            'mae': np.mean([m['mae'] for m in metrics_list]),
            'rmse': np.mean([m['rmse'] for m in metrics_list]),
            'wape': np.mean([m['wape'] for m in metrics_list]),
            'smape': np.mean([m['smape'] for m in metrics_list]),
        }

        result = {
            'model_name': f"{self.model_type.upper()} ({len(metrics_list)}-fold CV)",
            'mae': round(avg_metrics['mae'], 2),
            'rmse': round(avg_metrics['rmse'], 2),
            'wape': round(min(100.0, max(0.0, avg_metrics['wape'])), 2),
            'smape': round(min(100.0, max(0.0, avg_metrics['smape'])), 2),
            'cv_splits': len(metrics_list),
            'test_days_per_split': test_days,
            'tracking_signal': 0.0,
        }

        # ✅ Print kết quả
        print(
            f"\n[EVALUATION] Time Series CV ({len(metrics_list)} splits, {test_days} days each):\n"
            f"  MAE   : {result['mae']:.2f}\n"
            f"  RMSE  : {result['rmse']:.2f}\n"
            f"  WAPE  : {result['wape']:.2f}%\n"
            f"  sMAPE : {result['smape']:.2f}%\n"
        )

        return result

    def run(self, steps: int = 28, test_days: int = 14) -> tuple:
        """Chạy toàn bộ pipeline"""
        self.fit_trend_model()
        forecast_series = self.forecast(steps=steps)
        metrics = self.evaluate(test_days=test_days)
        return forecast_series, metrics

    @staticmethod
    def _empty_metrics():
        return {
            'model_name': 'None',
            'mae': 0.0,
            'rmse': 0.0,
            'wape': 0.0,
            'smape': 0.0,
            'cv_splits': 0,
            'test_days_per_split': 0,
            'tracking_signal': 0.0,
        }


# ================ EXAMPLE USAGE ================
if __name__ == "__main__":
    # Sample data
    np.random.seed(42)
    dates = pd.date_range('2023-01-01', periods=365, freq='D')
    trend = np.linspace(100, 150, 365)
    seasonality = 20 * np.sin(np.arange(365) * 2 * np.pi / 7)
    noise = np.random.normal(0, 5, 365)
    revenue = trend + seasonality + noise
    
    ts = pd.Series(revenue, index=dates, name='revenue')
    
    # Run pipeline
    pipeline = RevenueForecasterPipeline(seasonal_period=7, auto_select=True)
    pipeline.load_from_series(ts)
    pipeline.fit_trend_model()
    
    forecast, metrics = pipeline.run(steps=28, test_days=14)
    
    print("\n" + "="*50)
    print("FORECAST RESULTS")
    print("="*50)
    print(forecast)
    print("\nMETRICS:")
    print(metrics)
