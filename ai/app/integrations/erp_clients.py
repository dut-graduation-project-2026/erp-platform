import httpx
from ..core.config import config, Config


class ERPClient:
    def __init__(self, config: Config):
        self.api_url = f"{config.BACKEND_URL}/api/v1"
        self.admin_user = config.ADMIN_USERNAME
        self.admin_pass = config.ADMIN_PASSWORD

        self.client = httpx.AsyncClient(timeout=30.0)
        self._authenticated = False  # Flag đánh dấu đã login chưa

    async def authenticate(self):
        if self._authenticated:
            return  # Đăng nhập rồi, bỏ qua

        response = await self.client.post(
            f"{self.api_url}/auth/login",
            json={
                "email": self.admin_user,
                "password": self.admin_pass,
            },
        )

        if response.status_code == 200:
            self._authenticated = True
        else:
            raise Exception(f"Authentication failed: {response.text}")

    async def request(self, method: str, path: str, **kwargs):
        await self.authenticate()
        response = await self.client.request(method, f"{self.api_url}{path}", **kwargs)

        if response.status_code == 401:
            self._authenticated = False
            await self.authenticate()
            response = await self.client.request(
                method, f"{self.api_url}{path}", **kwargs
            )

        return response

    async def get(self, path: str, **kwargs):
        return await self.request("GET", path, **kwargs)

    async def post(self, path: str, **kwargs):
        return await self.request("POST", path, **kwargs)

    async def put(self, path: str, **kwargs):
        return await self.request("PUT", path, **kwargs)

    async def delete(self, path: str, **kwargs):
        return await self.request("DELETE", path, **kwargs)


erp_client = ERPClient(config)
