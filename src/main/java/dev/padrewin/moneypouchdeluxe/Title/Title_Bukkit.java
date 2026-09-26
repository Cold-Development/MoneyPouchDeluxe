package dev.padrewin.moneypouchdeluxe.Title;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.entity.Player;

public class Title_Bukkit implements Title {

    @Override
    public void sendTitle(Player player, String message, String submessage) {
        Text.sendTitle(player, message, submessage, 0, 50, 20);
    }
}
