package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.manager.LocaleManager;

/**
 * Base of every /mp subcommand.
 */
public abstract class BasePouchCommand extends BaseColdCommand {

    /**
     * Permission for the admin commands (give, list, economies, reload).
     */
    public static final String ADMIN_PERMISSION = "moneypouch.admin";

    protected final MoneyPouchDeluxe plugin;
    protected final LocaleManager localeManager;

    public BasePouchCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

}
