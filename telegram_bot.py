import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request


class TelegramBot:
    def __init__(self, token: str, polling_timeout: int = 30, retry_delay: int = 3):
        self.token = token
        self.base_url = f"https://api.telegram.org/bot{token}"
        self.offset = 0
        self.polling_timeout = polling_timeout
        self.retry_delay = retry_delay

    def _api_request(self, method: str, payload: dict) -> dict:
        encoded_payload = urllib.parse.urlencode(payload).encode("utf-8")
        request = urllib.request.Request(
            url=f"{self.base_url}/{method}",
            data=encoded_payload,
            method="POST",
        )

        with urllib.request.urlopen(request, timeout=self.polling_timeout + 10) as response:
            response_data = json.loads(response.read().decode("utf-8"))
            if not response_data.get("ok"):
                raise RuntimeError(f"Erro da API Telegram: {response_data}")
            return response_data

    def get_updates(self) -> list[dict]:
        payload = {"timeout": self.polling_timeout, "offset": self.offset}
        response_data = self._api_request("getUpdates", payload)
        return response_data.get("result", [])

    def send_message(self, chat_id: int, text: str) -> None:
        self._api_request("sendMessage", {"chat_id": chat_id, "text": text})

    def handle_update(self, update: dict) -> None:
        message = update.get("message") or {}
        chat = message.get("chat") or {}
        chat_id = chat.get("id")
        text = (message.get("text") or "").strip()

        if not chat_id or not text:
            return

        if text in ("/start", "/help"):
            self.send_message(
                chat_id,
                "Olá! Eu sou seu bot.\n"
                "Comandos disponíveis:\n"
                "/start - iniciar conversa\n"
                "/help - ajuda\n"
                "/ping - testar conexão",
            )
            return

        if text == "/ping":
            self.send_message(chat_id, "pong")
            return

        self.send_message(chat_id, f"Você disse: {text}")

    def run(self) -> None:
        print("Bot iniciado. Aguardando mensagens...")
        while True:
            try:
                updates = self.get_updates()
                for update in updates:
                    self.offset = update["update_id"] + 1
                    self.handle_update(update)
            except urllib.error.URLError as error:
                print(f"Erro de rede: {error}. Tentando novamente em {self.retry_delay}s...")
                time.sleep(self.retry_delay)
            except Exception as error:
                print(f"Erro inesperado: {error}. Tentando novamente em {self.retry_delay}s...")
                time.sleep(self.retry_delay)


def main() -> None:
    token = os.getenv("TELEGRAM_BOT_TOKEN")
    if not token:
        raise RuntimeError("Defina a variável de ambiente TELEGRAM_BOT_TOKEN.")

    bot = TelegramBot(token=token)
    bot.run()


if __name__ == "__main__":
    main()
