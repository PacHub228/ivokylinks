package pac.hy_da.ivokylinks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TelegramBot extends TelegramLongPollingBot {
    private final Ivokylinks plugin;
    private final String username;
    private final String token;

    public TelegramBot(Ivokylinks plugin) {
        super(shortPollingOptions());
        this.plugin = plugin;
        this.username = plugin.getConfig().getString("bot.username", "USERNAME_BOT_TELEGRAM");
        this.token = plugin.getConfig().getString("bot.token", "TOKEN_BOT_TELEGRAM");
    }

    private static DefaultBotOptions shortPollingOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setGetUpdatesTimeout(5);
        return options;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText() || !update.getMessage().isUserMessage()) return;

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        if (text.equals("/start")) {
            sendMenu(chatId, "Добро пожаловать!");
        } else if (text.matches("\\d{6}")) {
            handleLinkCode(chatId, text);
        } else if (text.equals("ℹ Инфо")) {
            handleInfo(chatId);
        } else if (text.equals("🔒 Блокировка") || text.equals("🔓 Разблокировка") || text.equals("🚫 Отвязать")) {
            handleAccountAction(chatId, text);
        }
    }

    private void handleLinkCode(long chatId, String code) {
        if (plugin.getDatabase().getPlayerNameByTg(chatId) != null) {
            sendMsg(chatId, "Этот Telegram уже привязан к другому аккаунту.");
            return;
        }

        UUID uuid = plugin.getLinkManager().getPlayerByCode(code);
        if (uuid == null) {
            sendMsg(chatId, "Код неверен или устарел.");
            return;
        }

        plugin.getLinkManager().removeCode(code);
        if (!plugin.getDatabase().linkPlayer(uuid, chatId)) {
            sendMsg(chatId, "Этот игровой аккаунт уже привязан.");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (player.getAddress() != null) {
                    plugin.getDatabase().updateIp(uuid, player.getAddress().getAddress().getHostAddress());
                }
                plugin.getAuthListener().giveRewardIfEligible(player, chatId);
            }
        });

        sendMenu(chatId, "✅ Привязано!");
    }

    private void handleInfo(long chatId) {
        String uuidStr = plugin.getDatabase().getPlayerNameByTg(chatId);
        if (uuidStr == null) {
            sendMsg(chatId, "Сначала привяжите аккаунт!");
            return;
        }
        UUID uuid = UUID.fromString(uuidStr);

        String nickname = Bukkit.getOfflinePlayer(uuid).getName();
        boolean blocked = plugin.getDatabase().isBlocked(uuid);
        boolean rewarded = plugin.getDatabase().hasReceivedReward(chatId);
        String ip = plugin.getDatabase().getIp(uuid);

        String info = "👤 Никнейм: " + (nickname != null ? nickname : "неизвестно") + "\n"
                + "🔐 Статус: " + (blocked ? "заблокирован" : "активен") + "\n"
                + "🎁 Награда получена: " + (rewarded ? "да" : "нет") + "\n"
                + "🌐 Последний IP: " + (ip != null ? ip : "неизвестен");
        sendMsg(chatId, info);
    }

    private void handleAccountAction(long chatId, String action) {
        String uuidStr = plugin.getDatabase().getPlayerNameByTg(chatId);
        if (uuidStr == null) {
            sendMsg(chatId, "Сначала привяжите аккаунт!");
            return;
        }
        UUID uuid = UUID.fromString(uuidStr);

        if (action.equals("🔒 Блокировка")) {
            plugin.getDatabase().setBlocked(uuid, true);
            plugin.getAuthListener().kickIfOnline(uuid, "§cАккаунт заблокирован через Telegram!");
        } else if (action.equals("🔓 Разблокировка")) {
            plugin.getDatabase().setBlocked(uuid, false);
        } else if (action.equals("🚫 Отвязать")) {
            plugin.getDatabase().unlink(chatId);
            plugin.getAuthListener().cancelPendingAuth(uuid);
        }

        sendMenu(chatId, "Действие выполнено.");
    }

    private void handleCallback(CallbackQuery query) {
        if (!"confirm_login".equals(query.getData()) || query.getMessage() == null) return;

        boolean confirmed = plugin.getAuthListener().confirmLogin(query.getMessage().getChatId());

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(query.getId());
        answer.setText(confirmed ? "Подтверждено!" : "Запрос неактуален.");
        try {
            execute(answer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAuthConfirmation(long chatId, String ip) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText("⚠️ Обнаружен вход с нового IP-адреса (" + ip + "). Если это вы — подтвердите вход.");

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("✅ Подтвердить вход");
        button.setCallbackData("confirm_login");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(button)));
        sm.setReplyMarkup(markup);

        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMenu(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(text);

        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow r1 = new KeyboardRow();
        r1.add("ℹ Инфо");
        rows.add(r1);

        String uuidStr = plugin.getDatabase().getPlayerNameByTg(chatId);
        if (uuidStr != null) {
            KeyboardRow r2 = new KeyboardRow();
            r2.add(plugin.getDatabase().isBlocked(UUID.fromString(uuidStr)) ? "🔓 Разблокировка" : "🔒 Блокировка");
            rows.add(r2);

            KeyboardRow r3 = new KeyboardRow();
            r3.add("🚫 Отвязать");
            rows.add(r3);
        }

        rkm.setKeyboard(rows);
        rkm.setResizeKeyboard(true);
        sm.setReplyMarkup(rkm);

        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(text);
        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
