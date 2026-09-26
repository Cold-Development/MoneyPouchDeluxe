package dev.padrewin.moneypouchdeluxe.hook;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.glyphs.Glyph;
import com.nexomc.nexo.items.ItemBuilder;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Soft hook into Nexo: renders {@code <glyph:id>} tags as Nexo glyph components wherever text
 * reaches a player (chat, titles, menus, items) and builds {@code nexo:<id>} custom items.
 * <p>
 * Nexo only runs on Paper, so the Paper-only component methods used here are safe to call
 * whenever {@link #isEnabled()} is true.
 * <p>
 * Every reference to a Nexo class lives in this file, so the class is only ever loaded
 * when Nexo is actually present on the server.
 */
public final class NexoHook {

    private static final Pattern GLYPH_PATTERN = Pattern.compile("<glyph:([a-zA-Z0-9_.\\-]+)>");

    private static Boolean enabled;

    private NexoHook() {
    }

    /**
     * @return true if Nexo is installed and enabled on this server
     */
    public static boolean isEnabled() {
        if (enabled == null)
            enabled = Bukkit.getPluginManager().isPluginEnabled("Nexo");
        return enabled;
    }

    /**
     * Called on plugin reload, in case Nexo was installed/removed in the meantime.
     */
    public static void reset() {
        enabled = null;
    }

    /**
     * Turns already-coloured (§) text into a component in which every {@code <glyph:id>} tag is
     * Nexo's own glyph component. The component keeps the glyph's font, which is what makes the
     * client draw the texture instead of a plain symbol; a plain string can't carry a font.
     */
    public static Component toComponent(String colored) {
        Matcher matcher = GLYPH_PATTERN.matcher(colored);
        TextComponent.Builder builder = Component.text();
        int last = 0;
        while (matcher.find()) {
            Component glyph = glyphComponent(matcher.group(1));
            if (glyph == null)
                continue; // unknown glyph: its tag stays in the surrounding text
            builder.append(legacySegment(colored, last, matcher.start()));
            builder.append(glyph);
            last = matcher.end();
        }
        builder.append(legacySegment(colored, last, colored.length()));
        return builder.build();
    }

    /**
     * Item names and lore are italic by default; legacy text never was, so keep it that way.
     */
    private static Component toItemComponent(String colored) {
        return toComponent(colored).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static void sendMessage(CommandSender sender, String colored) {
        sender.sendMessage(toComponent(colored));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.showTitle(net.kyori.adventure.title.Title.title(toComponent(title), toComponent(subtitle),
                net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))));
    }

    public static Inventory createInventory(InventoryHolder holder, int size, String colored) {
        return Bukkit.createInventory(holder, size, toComponent(colored));
    }

    public static void setDisplayName(ItemMeta meta, String colored) {
        meta.displayName(toItemComponent(colored));
    }

    public static void setLore(ItemMeta meta, List<String> colored) {
        List<Component> lore = new ArrayList<>(colored.size());
        for (String line : colored)
            lore.add(toItemComponent(line));
        meta.lore(lore);
    }

    public static void copyNameAndLore(ItemMeta from, ItemMeta to) {
        to.displayName(from.displayName());
        to.lore(from.lore());
    }

    /**
     * A slice of the text, starting with the colours that were active where the slice begins,
     * since every slice is deserialized on its own.
     */
    private static Component legacySegment(String colored, int start, int end) {
        String segment = ChatColor.getLastColors(colored.substring(0, start)) + colored.substring(start, end);
        return LegacyComponentSerializer.legacySection().deserialize(segment);
    }

    private static Component glyphComponent(String id) {
        try {
            Glyph glyph = NexoPlugin.instance().fontManager().glyphFromID(id);
            return glyph != null ? glyph.glyphComponent() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Builds the Nexo custom item with the given id (the part after {@code nexo:} in the config).
     *
     * @return the item, or null if Nexo is missing or doesn't know this id (yet)
     */
    public static ItemStack buildItem(String id) {
        if (!isEnabled())
            return null;
        try {
            ItemBuilder builder = NexoItems.itemFromId(id);
            return builder != null ? builder.build() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Nexo loads its items after every plugin is enabled (and again on /nexo reload), so pouches
     * using {@code nexo:} items are rebuilt once Nexo reports its items are ready.
     */
    public static void registerItemsLoadedListener(MoneyPouchDeluxe plugin) {
        if (isEnabled())
            Bukkit.getPluginManager().registerEvents(new ItemsLoadedListener(plugin), plugin);
    }

    private static final class ItemsLoadedListener implements Listener {

        private final MoneyPouchDeluxe plugin;

        private ItemsLoadedListener(MoneyPouchDeluxe plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onItemsLoaded(NexoItemsLoadedEvent event) {
            plugin.getScheduler().runTask(plugin::reload);
        }
    }

}
