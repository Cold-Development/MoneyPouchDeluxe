package dev.padrewin.moneypouchdeluxe.Command;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe.Message;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoneyPouchDeluxeBaseCommand implements CommandExecutor, TabCompleter {

    /**
     * A full player inventory of pouches; anything above that would mostly end up on the ground.
     */
    public static final int MAX_AMOUNT = 36 * 64;
    public static final String GIVE_ALL_PERMISSION = "moneypouch.admin.giveall";

    private final MoneyPouchDeluxe plugin;

    public MoneyPouchDeluxeBaseCommand(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            plugin.sendHelp(sender);
            return true;
        }

        Pouch pouch = plugin.getPouch(args[0]);
        if (pouch == null) {
            Text.send(sender, plugin.getMessage(Message.POUCH_NOT_FOUND, "%pouch%", args[0]));
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            amount = parseAmount(args[2]);
            if (amount < 1) {
                Text.send(sender, plugin.getMessage(Message.INVALID_AMOUNT,
                        "%amount%", args[2], "%max%", String.valueOf(MAX_AMOUNT)));
                return true;
            }
        }
        String item = plugin.getPouchName(pouch);

        // === GIVE TO ALL ONLINE PLAYERS WITH "*" ===
        if (args.length >= 2 && args[1].equals("*")) {
            if (!sender.hasPermission(GIVE_ALL_PERMISSION)) {
                Text.send(sender, plugin.getMessage(Message.NO_PERMISSION_COMMAND));
                return true;
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                give(sender, online, pouch, amount, false);
            }
            Text.send(sender, plugin.getMessage(Message.GIVE_ALL,
                    "%item%", item, "%amount%", String.valueOf(amount)));
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                Text.send(sender, plugin.getMessage(Message.PLAYER_NOT_FOUND, "%player%", args[1]));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            Text.send(sender, plugin.getMessage(Message.PLAYER_REQUIRED));
            return true;
        }

        give(sender, target, pouch, amount, true);
        Text.send(sender, plugin.getMessage(Message.GIVE_ITEM,
                "%player%", target.getName(), "%item%", item, "%amount%", String.valueOf(amount)));
        return true;
    }

    /**
     * @return the amount, or -1 if it isn't a whole number from 1 to {@link #MAX_AMOUNT}
     */
    private static int parseAmount(String input) {
        try {
            int amount = Integer.parseInt(input);
            return amount >= 1 && amount <= MAX_AMOUNT ? amount : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * @param tellSender whether the sender hears about a full inventory (not for every player of a give-all)
     */
    private void give(CommandSender sender, Player target, Pouch pouch, int amount, boolean tellSender) {
        String item = plugin.getPouchName(pouch);
        plugin.runAtPlayer(target, () -> {
            if (!target.isOnline()) {
                return;
            }
            if (plugin.giveOrDrop(target, pouch.getItemStack(), amount)) {
                if (tellSender && sender != target) {
                    Text.send(sender, plugin.getMessage(Message.FULL_INV, "%player%", target.getName()));
                }
                Text.send(target, plugin.getMessage(Message.PLAYER_FULL_INV));
            }
            Text.send(target, plugin.getMessage(Message.RECEIVE_ITEM,
                    "%player%", target.getName(), "%item%", item, "%amount%", String.valueOf(amount)));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            for (Pouch pouch : plugin.getPouches()) {
                options.add(pouch.getId());
            }
        } else if (args.length == 2) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!(sender instanceof Player) || ((Player) sender).canSee(online)) {
                    options.add(online.getName());
                }
            }
            if (sender.hasPermission(GIVE_ALL_PERMISSION)) {
                options.add("*");
            }
        } else if (args.length == 3) {
            options.add("1");
            options.add("16");
            options.add("64");
        }

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], options, completions);
        Collections.sort(completions);
        return completions;
    }

}
