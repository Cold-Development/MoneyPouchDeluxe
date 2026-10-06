package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * An economy paid out by running a console command, so any plugin with a "give" command works
 * without MoneyPouchDeluxe depending on it.
 * <p>
 * A command can only report that it ran, not that the plugin behind it actually gave anything, so
 * this only detects a command that doesn't exist or throws. Vault and PlayerPoints have real hooks
 * ({@link VaultEconomyType}, {@link PlayerPointsEconomyType}) that do detect a failed transaction.
 */
public class CustomEconomyType extends EconomyType {

    private final String command;

    public CustomEconomyType(String name, String prefix, String suffix, String command) {
        super(name, prefix, suffix);
        this.command = command;
    }

    @Override
    public CompletableFuture<Void> processPayment(Player player, long amount) {
        String resolved = command.replace("%player%", player.getName()).replace("%prize%", String.valueOf(amount));
        CompletableFuture<Void> result = new CompletableFuture<>();
        // Console commands run on the global region thread on Folia
        MoneyPouchDeluxe.getInstance().getScheduler().executeGlobal(() -> {
            try {
                if (Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), resolved)) {
                    result.complete(null);
                } else {
                    // The command doesn't exist (its plugin missing?), so nothing was given
                    result.completeExceptionally(new PaymentFailedException("unknown command: /" + resolved));
                }
            } catch (Throwable t) {
                result.completeExceptionally(new PaymentFailedException("command threw an error: /" + resolved, t));
            }
        });
        return result;
    }


    @Override
    public String toString() {
        return "Command (/" + command + ")";
    }

}
