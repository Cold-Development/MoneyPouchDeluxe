package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.ColdCommandWrapper;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Keeps the command form of older versions working: /mp &lt;pouch&gt; [player] [amount] runs
 * /mp give &lt;pouch&gt; [player] [amount]. Owners have it in crates, NPCs and other plugins' configs.
 * A subcommand with the same name as a pouch always wins.
 */
public class ShortcutCommandWrapper extends ColdCommandWrapper {

    private final MoneyPouchDeluxe plugin;
    private final BaseCommand command;

    public ShortcutCommandWrapper(MoneyPouchDeluxe plugin, BaseCommand command) {
        super(plugin, command);
        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return super.execute(sender, commandLabel, this.rewrite(args));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args) {
        if (args.length > 1) {
            return super.tabComplete(sender, commandLabel, this.rewrite(args));
        }

        // First argument: the subcommands, plus the pouches for the shortcut
        List<String> suggestions = new ArrayList<>(super.tabComplete(sender, commandLabel, args));
        Optional<GiveCommand> give = this.command.getGiveCommand();
        if (args.length == 1 && give.isPresent() && give.get().canUse(sender)) {
            for (Pouch pouch : this.plugin.getPouches()) {
                if (StringUtil.startsWithIgnoreCase(pouch.getId(), args[0]) && !suggestions.contains(pouch.getId())) {
                    suggestions.add(pouch.getId());
                }
            }
        }
        Collections.sort(suggestions);
        return suggestions;
    }

    private String[] rewrite(String[] args) {
        if (args.length == 0 || this.command.findCommand(args[0]).isPresent() || this.plugin.getPouch(args[0]) == null) {
            return args;
        }
        Optional<GiveCommand> give = this.command.getGiveCommand();
        if (give.isEmpty()) {
            return args;
        }
        String[] rewritten = new String[args.length + 1];
        rewritten[0] = give.get().getName();
        System.arraycopy(args, 0, rewritten, 1, args.length);
        return rewritten;
    }

}
