# DiarioTurnoPlacas

Aplicativo Android (Kotlin + Compose + Room + DataStore) para diario de turno do patio de placas.

## Requisitos
- Windows 10/11
- Android Studio (versao recente)
- JDK 17

### Instalar JDK 17 (PowerShell)
```powershell
winget install -e --id EclipseAdoptium.Temurin.17.JDK
```

### Confirmar Java
```powershell
java -version
```

## Abrir e executar
1. Abra o Android Studio.
2. `File > Open` e selecione esta pasta.
3. Aguarde o Gradle Sync.
4. Rode no emulador/dispositivo com o botao Run.

## Estrutura
- `app/src/main/java/com/seuapp/diarioturnoplacas/data` Room, repositorio, importador Excel, seed
- `app/src/main/java/com/seuapp/diarioturnoplacas/domain` modelos e calculo capacidade x demanda
- `app/src/main/java/com/seuapp/diarioturnoplacas/ui` telas, componentes, tema
- `app/src/main/java/com/seuapp/diarioturnoplacas/navigation` abas
- `app/src/main/java/com/seuapp/diarioturnoplacas/di` ServiceLocator

## Abas
- Turno
- Programacao
- Recursos
- Capacidade
- Diario

## Regra de calculo de tempo por tarefa
- `modo_calculo = MULTIPLICAR` -> `tempoTotal = tempoBaseMin * frequencia * quantidade`
- `modo_calculo = DIVIDIR` -> `tempoTotal = (tempoBaseMin / frequencia) * quantidade`
- sem `modo_calculo`: heuristica (frequencia > 1 e tempoBase <= 240 => MULTIPLICAR; senao DIVIDIR)

## Colunas esperadas no Excel
Lidas por cabecalho da primeira planilha:
- `categoria`
- `descricao`
- `quantidade`
- `frequencia`
- `tempoBaseMin` (tambem aceita `tempo` ou `tempo_base_min`)
- `modo_calculo` (opcional)
- `recursoSugestao` (opcional)

## Observacoes
- O app inicializa com dados seed para demonstracao.
- Importacao Excel fica na aba Programacao.
- Gauge e banner mostram uso/acima da capacidade e causa (efetivo incompleto vs ineficiencia/demanda).

## Bot Telegram (Python)
Foi adicionado o script `telegram_bot.py` com um bot simples via long polling.

### Configuracao
1. Crie um bot com o `@BotFather` e obtenha o token.
2. Defina a variavel de ambiente:
   - PowerShell: `$env:TELEGRAM_BOT_TOKEN="SEU_TOKEN"`
   - Bash: `export TELEGRAM_BOT_TOKEN="SEU_TOKEN"`

### Execucao
```bash
python telegram_bot.py
```

Comandos disponiveis no bot:
- `/start` e `/help`
- `/status`
- `/turno`
