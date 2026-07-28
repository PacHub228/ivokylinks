package pac.hy_da.ivokylinks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthListener implements Listener {
    private final Ivokylinks plugin;
    private final Set<UUID> pendingAuth = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AuthListener(Ivokylinks plugin) { this.plugin = plugin; }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getDatabase().isBlocked(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§cАккаунт заблокирован через Telegram!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long tgId = plugin.getDatabase().getTelegramId(uuid);
        if (tgId == -1) return;

        if (!plugin.getDatabase().hasReceivedReward(tgId)) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(
                    new ItemStack(Material.DIAMOND, plugin.getConfig().getInt("reward.diamonds", 5)));
            leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            player.giveExp(plugin.getConfig().getInt("reward.exp", 50));
            plugin.getDatabase().setRewarded(uuid);
            player.sendMessage("§aНаграда за привязку Telegram получена!");
        }

        if (player.getAddress() == null) return;
        String currentIp = player.getAddress().getAddress().getHostAddress();

        String storedIp = plugin.getDatabase().getIp(uuid);
        if (storedIp == null) {
            plugin.getDatabase().updateIp(uuid, currentIp);
            return;
        }
        if (storedIp.equals(currentIp)) return;

        if (plugin.getBot() == null) {
            plugin.getLogger().warning("IP игрока " + player.getName() + " изменился, но Telegram-бот недоступен — проверка пропущена, чтобы не заблокировать игрока навсегда.");
            plugin.getDatabase().updateIp(uuid, currentIp);
            return;
        }

        plugin.getLastLocations().put(uuid, player.getLocation());
        pendingAuth.add(uuid);
        player.teleport(getAuthWorld().getSpawnLocation());
        player.sendMessage("§cОбнаружен новый IP-адрес! Подтвердите вход через Telegram-бота.");
        plugin.getBot().sendAuthConfirmation(tgId, currentIp);
    }

    public void confirmLogin(long tgId) {
        String uuidStr = plugin.getDatabase().getPlayerNameByTg(tgId);
        if (uuidStr == null) return;
        UUID uuid = UUID.fromString(uuidStr);
        if (!pendingAuth.remove(uuid)) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (player.getAddress() != null) {
            plugin.getDatabase().updateIp(uuid, player.getAddress().getAddress().getHostAddress());
        }
        Location back = plugin.getLastLocations().remove(uuid);
        player.teleport(back != null ? back : Bukkit.getWorlds().get(0).getSpawnLocation());
        player.sendMessage("§aВход подтверждён!");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!pendingAuth.contains(event.getPlayer().getUniqueId())) return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    private World getAuthWorld() {
        String name = plugin.getConfig().getString("auth.world", "AUTHTG");
        World world = Bukkit.getWorld(name);
        if (world == null) world = new WorldCreator(name).createWorld();
        return world;
    }
}
