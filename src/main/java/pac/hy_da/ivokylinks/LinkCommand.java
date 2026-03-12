package pac.hy_da.ivokylinks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkCommand implements CommandExecutor {
    private final Ivokylinks plugin;

    public LinkCommand(Ivokylinks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getDatabase().getTelegramId(player.getUniqueId()) != -1) {
            player.sendMessage(ChatColor.RED + "Ваш аккаунт уже привязан к Telegram!");
            return true;
        }

        String code = plugin.getLinkManager().generateCode(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Ваш код: " + ChatColor.YELLOW + code);
        player.sendMessage(ChatColor.GRAY + "Отправьте его боту: @ivokyserver_bot");

        return true;
    }
}