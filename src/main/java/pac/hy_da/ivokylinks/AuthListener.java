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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthListener implements Listener {
    private final Ivokylinks plugin;
    private final Set<UUID> pendingAuth = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AuthListener(Ivokylinks plugin) {
        this.plugin = plugin;
    }

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

        giveRewardIfEligible(player, tgId);

        if (player.getAddress() == null) return;
        String currentIp = player.getAddress().getAddress().getHostAddress();

        String storedIp = plugin.getDatabase().getIp(uuid);
        if (storedIp == null) {
            plugin.getDatabase().updateIp(uuid, currentIp);
            return;
        }
        if (storedIp.equals(currentIp)) return;

        if (plugin.getBot() == null) {
            plugin.getLogger().warning("IP игрока " + player.getName() + " изменился, но Telegram-бот недоступен — проверка пропущена.");
            plugin.getDatabase().updateIp(uuid, currentIp);
            return;
        }

        plugin.getLastLocations().put(uuid, player.getLocation());
        pendingAuth.add(uuid);
        player.teleport(getAuthWorld().getSpawnLocation());
        player.sendMessage("§cОбнаружен новый IP-адрес! Подтвердите вход через Telegram-бота.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getBot().sendAuthConfirmation(tgId, currentIp));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!pendingAuth.remove(uuid)) return;

        Location back = plugin.getLastLocations().remove(uuid);
        if (back != null) {
            event.getPlayer().teleport(back);
        }
    }

    public void giveRewardIfEligible(Player player, long tgId) {
        if (plugin.getDatabase().hasReceivedReward(tgId)) return;

        int diamonds = plugin.getConfig().getInt("reward.diamonds", 5);
        int exp = plugin.getConfig().getInt("reward.exp", 50);

        if (diamonds > 0) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
            leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        if (exp > 0) {
            player.giveExp(exp);
        }

        plugin.getDatabase().setRewarded(player.getUniqueId());
        player.sendMessage("§aНаграда за привязку Telegram получена!");
    }

    public boolean confirmLogin(long tgId) {
        String uuidStr = plugin.getDatabase().getPlayerNameByTg(tgId);
        if (uuidStr == null) return false;
        UUID uuid = UUID.fromString(uuidStr);
        if (!pendingAuth.remove(uuid)) return false;

        Bukkit.getScheduler().runTask(plugin, () -> restorePlayer(uuid, "§aВход подтверждён!", true));
        return true;
    }

    public void cancelPendingAuth(UUID uuid) {
        if (!pendingAuth.remove(uuid)) return;
        Bukkit.getScheduler().runTask(plugin, () -> restorePlayer(uuid, "§eПривязка Telegram снята, авторизация отменена.", false));
    }

    public void kickIfOnline(UUID uuid, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            pendingAuth.remove(uuid);
            Location back = plugin.getLastLocations().remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (back != null) player.teleport(back);
                player.kickPlayer(reason);
            }
        });
    }

    private void restorePlayer(UUID uuid, String message, boolean updateIp) {
        Location back = plugin.getLastLocations().remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (updateIp && player.getAddress() != null) {
            plugin.getDatabase().updateIp(uuid, player.getAddress().getAddress().getHostAddress());
        }
        player.teleport(back != null ? back : Bukkit.getWorlds().get(0).getSpawnLocation());
        player.sendMessage(message);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || !pendingAuth.contains(event.getPlayer().getUniqueId())) return;
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

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && pendingAuth.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && pendingAuth.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && pendingAuth.contains(event.getDamager().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (pendingAuth.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    public World getAuthWorld() {
        String name = plugin.getConfig().getString("auth.world", "AUTHTG");
        World world = Bukkit.getWorld(name);
        if (world == null) world = new WorldCreator(name).createWorld();
        return world;
    }
}
