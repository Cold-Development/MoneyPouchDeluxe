package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Points through the PlayerPoints API, which tells whether the points were given.
 * <p>
 * Only created through {@link EconomyHooks} when PlayerPoints is installed, so its classes are
 * never loaded without it.
 */
public class PlayerPointsEconomyType extends EconomyType {

    public PlayerPointsEconomyType(String name, String prefix, String suffix) {
        super(name, prefix, suffix);
    }

    @Override
    public CompletableFuture<Void> processPayment(Player player, long amount) {
        if (amount > Integer.MAX_VALUE) {
            return failed("points amount is too large (max " + Integer.MAX_VALUE + ")");
        }
        try {
            if (!PlayerPoints.getInstance().getAPI().give(player.getUniqueId(), (int) amount)) {
                return failed("PlayerPoints refused to give the points");
            }
            return paid();
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(
                    new PaymentFailedException("PlayerPoints threw an error while giving points", t));
        }
    }

    @Override
    public String toString() {
        return "PlayerPoints";
    }

}
