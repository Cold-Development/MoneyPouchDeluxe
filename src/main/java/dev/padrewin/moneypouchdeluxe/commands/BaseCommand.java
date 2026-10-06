package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.HelpCommand;
import dev.padrewin.colddev.command.PrimaryCommand;
import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.ColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.manager.LocaleManager;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Optional;

/**
 * /moneypouchdeluxe (aliases mp, cp, moneypouch, mpa, cpa): every subcommand lives in its own class.
 * The name and aliases can be changed in commands/moneypouchdeluxe.yml.
 */
public class BaseCommand extends PrimaryCommand {

    private final MoneyPouchDeluxe plugin;

    public BaseCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Without a subcommand: the help menu, or "no permission" for someone who can't use any of them.
     */
    @ColdExecutable
    @Override
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        boolean canUseAny = this.getSubCommands().stream()
                .anyMatch(command -> !(command instanceof HelpCommand) && command.canUse(sender));
        if (!canUseAny) {
            this.plugin.getManager(LocaleManager.class).sendMessage(sender, "no-permission");
            return;
        }
        this.findCommand("help").ifPresent(help -> help.invoke(context));
    }

    public Collection<ColdCommand> getSubCommands() {
        return ((Argument.SubCommandArgument) this.getCommandArguments().get(0)).subCommands();
    }

    /**
     * @param name a subcommand's current name or alias (both can be changed in the commands folder)
     */
    public Optional<ColdCommand> findCommand(String name) {
        return this.getSubCommands().stream()
                .filter(command -> command.getName().equalsIgnoreCase(name)
                        || command.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)))
                .findFirst();
    }

    public Optional<GiveCommand> getGiveCommand() {
        return this.getSubCommands().stream()
                .filter(GiveCommand.class::isInstance)
                .map(GiveCommand.class::cast)
                .findFirst();
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("moneypouchdeluxe")
                .aliases("moneypouch", "mp", "cp", "mpa", "cpa", "moneypouchadmin")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new EconomiesCommand(this.plugin),
                                new GiveCommand(this.plugin),
                                new HelpCommand(this.plugin, this),
                                new ListCommand(this.plugin),
                                new ReloadCommand(this.plugin)
                        ))
                .build();
    }

}
