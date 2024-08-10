package dev.padrewin.coldpouch.Command;

import dev.padrewin.coldpouch.ColdPouch;
import dev.padrewin.coldpouch.Pouch;
import dev.padrewin.coldpouch.EconomyType.EconomyType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ColdPouchAdminCommand implements CommandExecutor, TabCompleter {

    private final ColdPouch plugin;

    public ColdPouchAdminCommand(ColdPouch plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("list")) {
                for (Pouch pouch : plugin.getPouches()) {
                    sender.sendMessage(ChatColor.DARK_PURPLE + pouch.getId() + " " + ChatColor.LIGHT_PURPLE + "(min: " +
                            pouch.getMinRange() + ", max: " + pouch.getMaxRange() + ", economy: " +
                            pouch.getEconomyType().toString() + " [" + pouch.getEconomyType().getPrefix() +
                            ChatColor.DARK_GRAY + "/" + ChatColor.LIGHT_PURPLE + pouch.getEconomyType().getSuffix() + "])");
                }
                return true;
            } else if (args[0].equals("economy") || args[0].equals("economies") ) {
                for (Map.Entry<String, EconomyType> economyTypeEntry : plugin.getEconomyTypes().entrySet()) {
                    sender.sendMessage(ChatColor.DARK_PURPLE + economyTypeEntry.getKey() + " " + ChatColor.LIGHT_PURPLE
                            + economyTypeEntry.getValue().toString() + " [" + economyTypeEntry.getValue().getPrefix() +
                            ChatColor.DARK_GRAY + "/" + ChatColor.LIGHT_PURPLE +  economyTypeEntry.getValue().getSuffix() + "])");
                }
                return true;
            } else if (args[0].equals("reload")) {
                plugin.reload();
                String reloadMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.reloaded"));
                sender.sendMessage(reloadMessage);
                return true;
            }
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Cold Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage(ChatColor.GRAY + "<> = required, [] = optional");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cpa :" + ChatColor.GRAY + " view this menu");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cp <tier> [player] [amount] :" + ChatColor.GRAY + " give <item> to [player] (or self if blank)");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cpshop :" + ChatColor.GRAY + " open the shop");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cpa list :" + ChatColor.GRAY + " list all pouches");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cpa economies :" + ChatColor.GRAY + " list all economies");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/cpa reload :" + ChatColor.GRAY + " reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                List<String> options = Arrays.asList("list", "economies", "reload");
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], options, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return null;
    }
}
