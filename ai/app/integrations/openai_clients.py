from openai import AsyncOpenAI
from ..core.config import Config, config


class OpenAIClient:
    def __init__(self, config: Config):
        self.model = config.OPENAI_MODEL
        self.client = AsyncOpenAI(
            api_key=config.OPENAI_API_KEY,
            base_url=config.OPENAI_URL,
        )

    async def chat(
        self,
        messages: list,
        tools: list | None = None,
        response_format: type | None = None,
    ):
        kwargs = {
            "model": self.model,
            "messages": messages,
        }

        if tools:
            kwargs["tools"] = tools
            kwargs["tool_choice"] = "auto"

        if response_format:
            kwargs["response_format"] = response_format
            return await self.client.chat.completions.parse(**kwargs)

        return await self.client.chat.completions.create(**kwargs)


openai_client = OpenAIClient(config)
