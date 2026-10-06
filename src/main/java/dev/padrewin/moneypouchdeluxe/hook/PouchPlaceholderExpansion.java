package dev.padrewin.moneypouchdeluxe.hook;

import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.manager.DataManager;
import dev.padrewin.moneypouchdeluxe.manager.DataManager.PouchEconomy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * PlaceholderAPI placeholders, for the player they're asked for:
 * <ul>
 *     <li>{@code %moneypouch_opened%}: pouches opened</li>
 *     <li>{@code %moneypouch_opened_<pouch>%}: pouches of one kind opened</li>
 *     <li>{@code %moneypouch_won_<economy>%}: amount won in an economy (e.g. won_vault, won_xp)</li>
 *     <li>{@code %moneypouch_won_pouch_<pouch>%}: amount won from one kind of pouch</li>
 * </ul>
 * Add {@code _formatted} to any of them for the number with the separator from config.yml.
 * Only created when PlaceholderAPI is installed.
 */
public class PouchPlaceholderExpansion extends PlaceholderExpansion {

    private final MoneyPouchDeluxe plugin;

    public PouchPlaceholderExpansion(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "moneypouch";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", this.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        boolean formatted = params.endsWith("_formatted");
        String name = formatted ? params.substring(0, params.length() - "_formatted".length()) : params;

        // Still loading: 0 for now, the real value shows up on the next update
        Map<PouchEconomy, long[]> stats = this.plugin.getManager(DataManager.class).getStats(player.getUniqueId());
        if (stats == null) {
            stats = Collections.emptyMap();
        }

        long value;
        if (name.equals("opened")) {
            value = sum(stats, key -> true, values -> values[0]);
        } else if (name.startsWith("opened_")) {
            String pouch = name.substring("opened_".length());
            value = sum(stats, key -> key.pouch().equalsIgnoreCase(pouch), values -> values[0]);
        } else if (name.startsWith("won_pouch_")) {
            String pouch = name.substring("won_pouch_".length());
            value = sum(stats, key -> key.pouch().equalsIgnoreCase(pouch), values -> values[1]);
        } else if (name.startsWith("won_")) {
            String economy = name.substring("won_".length());
            value = sum(stats, key -> key.economy().equalsIgnoreCase(economy), values -> values[1]);
        } else {
            return null;
        }

        if (formatted) {
            return UseListener.formatNumber(value, this.plugin.getConfig().getString("pouches.title.format.separator", ","));
        }
        return String.valueOf(value);
    }

    private static long sum(Map<PouchEconomy, long[]> stats, Predicate<PouchEconomy> filter, ToLongFunction<long[]> field) {
        long total = 0;
        for (Map.Entry<PouchEconomy, long[]> entry : stats.entrySet()) {
            if (filter.test(entry.getKey())) {
                total += field.applyAsLong(entry.getValue());
            }
        }
        return total;
    }

}
