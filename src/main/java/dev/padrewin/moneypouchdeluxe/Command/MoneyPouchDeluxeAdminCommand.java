package dev.padrewin.moneypouchdeluxe.Command;

import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe.Message;
import dev.padrewin.moneypouchdeluxe.Pouch;
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;

import java.util.*;

public class MoneyPouchDeluxeAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("list", "economies", "reload");

    private final MoneyPouchDeluxe plugin;

    public MoneyPouchDeluxeAdminCommand(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("list")) {
                list(sender);
                return true;
            } else if (sub.equals("economy") || sub.equals("economies")) {
                economies(sender);
                return true;
            } else if (sub.equals("reload")) {
                plugin.reload();
                Text.send(sender, plugin.getMessage(Message.RELOADED));
                return true;
            }
        }

        plugin.sendHelp(sender);
        return true;
    }

    private void list(CommandSender sender) {
        List<Pouch> pouches = plugin.getPouches();
        Text.send(sender, plugin.getMessage(Message.LIST_HEADER, "%count%", String.valueOf(pouches.size())));
        if (pouches.isEmpty()) {
            Text.send(sender, plugin.getMessage(Message.LIST_EMPTY));
            return;
        }
        String separator = plugin.getConfig().getString("pouches.title.format.separator", ",");
        for (Pouch pouch : pouches) {
            EconomyType economy = pouch.getEconomyType();
            Text.send(sender, plugin.getMessage(Message.LIST_ENTRY,
                    "%pouch%", pouch.getId(),
                    "%min%", Text.color(economy.getPrefix()) + UseListener.formatNumber(pouch.getMinRange(), separator) + Text.color(economy.getSuffix()),
                    "%max%", Text.color(economy.getPrefix()) + UseListener.formatNumber(pouch.getMaxRange(), separator) + Text.color(economy.getSuffix()),
                    "%economy%", economyId(economy),
                    "%permission%", pouch.getPermission() != null ? Text.color(" &8| &7" + pouch.getPermission()) : ""));
        }
    }

    private void economies(CommandSender sender) {
        Map<String, EconomyType> economies = new TreeMap<>(plugin.getEconomyTypes());
        Text.send(sender, plugin.getMessage(Message.ECONOMIES_HEADER, "%count%", String.valueOf(economies.size())));
        for (Map.Entry<String, EconomyType> entry : economies.entrySet()) {
            Text.send(sender, plugin.getMessage(Message.ECONOMIES_ENTRY,
                    "%id%", entry.getKey(),
                    "%type%", entry.getValue().toString(),
                    "%prefix%", Text.color(entry.getValue().getPrefix()),
                    "%suffix%", Text.color(entry.getValue().getSuffix())));
        }
    }

    private String economyId(EconomyType economy) {
        for (Map.Entry<String, EconomyType> entry : plugin.getEconomyTypes().entrySet()) {
            if (entry.getValue() == economy) {
                return entry.getKey();
            }
        }
        return economy.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
