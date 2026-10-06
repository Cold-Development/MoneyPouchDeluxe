package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.manager.LocaleManager;
import org.bukkit.command.CommandSender;

/**
 * /mp reload: config.yml, the locale files, commands, economies and pouches.
 */
public class ReloadCommand extends BasePouchCommand {

    public ReloadCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        this.plugin.reload(() -> {
            this.plugin.reloadPouches();
            this.plugin.getManager(LocaleManager.class).sendMessage(sender, "command-reload-reloaded");
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("reload")
                .descriptionKey("command-reload-description")
                .permission(ADMIN_PERMISSION)
                .build();
    }

}
