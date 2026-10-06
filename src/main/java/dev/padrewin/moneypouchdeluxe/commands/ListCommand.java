package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * /mp list: every loaded pouch with its range, economy and permission.
 */
public class ListCommand extends BasePouchCommand {

    public ListCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        List<Pouch> pouches = this.plugin.getPouches();

        this.localeManager.sendMessage(sender, "command-list-header", StringPlaceholders.of("count", pouches.size()));
        if (pouches.isEmpty()) {
            this.localeManager.sendSimpleMessage(sender, "command-list-empty");
            return;
        }

        String separator = this.plugin.getConfig().getString("pouches.title.format.separator", ",");
        for (Pouch pouch : pouches) {
            EconomyType economy = pouch.getEconomyType();
            this.localeManager.sendSimpleMessage(sender, "command-list-entry", StringPlaceholders.builder()
                    .add("pouch", pouch.getId())
                    .add("min", economy.getPrefix() + UseListener.formatNumber(pouch.getMinRange(), separator) + economy.getSuffix())
                    .add("max", economy.getPrefix() + UseListener.formatNumber(pouch.getMaxRange(), separator) + economy.getSuffix())
                    .add("economy", this.plugin.getEconomyId(economy))
                    .add("permission", pouch.getPermission() != null ? " &8| &7" + pouch.getPermission() : "")
                    .build());
        }
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("list")
                .descriptionKey("command-list-description")
                .permission(ADMIN_PERMISSION)
                .build();
    }

}
