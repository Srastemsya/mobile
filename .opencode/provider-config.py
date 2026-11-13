import json
import os
import re
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, urlopen

base = os.environ["COMPAT_API_BASE"].rstrip("/")
selected = os.environ.get("COMPAT_API_MODEL")
models = {}
if urlsplit(base).hostname == "gptunnel.ru":
    if base != "https://gptunnel.ru/v1":
        raise SystemExit("Для GPTunneL укажите https://gptunnel.ru/v1.")
    request = Request(base + "/models", headers={
        "Authorization": os.environ["COMPAT_API_KEY"].strip(),
    })
    try:
        with urlopen(request, timeout=15) as response:
            catalog = json.load(response)["data"]
    except HTTPError as error:
        raise SystemExit(f"GPTunneL /models: HTTP {error.code}. Проверьте ключ.")
    except (URLError, TimeoutError, ValueError, KeyError):
        raise SystemExit("Не удалось загрузить каталог GPTunneL.")
    for item in catalog:
        model_id = item["id"]
        if item.get("deprecated") or re.search(r"embed|moderation", model_id):
            continue
        models[model_id] = {"name": item.get("title") or model_id}
else:
    if not selected:
        raise SystemExit("Для этого API задайте COMPAT_API_MODEL.")
    models[selected] = {"name": selected}

if not models or (selected and selected not in models):
    raise SystemExit("Запрошенная модель отсутствует в доступном каталоге.")

config = {
    "model": "compatible/" + (selected or next(
        (model for model in ("qwen3.8-max",) if model in models),
        next(iter(models)),
    )),
    "providers": {"compatible": {
        "name": "GPTunneL" if base == "https://gptunnel.ru/v1" else "Custom OpenAI-compatible",
        "env": ["COMPAT_API_KEY"],
        "package": "@opencode/ai/providers/openai-compatible",
        "settings": {"baseURL": base},
        "models": models,
    }},
    "agents": {},
}
for name, model, description in (
    ("quick-check", "qwen3.8-max", "Короткий поиск и проверка в рамках обычной задачи"),
    ("deep-review", "qwen3.8-max", "Подробное ревью обычной задачи и анализ сложных изменений"),
):
    if model in models:
        config["agents"][name] = {
            "mode": "subagent",
            "model": f"compatible/{model}",
            "description": description,
            "system": "Выполни порученную проверку и сообщи факты с путями. Не изменяй файлы. Не используй эту роль вместо независимых ролей очереди спецификаций.",
            "permissions": [{"action": action, "resource": "*", "effect": "deny"}
                            for action in ("edit", "shell")],
        }
print(json.dumps(config))
