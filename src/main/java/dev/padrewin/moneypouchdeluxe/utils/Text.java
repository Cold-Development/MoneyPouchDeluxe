package dev.padrewin.moneypouchdeluxe.utils;

import dev.padrewin.colddev.utils.HexUtils;
import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Single place where configured text is turned into what players see: colour codes first
 * (legacy {@code &a}, {@code &#RRGGBB}, {@code <#RRGGBB>}, gradients), then Nexo
 * {@code <glyph:id>} tags when Nexo is installed.
 * <p>
 * {@link #color(String)} only applies colours and leaves glyph tags in place, so placeholders can
 * still be replaced afterwards. Tags are turned into glyphs by the methods below, at the moment
 * the text reaches the player: a glyph only renders as its texture when it keeps its Nexo font,
 * which a plain string can't carry. Without Nexo the plain-string Bukkit methods are used.
 */
public final class Text {

    private Text() {
    }

    public static String color(String text) {
        if (text == null) {
            return null;
        }
        return HexUtils.colorify(text);
    }

    /**
     * Sends a message; an empty one (a message disabled in the config) is not sent at all.
     */
    public static void send(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (NexoHook.isEnabled()) {
            NexoHook.sendMessage(sender, message);
        } else {
            sender.sendMessage(message);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (NexoHook.isEnabled()) {
            NexoHook.sendTitle(player, title, subtitle == null ? "" : subtitle, fadeIn, stay, fadeOut);
        } else {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void setDisplayName(ItemMeta meta, String text) {
        if (NexoHook.isEnabled()) {
            NexoHook.setDisplayName(meta, color(text));
        } else {
            meta.setDisplayName(color(text));
        }
    }

    public static void setLore(ItemMeta meta, List<String> lines) {
        List<String> colored = new ArrayList<>(lines.size());
        for (String line : lines) {
            colored.add(color(line));
        }
        if (NexoHook.isEnabled()) {
            NexoHook.setLore(meta, colored);
        } else {
            meta.setLore(colored);
        }
    }

}
