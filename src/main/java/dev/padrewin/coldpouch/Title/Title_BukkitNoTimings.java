package dev.padrewin.coldpouch.Title;

import org.bukkit.entity.Player;

public class Title_BukkitNoTimings implements Title {
    @Override
    public void sendTitle(Player player, String message, String submessage) {
        player.sendTitle(message, submessage);
    }

}
