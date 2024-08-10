package dev.padrewin.coldpouch.Command;

import dev.padrewin.coldpouch.ColdPouch;
import dev.padrewin.coldpouch.Gui.ShopMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ColdPouchShopCommand implements CommandExecutor {

    private final ColdPouch plugin;

    public ColdPouchShopCommand(ColdPouch plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (!plugin.getConfig().getBoolean("shop.enabled", false)) {
                sender.sendMessage(plugin.getMessage(ColdPouch.Message.SHOP_DISABLED));
                return true;
            }
            Player player = (Player) sender;
            plugin.getMenuController().openMenu(player, new ShopMenu(player, plugin));
        }
        return true;
    }

}
