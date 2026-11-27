# TGLink (ivokylinks)

Плагин для серверов Minecraft (Spigot/Paper 1.16.5), который связывает игровой аккаунт с Telegram. Игрок привязывает профиль по одноразовому коду, получает награду, а дальше может защитить вход по IP и блокировать аккаунт прямо из бота.

## Что умеет плагин

- **Привязка аккаунта** - игрок вводит `/link` в игре, получает 6-значный код и отправляет его боту в личные сообщения. Код действует 10 минут.
- **Награда за привязку** - выдает алмазы и опыт (настраивается в конфиге). Если игрок в этот момент на сервере, награда придет сразу. Если инвентарь полон, лишнее упадет рядом.
- **Блокировка из бота** - можно в любой момент заблокировать или разблокировать свой аккаунт. Если игрок уже на сервере, его сразу кикнет.
- **Подтверждение входа при смене IP** - если игрок зашел с нового IP, его переносит в отдельный мир (`AUTHTG` по умолчанию) и замораживает, пока он не нажмет кнопку подтверждения в Telegram. Если бот в этот момент не работает, проверка пропускается, чтобы никто не застрял.
- **Инфо об аккаунте** - в боте можно посмотреть ник, статус блокировки, получена ли награда и последний IP.

## Установка

1. Скачайте готовый jar из [Releases](../../releases) или соберите проект сами.
2. Закиньте файл в папку `plugins` вашего сервера.
3. Запустите сервер, чтобы появился файл `plugins/TGLink/config.yml`.
4. Впишите в `config.yml` токен и юзернейм бота (их выдает [@BotFather](https://t.me/BotFather)).
5. Перезапустите сервер.
6. Готово - игроки могут писать `/link` в игре и отправлять код боту.

## Настройка (`config.yml`)

```yaml
bot:
  username: "USERNAME_BOT_TELEGRAM"   # юзернейм бота без @
  token: "TOKEN_BOT_TELEGRAM"         # токен от @BotFather

reward:
  diamonds: 5                         # сколько алмазов выдать за привязку
  exp: 50                             # сколько опыта выдать за привязку

auth:
  world: "AUTHTG"                     # мир ожидания при смене IP
```

## Сборка

```bash
mvn clean package
```

Готовый файл появится в папке `target/TGLink-1.0.jar` (все нужные библиотеки уже внутри).

## Требования

- Сервер на Paper/Spigot **1.16.5**
- Java **8-16** (для самого ядра 1.16.5)
- Созданный бот в Telegram через [@BotFather](https://t.me/BotFather)
- База данных SQLite уже вшита в плагин, отдельно ничего ставить не нужно

## Команды

- `/link` - получить код для привязки Telegram (доступно только игрокам).

Все остальное управление (информация, блокировка, отвязка и подтверждение входа) работает через кнопки в боте.

---

## English

TGLink is a Minecraft plugin for Spigot/Paper 1.16.5 that connects your in-game account to Telegram. Players link their profile with a one-time code, get a reward, and can protect their account with IP login confirmation and remote blocking.

### Features

- **Account linking** - run `/link` in-game to get a 6-digit code (valid for 10 minutes) and send it to the bot in a private chat.
- **First-link reward** - gives configurable diamonds and XP right after linking (or on next join). Extra items drop on the ground if the inventory is full.
- **Remote lock** - lock or unlock your Minecraft account from Telegram. Online players are kicked immediately when locked.
- **IP change protection** - if a linked player joins from a new IP, they are moved to a separate world (`AUTHTG` by default) and restricted until they tap Confirm in Telegram. If the bot is offline, the check is skipped so players do not get stuck.
- **Account info** - check your nickname, lock status, reward status, and last IP in the bot.

### Installation

1. Download the jar from [Releases](../../releases) or build it from source.
2. Put the jar into your server's `plugins` folder.
3. Start the server once to generate `plugins/TGLink/config.yml`.
4. Set your bot username and token from [@BotFather](https://t.me/BotFather) in `config.yml`.
5. Restart the server.
6. Players can now run `/link` and send the code to your bot.

### Build

```bash
mvn clean package
```

### Requirements

- Paper/Spigot **1.16.5**
- Java **8-16**
- A Telegram bot token from [@BotFather](https://t.me/BotFather)
- SQLite is bundled inside the jar
