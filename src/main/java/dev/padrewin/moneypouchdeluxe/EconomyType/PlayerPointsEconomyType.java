package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

public class PlayerPointsEconomyType extends EconomyType {

    private final MoneyPouchDeluxe plugin;

    public PlayerPointsEconomyType(MoneyPouchDeluxe plugin, String prefix, String suffix) {
        super(prefix, suffix);
        this.plugin = plugin;
    }

    @Override
    public void processPayment(Player player, long amount) {
        PlayerPointsAPI playerPointsAPI = plugin.getPlayerPointsAPI();
        if (playerPointsAPI != null) {
            playerPointsAPI.give(player.getUniqueId(), (int) amount);
        } else {
            plugin.getLogger().warning("PlayerPoints API is not available. Could not process payment.");
        }
    }

    @Override
    public boolean doTransaction(Player player, long amount) {
        PlayerPointsAPI playerPointsAPI = plugin.getPlayerPointsAPI();
        if (playerPointsAPI != null) {
            if (playerPointsAPI.look(player.getUniqueId()) >= amount) {
                playerPointsAPI.take(player.getUniqueId(), (int) amount);
                return true;
            }
        } else {
            plugin.getLogger().warning("PlayerPoints API is not available. Could not process transaction.");
        }
        return false;
    }

    @Override
    public String toString() {
        return "PlayerPointsEconomyType";
    }
}
