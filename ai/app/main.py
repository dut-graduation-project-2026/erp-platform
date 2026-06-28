import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .core.config import config
from .routers.default_routes import router

logger = logging.getLogger("uvicorn.error")

app = FastAPI(
    title="ERP AI API",
    version="1.0.0"
)

# CORS configuration
origins = []
if config.BACKEND_URL:
    backend_url = config.BACKEND_URL
    if not backend_url.startswith(("http://", "https://")):
        origins.extend([f"http://{backend_url}", f"https://{backend_url}"])
    else:
        origins.append(backend_url)

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins if origins else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router, prefix="/api/v1", tags=["v1"])
