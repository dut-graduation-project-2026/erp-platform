from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .core.config import config
from .routers.default_routes import router
from .integrations.erp_clients import erp_client

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Connecting to ERP backend...")
    try:
        await erp_client.authenticate()
        logger.info("Connected to ERP backend successfully.")
    except Exception as e:
        logger.warning(f"Could not connect/authenticate with ERP backend on startup: {e}. Will retry on demand.")
    yield
    logger.info("Closing ERP client connection...")
    await erp_client.client.aclose()
    logger.info("ERP client connection closed.")


app = FastAPI(
    title="ERP AI API",
    version="1.0.0",
    lifespan=lifespan,
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

