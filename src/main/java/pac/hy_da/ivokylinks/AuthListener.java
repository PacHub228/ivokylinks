package pac.hy_da.ivokylinks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AuthListener implements Listener {
    private final Ivokylinks plugin;
    public AuthListener(Ivokylinks plugin) { this.plugin = plugin; }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getDatabase().isBlocked(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§cАккаунт заблокирован через Telegram!");
        }
    }
}