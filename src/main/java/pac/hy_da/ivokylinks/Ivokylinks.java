package pac.hy_da.ivokylinks;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ivokylinks extends JavaPlugin {
    private TelegramBot bot;
    private BotSession botSession;
    private Database database;
    private LinkManager linkManager;
    private AuthListener authListener;
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        database = new Database(new File(getDataFolder(), "database.db").getAbsolutePath());
        linkManager = new LinkManager();
        authListener = new AuthListener(this);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            bot = new TelegramBot(this);
            botSession = botsApi.registerBot(bot);
        } catch (Exception e) {
            getLogger().severe("Не удалось запустить Telegram-бота, функции привязки будут недоступны: " + e.getMessage());
            bot = null;
        }

        getCommand("link").setExecutor(new LinkCommand(this));
        getServer().getPluginManager().registerEvents(authListener, this);
        getLogger().info("Ivokylinks запущен!");
    }

    public Map<UUID, Location> getLastLocations() { return lastLocations; }
    public TelegramBot getBot() { return bot; }
    public Database getDatabase() { return database; }
    public LinkManager getLinkManager() { return linkManager; }
    public AuthListener getAuthListener() { return authListener; }

    @Override
    public void onDisable() {
        if (botSession != null && botSession.isRunning()) {
            Thread stopper = new Thread(() -> {
                try { botSession.stop(); } catch (Throwable ignored) { }
            }, "TGLink-BotSession-Stop");
            stopper.setDaemon(true);
            stopper.start();
        }
        if (database != null) database.close();
    }
}
