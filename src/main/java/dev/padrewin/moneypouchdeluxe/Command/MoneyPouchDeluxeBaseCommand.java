package dev.padrewin.moneypouchdeluxe.Command;

import dev.padrewin.moneypouchdeluxe.Exception.HologramHandler;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoneyPouchDeluxeBaseCommand implements CommandExecutor, TabCompleter {

    private final MoneyPouchDeluxe plugin;

    public MoneyPouchDeluxeBaseCommand(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    private static class InventoryChecker {
        private final Player player;

        public InventoryChecker(Player player) {
            this.player = player;
        }

        public int getEmptyInventorySlots() {
            int emptySlots = 0;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < 36; i++) {
                if (contents[i] == null) {
                    emptySlots++;
                }
            }
            return emptySlots;
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            Player target = null;

            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            }

            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount > 64) {
                        sender.sendMessage(ChatColor.RED + "Warning: The amount requested is above 64. This may result in strange behaviour.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid integer");
                    return true;
                }
            }

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "The specified player could not be found.");
                return true;
            }

            Pouch pouch = null;
            for (Pouch p : plugin.getPouches()) {
                if (p.getId().equalsIgnoreCase(args[0])) {
                    pouch = p;
                    break;
                }
            }

            if (pouch == null) {
                sender.sendMessage(ChatColor.RED + "The pouch " + ChatColor.DARK_RED + args[0] + ChatColor.RED + " could not be found.");
                return true;
            }

            InventoryChecker inventoryChecker = new InventoryChecker(target);
            int emptySlots = inventoryChecker.getEmptyInventorySlots();
            if (emptySlots >= amount) {
                target.getInventory().addItem(pouch.getItemStack());
            } else {

                boolean isStackerPluginPresent = isStackerPluginDetected();

                for (int i = 0; i < amount; i++) {
                    ItemStack pouchItem = pouch.getItemStack();
                    Item droppedItem = target.getWorld().dropItemNaturally(target.getLocation(), pouchItem);


                    if (!isStackerPluginPresent) {
                        HologramHandler.createHologram(droppedItem, plugin);
                    }
                }

                String fullInventoryMessage = plugin.getMessage(MoneyPouchDeluxe.Message.FULL_INV)
                        .replace("%player%", target.getName());
                sender.sendMessage(fullInventoryMessage);

                String fullInventoryPlayerMessage = plugin.getMessage(MoneyPouchDeluxe.Message.PLAYER_FULL_INV);
                target.sendMessage(fullInventoryPlayerMessage);
            }

            sender.sendMessage(plugin.getMessage(MoneyPouchDeluxe.Message.GIVE_ITEM)
                    .replace("%player%", target.getName())
                    .replace("%item%", pouch.getItemStack().getItemMeta().getDisplayName()));

            if (plugin.getConfig().getBoolean("options.show-receive-message", true)) {
                target.sendMessage(plugin.getMessage(MoneyPouchDeluxe.Message.RECEIVE_ITEM)
                        .replace("%player%", target.getName())
                        .replace("%item%", pouch.getItemStack().getItemMeta().getDisplayName()));
            }

            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "MoneyPouchDeluxe (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage(ChatColor.GRAY + "<> = required, [] = optional");
        sender.sendMessage(ChatColor.YELLOW + "/mp | /cp :" + ChatColor.GRAY + " view this menu");
        sender.sendMessage(ChatColor.YELLOW + "/mp | /cp <tier> [player] [amount] :" + ChatColor.GRAY + " give <item> to [player] (or self if blank)");
        sender.sendMessage(ChatColor.YELLOW + "/mpshop | /cpshop :" + ChatColor.GRAY + " open the shop");
        sender.sendMessage(ChatColor.YELLOW + "/mpa list | /cpa list :" + ChatColor.GRAY + " list all pouches");
        sender.sendMessage(ChatColor.YELLOW + "/mpa economies | /cpa economies :" + ChatColor.GRAY + " list all economies");
        sender.sendMessage(ChatColor.YELLOW + "/mpa reload | /cpa reload :" + ChatColor.GRAY + " reload the config");
        sender.sendMessage(ChatColor.YELLOW + "/mpa killholo | /cpa killholo :" + ChatColor.GRAY + " kill holo made by plugin");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                List<String> pouchNames = new ArrayList<>();
                for (Pouch pouch : plugin.getPouches()) {
                    pouchNames.add(pouch.getId());
                }
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], pouchNames, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return null;
    }
    private boolean isStackerPluginDetected() {
        String[] stackerPlugins = {
                "RoseStacker",
                "WildStacker",
                "EpicStacker",
                "LagAssist",
                "SimpleStack",
                "StackMob",
                "Stacker"
        };

        for (String pluginName : stackerPlugins) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
                return true;
            }
        }
        return false;
    }
}
