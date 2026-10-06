package dev.padrewin.moneypouchdeluxe.utils;

import dev.padrewin.colddev.utils.HexUtils;
import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    /**
     * A gradient or rainbow tag directly followed by another colour: the gradient would cover nothing.
     * HexUtils doesn't see a colour right after the tag as the end of the gradient, so it would
     * colour that code letter by letter (showing e.g. "&a" in the message) instead of using it.
     */
    private static final Pattern EMPTY_GRADIENT = Pattern.compile(
            "(?:<(?:gradient|g)(?:#\\d+)?(?::#(?:[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,}(?::(?:l|L|loop))?>"
                    + "|<(?:rainbow|r)(?:#\\d+)?(?::\\d*\\.?\\d+)?(?::\\d*\\.?\\d+)?(?::(?:l|L|loop))?>)"
                    + "(?=&[0-9a-fA-Fr]|&#[0-9a-fA-F]{6}|§|<#[0-9a-fA-F]{6}>|\\{#[0-9a-fA-F]{6}}|<(?:gradient|g|rainbow|r)[#:>])");
    private static final Pattern GLYPH = Pattern.compile("<glyph:[a-zA-Z0-9_.\\-]+>");

    public static String color(String text) {
        if (text == null) {
            return null;
        }
        String withoutEmptyGradients = EMPTY_GRADIENT.matcher(text).replaceAll("");
        return repairGlyphs(HexUtils.colorify(withoutEmptyGradients));
    }

    /**
     * A gradient puts a colour code before every letter, so a {@code <glyph:id>} tag inside one comes out
     * as {@code <§x..g§x..l§x..y...>}, which Nexo can't find. The codes inside the tag are taken out,
     * and the last one is put back after it so the gradient carries on.
     */
    static String repairGlyphs(String text) {
        if (text.indexOf('<') < 0 || text.indexOf('§') < 0) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '<') {
                StringBuilder tag = new StringBuilder("<");
                StringBuilder codes = new StringBuilder();
                int j = i + 1;
                while (j < text.length() && tag.length() < 100) {
                    char next = text.charAt(j);
                    if (next == '§' && j + 1 < text.length()) {
                        codes.append(next).append(text.charAt(j + 1));
                        j += 2;
                        continue;
                    }
                    if (next == '<') {
                        break;
                    }
                    tag.append(next);
                    j++;
                    if (next == '>') {
                        break;
                    }
                }
                if (codes.length() > 0 && GLYPH.matcher(tag).matches()) {
                    result.append(tag).append(lastColor(codes.toString()));
                    i = j;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * @return the last colour in a run of codes (a hex colour is §x followed by six §digit codes),
     * with the formatting codes after it
     */
    private static String lastColor(String codes) {
        int hex = codes.lastIndexOf("§x");
        if (hex >= 0) {
            return codes.substring(hex);
        }
        return codes.substring(Math.max(0, codes.length() - 2));
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
