package dev.padrewin.moneypouchdeluxe.commands;

import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import dev.padrewin.moneypouchdeluxe.commands.arguments.StringSuggestingArgumentHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /mp give &lt;pouch&gt; [player|*] [amount]. Also reachable as /mp &lt;pouch&gt; [player] [amount],
 * the form older versions used and owners have in their crates, NPCs and scripts.
 */
public class GiveCommand extends BasePouchCommand {

    /**
     * A full player inventory of pouches; anything above that would mostly end up on the ground.
     */
    public static final int MAX_AMOUNT = 36 * 64;
    public static final String GIVE_ALL_PERMISSION = "moneypouch.admin.giveall";

    public GiveCommand(MoneyPouchDeluxe plugin) {
        super(plugin);
    }

    @ColdExecutable
    public void execute(CommandContext context, String pouchId, String target, String amountInput) {
        CommandSender sender = context.getSender();

        Pouch pouch = this.plugin.getPouch(pouchId);
        if (pouch == null) {
            this.localeManager.sendMessage(sender, "pouch-not-found", StringPlaceholders.of("pouch", pouchId));
            return;
        }

        int amount = 1;
        if (amountInput != null) {
            amount = parseAmount(amountInput);
            if (amount < 1) {
                this.localeManager.sendMessage(sender, "invalid-amount",
                        StringPlaceholders.of("amount", amountInput, "max", MAX_AMOUNT));
                return;
            }
        }
        String item = this.plugin.getPouchName(pouch);

        if ("*".equals(target)) {
            if (!sender.hasPermission(GIVE_ALL_PERMISSION)) {
                this.localeManager.sendMessage(sender, "no-permission");
                return;
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                this.give(sender, online, pouch, amount, false);
            }
            this.plugin.getTransactionLog().logGiveAll(sender.getName(), Bukkit.getOnlinePlayers().size(), pouch.getId(), amount);
            this.localeManager.sendMessage(sender, "command-give-all-success",
                    StringPlaceholders.of("item", item, "amount", amount));
            return;
        }

        Player player;
        if (target != null) {
            player = Bukkit.getPlayerExact(target);
            if (player == null) {
                this.localeManager.sendMessage(sender, "player-not-found", StringPlaceholders.of("player", target));
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            this.localeManager.sendMessage(sender, "player-required");
            return;
        }

        this.give(sender, player, pouch, amount, true);
        this.plugin.getTransactionLog().logGive(sender.getName(), player.getName(), player.getUniqueId(), pouch.getId(), amount);
        this.localeManager.sendMessage(sender, "command-give-success",
                StringPlaceholders.of("player", player.getName(), "item", item, "amount", amount));
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
     * Inventories are changed on the player's own thread (Folia).
     *
     * @param tellSender whether the sender hears about a full inventory (not for every player of a give-all)
     */
    private void give(CommandSender sender, Player target, Pouch pouch, int amount, boolean tellSender) {
        String item = this.plugin.getPouchName(pouch);
        this.plugin.runAtPlayer(target, () -> {
            if (!target.isOnline()) {
                return;
            }
            if (this.plugin.giveOrDrop(target, pouch.getItemStack(), amount)) {
                if (tellSender && sender != target) {
                    this.localeManager.sendMessage(sender, "command-give-full-inventory",
                            StringPlaceholders.of("player", target.getName()));
                }
                this.localeManager.sendMessage(target, "player-full-inv");
            }
            this.localeManager.sendMessage(target, "receive-item",
                    StringPlaceholders.of("player", target.getName(), "item", item, "amount", amount));
        });
    }

    private List<String> suggestPouches(CommandContext context) {
        List<String> ids = new ArrayList<>();
        for (Pouch pouch : this.plugin.getPouches()) {
            ids.add(pouch.getId());
        }
        return ids;
    }

    private List<String> suggestTargets(CommandContext context) {
        CommandSender sender = context.getSender();
        List<String> names = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!(sender instanceof Player) || ((Player) sender).canSee(online)) {
                names.add(online.getName());
            }
        }
        if (sender.hasPermission(GIVE_ALL_PERMISSION)) {
            names.add("*");
        }
        return names;
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("give")
                .descriptionKey("command-give-description")
                .permission(ADMIN_PERMISSION)
                .arguments(ArgumentsDefinition.builder()
                        .required("pouch", new StringSuggestingArgumentHandler(this::suggestPouches))
                        .optional("player", new StringSuggestingArgumentHandler(this::suggestTargets))
                        // Only after a player: otherwise the framework would also suggest the amounts
                        // while the player is still being typed
                        .optional("amount", new StringSuggestingArgumentHandler("1", "16", "64"),
                                context -> context.get("player") != null)
                        .build())
                .build();
    }

}
