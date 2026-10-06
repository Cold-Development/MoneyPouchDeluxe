package dev.padrewin.moneypouchdeluxe.manager;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.ColdCommandWrapper;
import dev.padrewin.colddev.manager.AbstractCommandManager;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.commands.BaseCommand;
import dev.padrewin.moneypouchdeluxe.commands.ShortcutCommandWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Registers /moneypouchdeluxe through {@link ShortcutCommandWrapper}, which keeps the old
 * /mp &lt;pouch&gt; form working, instead of the plain wrapper the base manager would use.
 */
public class CommandManager extends AbstractCommandManager {

    private final List<ColdCommandWrapper> wrappers = new ArrayList<>();

    public CommandManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @Override
    public List<Function<ColdPlugin, BaseColdCommand>> getRootCommands() {
        return Collections.singletonList(plugin -> new BaseCommand((MoneyPouchDeluxe) plugin));
    }

    @Override
    public void reload() {
        MoneyPouchDeluxe plugin = (MoneyPouchDeluxe) this.coldPlugin;
        this.wrappers.add(new ShortcutCommandWrapper(plugin, new BaseCommand(plugin)));
        this.wrappers.forEach(ColdCommandWrapper::register);
    }

    @Override
    public void disable() {
        this.wrappers.forEach(ColdCommandWrapper::unregister);
        this.wrappers.clear();
    }

    @Override
    public List<ColdCommandWrapper> getActiveCommands() {
        return Collections.unmodifiableList(this.wrappers);
    }

}
