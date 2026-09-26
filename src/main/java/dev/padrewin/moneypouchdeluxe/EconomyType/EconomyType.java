package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.entity.Player;

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
     * Process the reward for a player when they open a MoneyPouch.
     *
     * @param player the player to receive the reward
     * @param amount the reward amount (prize)
     */
    public abstract void processPayment(Player player, long amount);

    /**
     * Fills in the reward placeholders of a message: %prize%, %prefix%, %suffix% and %economy%.
     */
    public String applyPlaceholders(String message, String prize) {
        return message
                .replace("%prefix%", Text.color(prefix))
                .replace("%suffix%", Text.color(suffix))
                .replace("%economy%", Text.color(name))
                .replace("%prize%", prize);
    }

    public abstract String toString();

}
