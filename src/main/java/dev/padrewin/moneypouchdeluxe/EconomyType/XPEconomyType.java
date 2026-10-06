package dev.padrewin.moneypouchdeluxe.EconomyType;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class XPEconomyType extends EconomyType {

    public XPEconomyType(String name, String prefix, String suffix) {
        super(name, prefix, suffix);
    }

    @Override
    public CompletableFuture<Void> processPayment(Player player, long amount) {
        if (!player.isOnline()) {
            return failed("player is offline");
        }
        if (amount > Integer.MAX_VALUE) {
            return failed("XP amount is too large (max " + Integer.MAX_VALUE + ")");
        }
        player.giveExp((int) amount);
        return paid();
    }


    @Override
    public String toString() {
        return "XP";
    }

}
