from dotenv import load_dotenv
import os


class Config:
    def __init__(self):
        load_dotenv()
        self.OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
        self.OPENAI_MODEL = os.getenv("OPENAI_MODEL")
        self.OPENAI_URL = os.getenv("OPENAI_URL")

        self.BACKEND_URL = os.getenv("BACKEND_URL")
        self.RETRY_INTERVAL = int(os.getenv("RETRY_INTERVAL", "5"))

config = Config()
