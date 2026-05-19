import json
import os
import time
import urllib.parse
import urllib.request


class TelegramBot:
    def __init__(self, token: str) -> None:
        self.base_url = f"https://api.telegram.org/bot{token}"

    def _request(self, method: str, params: dict | None = None) -> dict:
        params = params or {}
        query = urllib.parse.urlencode(params)
        url = f"{self.base_url}/{method}"
        if query:
            url = f"{url}?{query}"

        with urllib.request.urlopen(url, timeout=60) as response:
            data = json.loads(response.read().decode("utf-8"))

        if not data.get("ok"):
            raise RuntimeError(f"Erro da API Telegram em {method}: {data}")
        return data["result"]

    def get_updates(self, offset: int | None = None, timeout: int = 30) -> list[dict]:
        params = {"timeout": timeout}
        if offset is not None:
            params["offset"] = offset
        return self._request("getUpdates", params)

    def send_message(self, chat_id: int, text: str) -> None:
        self._request("sendMessage", {"chat_id": chat_id, "text": text})


def reply_for_message(text: str) -> str:
    command = (text or "").strip().lower()
    if command in ("/start", "/help"):
        return (
            "Olá! Eu sou o bot do Diário de Turno.\n"
            "Comandos disponíveis:\n"
            "/start - iniciar\n"
            "/status - status do bot\n"
            "/turno - mensagem padrão de turno"
        )
    if command == "/status":
        return "✅ Bot online e funcionando."
    if command == "/turno":
        return "📋 Turno em andamento. Atualize o app para detalhes operacionais."
    return "Comando não reconhecido. Use /help."


def run() -> None:
    token = os.getenv("TELEGRAM_BOT_TOKEN")
    if not token:
        raise RuntimeError("Defina a variável de ambiente TELEGRAM_BOT_TOKEN.")

    bot = TelegramBot(token)
    offset = None
    print("Bot iniciado. Aguardando mensagens...")

    while True:
        try:
            updates = bot.get_updates(offset=offset, timeout=30)
            for update in updates:
                offset = update["update_id"] + 1
                message = update.get("message", {})
                chat = message.get("chat", {})
                chat_id = chat.get("id")
                text = message.get("text", "")
                if chat_id is None:
                    continue
                response = reply_for_message(text)
                bot.send_message(chat_id, response)
        except KeyboardInterrupt:
            print("\nBot finalizado.")
            break
        except Exception as error:
            print(f"Erro no loop do bot: {error}")
            time.sleep(3)


if __name__ == "__main__":
    run()
