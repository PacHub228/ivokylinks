# TGLink (ivokylinks)

Плагин для Minecraft-серверов на Spigot/Paper, который связывает игровой аккаунт с Telegram: привязка по одноразовому коду, награда за первую привязку, блокировка/разблокировка доступа и защита входа при смене IP-адреса — всё управляется через Telegram-бота.

## Возможности

- **Привязка аккаунта** — игрок вводит `/link` в игре, получает 6-значный код (действует 10 минут, одноразовый) и отправляет его боту в Telegram.
- **Награда за первую привязку** — настраиваемое количество алмазов и опыта; если инвентарь полон, остаток падает на землю рядом с игроком.
- **Блокировка/разблокировка** — владелец Telegram-аккаунта может заблокировать вход на сервер прямо из бота; заблокированный игрок получает кик при попытке зайти.
- **Защита по IP** — если у привязанного игрока меняется IP, он телепортируется в отдельный мир (`AUTHTG` по умолчанию) и не может двигаться/ломать блоки/использовать команды, пока не подтвердит вход кнопкой в Telegram. Если бот недоступен, проверка IP автоматически пропускается — чтобы игрок не остался заблокирован навсегда.
- **Информация об аккаунте** — кнопка «ℹ Инфо» в боте показывает никнейм, статус блокировки, статус награды и последний известный IP.

## Установка

1. Скачайте `TGLink-<версия>.jar` из [Releases](../../releases) или соберите сами (см. ниже).
2. Поместите jar в папку `plugins` вашего сервера.
3. Запустите сервер один раз, чтобы сгенерировался `plugins/TGLink/config.yml`.
4. Отредактируйте `config.yml` — впишите токен и юзернейм своего Telegram-бота (получить у [@BotFather](https://t.me/BotFather)).
5. Перезапустите сервер (или `/reload`, плагин это переживает без зависаний).
6. Игроки вводят `/link` в игре и отправляют полученный код боту.

## Конфигурация (`config.yml`)

```yaml
bot:
  username: "USERNAME_BOT_TELEGRAM"   # юзернейм бота без @
  token: "TOKEN_BOT_TELEGRAM"         # токен от @BotFather

reward:
  diamonds: 5                         # алмазов за первую привязку
  exp: 50                             # опыта за первую привязку

auth:
  world: "AUTHTG"                     # мир, в который телепортирует при смене IP
```

## Сборка из исходников

```bash
mvn clean package
```

Собранный jar появится в `target/TGLink-<версия>.jar` (используется maven-shade-plugin, все зависимости уже внутри).

## Требования

- Paper/Spigot API **1.16.5**
- Java **8–16** для запуска самого сервера Paper 1.16.5 (более новые JDK, например 17+, официально не поддерживаются ядром этой версии Minecraft)
- Аккаунт Telegram-бота (токен через [@BotFather](https://t.me/BotFather))
- SQLite — зависимость подключена автоматически, отдельно ставить не нужно

## Команды

| Команда | Кто | Описание |
|---|---|---|
| `/link` | игрок | получить код для привязки аккаунта к Telegram |

Всё остальное управление (инфо, блокировка, разблокировка, отвязка, подтверждение входа при смене IP) происходит через кнопки в самом Telegram-боте.

---

## English

TGLink is a Minecraft server plugin that integrates with Telegram: players link their in-game account via a one-time code, receive a reward for their first link, and can block/unblock server access or confirm a login after an IP change — all managed from a Telegram bot.

### Features

- **Account linking** via a one-time, 10-minute, single-use code.
- **First-link reward** (configurable diamonds + XP; overflow drops on the ground instead of being lost).
- **Block/unblock** server access directly from Telegram; blocked players are kicked on login.
- **IP-change protection** — a linked player whose IP changes is teleported to a holding world (`AUTHTG` by default) and restricted (no movement/block breaking or placing/commands) until they confirm via a Telegram button. If the bot is unavailable, the IP check is skipped automatically so players are never locked out permanently.
- **Account info** button in the bot shows nickname, block status, reward status, and last known IP.

### Installation

1. Download `TGLink-<version>.jar` from [Releases](../../releases) or build it yourself (below).
2. Drop the jar into your server's `plugins` folder.
3. Start the server once to generate `plugins/TGLink/config.yml`.
4. Edit `config.yml` and set your Telegram bot's token and username (from [@BotFather](https://t.me/BotFather)).
5. Restart the server (or `/reload` — the plugin shuts down its bot session without blocking the server).
6. Players run `/link` in-game and send the code to the bot.

### Build from source

```bash
mvn clean package
```

### Requirements

- Paper/Spigot API **1.16.5**
- Java **8–16** to run the Paper 1.16.5 server itself (newer JDKs such as 17+ are not officially supported by this Minecraft core version)
- A Telegram bot token from [@BotFather](https://t.me/BotFather)
- SQLite — bundled automatically via the shaded jar
