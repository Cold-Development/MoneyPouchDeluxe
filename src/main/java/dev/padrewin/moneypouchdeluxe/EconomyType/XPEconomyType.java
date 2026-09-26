package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import org.bukkit.entity.Player;

public class XPEconomyType extends EconomyType {

    public XPEconomyType(String name, String prefix, String suffix) {
        super(name, prefix, suffix);
    }

    @Override
    public void processPayment(Player player, long amount) {
        if (!player.isOnline()) {
            throw new PaymentFailedException("Player is offline!", new AssertionError("Player is offline!"));
        }
        int xp;
        try {
            xp = Integer.parseInt(String.valueOf(amount));
        } catch (NumberFormatException ex) {
            throw new PaymentFailedException("XP value is too large!");
        }
        player.giveExp(xp);
    }


    @Override
    public String toString() {
        return "XP";
    }

}
