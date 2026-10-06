package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Money through Vault, straight to whichever economy plugin provides it (EssentialsX, CMI, ...).
 * Unlike a command, Vault tells whether the deposit went through.
 * <p>
 * Only created through {@link EconomyHooks} when Vault is installed, so the Vault classes are never
 * loaded without it.
 */
public class VaultEconomyType extends EconomyType {

    public VaultEconomyType(String name, String prefix, String suffix) {
        super(name, prefix, suffix);
    }

    /**
     * The economy is looked up on every payment rather than once: economy plugins can register
     * with Vault late, or be swapped by a reload.
     */
    static Economy getEconomy() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        return registration != null ? registration.getProvider() : null;
    }

    @Override
    public CompletableFuture<Void> processPayment(Player player, long amount) {
        Economy economy = getEconomy();
        if (economy == null) {
            return failed("no economy plugin is registered with Vault");
        }
        try {
            EconomyResponse response = economy.depositPlayer(player, amount);
            if (response == null || !response.transactionSuccess()) {
                String reason = response == null ? "no response" : response.errorMessage;
                return failed(economy.getName() + " refused the deposit: " + reason);
            }
            return paid();
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(
                    new PaymentFailedException(economy.getName() + " threw an error while depositing", t));
        }
    }

    @Override
    public String toString() {
        Economy economy = getEconomy();
        return "Vault (" + (economy != null ? economy.getName() : "no economy plugin") + ")";
    }

}
