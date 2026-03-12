package pac.hy_da.ivokylinks;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ivokylinks extends JavaPlugin {
    private TelegramBot bot;
    private Database database;
    private LinkManager linkManager;
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        database = new Database(new File(getDataFolder(), "database.db").getAbsolutePath());
        linkManager = new LinkManager();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            bot = new TelegramBot(this);
            botsApi.registerBot(bot);
        } catch (Exception e) { e.printStackTrace(); }

        getCommand("link").setExecutor(new LinkCommand(this));
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);
        getLogger().info("Ivokylinks запущен!");
    }

    public Map<UUID, Location> getLastLocations() { return lastLocations; }
    public TelegramBot getBot() { return bot; }
    public Database getDatabase() { return database; }
    public LinkManager getLinkManager() { return linkManager; }
    @Override
    public void onDisable() { database.close(); }
}