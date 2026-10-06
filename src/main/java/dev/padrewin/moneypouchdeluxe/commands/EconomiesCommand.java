package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.TreeMap;

/**
 * /mp economies: every loaded economy, with how it pays (XP, Vault, PlayerPoints or a command).
 */
public class EconomiesCommand extends BasePouchCommand {

    public EconomiesCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        Map<String, EconomyType> economies = new TreeMap<>(this.plugin.getEconomyTypes());

        this.localeManager.sendMessage(sender, "command-economies-header", StringPlaceholders.of("count", economies.size()));
        for (Map.Entry<String, EconomyType> entry : economies.entrySet()) {
            this.localeManager.sendSimpleMessage(sender, "command-economies-entry", StringPlaceholders.of(
                    "id", entry.getKey(),
                    "type", entry.getValue().toString(),
                    "prefix", entry.getValue().getPrefix(),
                    "suffix", entry.getValue().getSuffix()));
        }
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("economies")
                .aliases("economy")
                .descriptionKey("command-economies-description")
                .permission(ADMIN_PERMISSION)
                .build();
    }

}
