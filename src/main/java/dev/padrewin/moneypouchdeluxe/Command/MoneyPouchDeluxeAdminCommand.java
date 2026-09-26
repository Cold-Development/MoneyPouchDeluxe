package dev.padrewin.moneypouchdeluxe.Command;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.*;

public class MoneyPouchDeluxeAdminCommand implements CommandExecutor, TabCompleter {

    private final MoneyPouchDeluxe plugin;

    public MoneyPouchDeluxeAdminCommand(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("list")) {
                for (Pouch pouch : plugin.getPouches()) {
                    sender.sendMessage(ChatColor.GOLD + pouch.getId() + " " + ChatColor.YELLOW + "(min: " +
                            pouch.getMinRange() + ", max: " + pouch.getMaxRange() + ", economy: " +
                            pouch.getEconomyType().toString() + " [" + pouch.getEconomyType().getPrefix() +
                            ChatColor.DARK_GRAY + "/" + ChatColor.YELLOW + pouch.getEconomyType().getSuffix() + "])");
                }
                return true;
            } else if (args[0].equals("economy") || args[0].equals("economies")) {
                for (Map.Entry<String, EconomyType> economyTypeEntry : plugin.getEconomyTypes().entrySet()) {
                    sender.sendMessage(ChatColor.GOLD + economyTypeEntry.getKey() + " " + ChatColor.YELLOW
                            + economyTypeEntry.getValue().toString() + " [" + economyTypeEntry.getValue().getPrefix() +
                            ChatColor.DARK_GRAY + "/" + ChatColor.YELLOW + economyTypeEntry.getValue().getSuffix() + "])");
                }
                return true;
            } else if (args[0].equals("reload")) {
                plugin.reload();
                Text.send(sender, plugin.getMessage(MoneyPouchDeluxe.Message.RELOADED));
                return true;
            }
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "MoneyPouchDeluxe (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage(ChatColor.GRAY + "<> = required, [] = optional");
        sender.sendMessage(ChatColor.YELLOW + "/mpa | /cpa :" + ChatColor.GRAY + " view this menu");
        sender.sendMessage(ChatColor.YELLOW + "/mp (/cp) <tier> [player] [amount] :" + ChatColor.GRAY + " give <item> to [player] (or self if blank)");
        sender.sendMessage(ChatColor.YELLOW + "/mpa list | /cpa list :" + ChatColor.GRAY + " list all pouches");
        sender.sendMessage(ChatColor.YELLOW + "/mpa economies | /cpa economies :" + ChatColor.GRAY + " list all economies");
        sender.sendMessage(ChatColor.YELLOW + "/mpa reload | /cpa reload :" + ChatColor.GRAY + " reload the config");
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