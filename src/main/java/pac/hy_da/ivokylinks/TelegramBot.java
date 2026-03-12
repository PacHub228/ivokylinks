package pac.hy_da.ivokylinks;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {
    private final Ivokylinks plugin;
    public TelegramBot(Ivokylinks plugin) { this.plugin = plugin; }
    @Override public String getBotUsername() { return "USERNAME_BOT_TELEGRAM"; }
    @Override public String getBotToken() { return "TOKEN_BOT_TELEGRAM"; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (text.equals("/start")) { sendMenu(chatId, "Добро пожаловать!"); }
        else if (text.matches("\\d{6}")) {
            UUID uuid = plugin.getLinkManager().getPlayerByCode(text);
            if (uuid != null) {
                plugin.getDatabase().linkPlayer(uuid, chatId);
                sendMenu(chatId, "✅ Привязано!");
            }
        }
        else if (text.equals("ℹ Инфо") || text.equals("🔒 Блокировка") || text.equals("🔓 Разблокировка") || text.equals("🚫 Отвязать")) {
            String uuidStr = plugin.getDatabase().getPlayerNameByTg(chatId);
            if (uuidStr == null) { sendMsg(chatId, "Сначала привяжите аккаунт!"); return; }
            UUID uuid = UUID.fromString(uuidStr);

            if (text.equals("🔒 Блокировка")) plugin.getDatabase().setBlocked(uuid, true);
            else if (text.equals("🔓 Разблокировка")) plugin.getDatabase().setBlocked(uuid, false);
            else if (text.equals("🚫 Отвязать")) plugin.getDatabase().unlink(chatId);

            sendMenu(chatId, "Действие выполнено.");
        }
    }

    public void sendMenu(long chatId, String text) {
        SendMessage sm = new SendMessage(); sm.setChatId(String.valueOf(chatId)); sm.setText(text);
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup(); List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow r1 = new KeyboardRow(); r1.add("ℹ Инфо"); rows.add(r1);

        String uuidStr = plugin.getDatabase().getPlayerNameByTg(chatId);
        if (uuidStr != null) {
            KeyboardRow r2 = new KeyboardRow();
            r2.add(plugin.getDatabase().isBlocked(UUID.fromString(uuidStr)) ? "🔓 Разблокировка" : "🔒 Блокировка");
            rows.add(r2);
        }
        KeyboardRow r3 = new KeyboardRow(); r3.add("🚫 Отвязать"); rows.add(r3);
        rkm.setKeyboard(rows); rkm.setResizeKeyboard(true); sm.setReplyMarkup(rkm);
        try { execute(sm); } catch (Exception e) { e.printStackTrace(); }
    }
    public void sendMsg(long chatId, String text) { SendMessage sm = new SendMessage(); sm.setChatId(String.valueOf(chatId)); sm.setText(text); try { execute(sm); } catch (Exception e) { e.printStackTrace(); } }
}