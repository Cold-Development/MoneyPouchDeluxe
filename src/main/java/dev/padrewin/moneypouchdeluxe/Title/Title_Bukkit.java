package dev.padrewin.moneypouchdeluxe.Title;

import org.bukkit.entity.Player;

public class Title_Bukkit implements Title {

    @Override
    public void sendTitle(Player player, String message, String submessage) {
        player.sendTitle(message, submessage, 0, 50, 20);
    }
}
