package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import dev.padrewin.colddev.utils.StringPlaceholders;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public abstract class EconomyType {

    private final String name;
    private final String prefix;
    private final String suffix;

    /**
     * @param name display name, shown by the %economy% placeholder
     */
    public EconomyType(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Gives the reward to a player who opened a pouch. Called on the player's thread (the main
     * thread on Paper/Spigot, the player's region thread on Folia).
     * <p>
     * The returned future completes once the reward is actually given, or completes exceptionally
     * (usually with a {@link PaymentFailedException}) if it wasn't, so the caller can tell the
     * player the right thing: the prize message or the reward error, never both.
     *
     * @param player the player to receive the reward
     * @param amount the reward amount (prize)
     */
    public abstract CompletableFuture<Void> processPayment(Player player, long amount);

    /**
     * The reward placeholders of prize-message and reward-error: %prize%, %prefix%, %suffix% and %economy%.
     */
    public StringPlaceholders placeholders(String prize) {
        return StringPlaceholders.of("prize", prize, "prefix", prefix, "suffix", suffix, "economy", name);
    }

    protected static CompletableFuture<Void> paid() {
        return CompletableFuture.completedFuture(null);
    }

    protected static CompletableFuture<Void> failed(String reason) {
        return CompletableFuture.failedFuture(new PaymentFailedException(reason));
    }

    public abstract String toString();

}
