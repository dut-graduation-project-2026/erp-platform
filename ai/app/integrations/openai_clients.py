import json
import re
import logging
from openai import AsyncOpenAI
from ..core.config import Config, config

logger = logging.getLogger("uvicorn.error")


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
            # Append explicit JSON schema instructions to guide the model
            schema_json = response_format.model_json_schema()
            json_instruction = (
                f"\n\nCRITICAL: You MUST respond with a JSON object matching this schema:\n"
                f"{json.dumps(schema_json, ensure_ascii=False, indent=2)}\n"
                f"Do NOT wrap the response in markdown blocks (e.g., do NOT use ```json ... ```). "
                f"Output ONLY the raw JSON string. Ensure all text fields in the JSON are in English."
            )
            
            # Find the last user message and append the instruction
            user_msg_found = False
            for msg in reversed(messages):
                if msg.get("role") == "user":
                    msg["content"] = msg.get("content", "") + json_instruction
                    user_msg_found = True
                    break
            
            if not user_msg_found:
                messages.append({"role": "user", "content": json_instruction})

            is_official_openai = "api.openai.com" in str(self.client.base_url).lower()

            # Try using completions.parse first if official OpenAI endpoint
            if is_official_openai:
                try:
                    kwargs["response_format"] = response_format
                    return await self.client.chat.completions.parse(**kwargs)
                except Exception as parse_err:
                    logger.warning(f"completions.parse failed: {parse_err}. Falling back to completions.create with manual parsing.")
            
            # Fallback: request as a json_object for non-official endpoints or when parse fails
            kwargs["response_format"] = {"type": "json_object"}
            res = await self.client.chat.completions.create(**kwargs)
            content = res.choices[0].message.content
            
            # Clean and parse content
            if content:
                content_clean = content.strip()
                if content_clean.startswith("```"):
                    content_clean = re.sub(r"^```(?:json)?\n", "", content_clean)
                    content_clean = re.sub(r"\n```$", "", content_clean)
                    content_clean = content_clean.strip()
                
                try:
                    parsed_data = json.loads(content_clean)
                    validated_model = response_format.model_validate(parsed_data)
                    
                    class MockMessage:
                        def __init__(self, parsed):
                            self.parsed = parsed
                            
                    class MockChoice:
                        def __init__(self, parsed):
                            self.message = MockMessage(parsed)
                            
                    class MockResponse:
                        def __init__(self, parsed):
                            self.choices = [MockChoice(parsed)]
                            
                    return MockResponse(validated_model)
                except Exception as val_err:
                    logger.error(f"Manual validation failed for content: {content}. Error: {val_err}")
                    raise val_err

        return await self.client.chat.completions.create(**kwargs)


openai_client = OpenAIClient(config)
