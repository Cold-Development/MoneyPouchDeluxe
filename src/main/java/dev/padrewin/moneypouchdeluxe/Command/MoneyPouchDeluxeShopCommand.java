package dev.padrewin.moneypouchdeluxe.Command;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Gui.ShopMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyPouchDeluxeShopCommand implements CommandExecutor {

    private final MoneyPouchDeluxe plugin;

    public MoneyPouchDeluxeShopCommand(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (!plugin.getConfig().getBoolean("shop.enabled", false)) {
                sender.sendMessage(plugin.getMessage(MoneyPouchDeluxe.Message.SHOP_DISABLED));
                return true;
            }
            Player player = (Player) sender;
            plugin.getMenuController().openMenu(player, new ShopMenu(player, plugin));
        }
        return true;
    }

}
