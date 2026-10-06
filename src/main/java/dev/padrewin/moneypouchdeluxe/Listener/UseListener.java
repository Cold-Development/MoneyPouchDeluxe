package dev.padrewin.moneypouchdeluxe.Listener;

import java.util.concurrent.ConcurrentHashMap;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.*;
import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import dev.padrewin.moneypouchdeluxe.manager.DataManager;
import dev.padrewin.moneypouchdeluxe.manager.LocaleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import dev.padrewin.colddev.scheduler.task.ScheduledTask;
import dev.padrewin.colddev.utils.StringPlaceholders;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class UseListener implements Listener {

    protected final MoneyPouchDeluxe plugin;
    protected final Set<UUID> opening = ConcurrentHashMap.newKeySet();

    public UseListener(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        String pouchId = getPouchId(itemInHand);
        if (pouchId == null) {
            return;
        }

        // Pouches are never placed or used as their normal item
        event.setCancelled(true);

        Pouch pouch = plugin.getPouch(pouchId);
        if (pouch == null) {
            // A pouch whose tier was removed from pouches.yml
            plugin.getManager(LocaleManager.class).sendMessage(player, "invalid-pouch");
            return;
        }
        if (!canOpen(player, pouch)) {
            return;
        }

        // Sneaking opens the whole stack in one go
        int count = 1;
        if (player.isSneaking() && plugin.getConfig().getBoolean("open-whole-stack-sneaking", true)) {
            count = itemInHand.getAmount();
        }

        usePouch(player, pouch, count);

        if (itemInHand.getAmount() > count) {
            itemInHand.setAmount(itemInHand.getAmount() - count);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        player.updateInventory();
    }

    /**
     * @return the id of the pouch this item is, or null if it isn't a pouch
     */
    protected String getPouchId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(Pouch.getIdKey(), PersistentDataType.STRING);
    }

    /**
     * Checks shared by every way of opening a pouch. Tells the player why when it can't be opened.
     */
    protected boolean canOpen(Player player, Pouch pouch) {
        if (opening.contains(player.getUniqueId())) {
            plugin.getManager(LocaleManager.class).sendMessage(player, "already-opening");
            return false;
        }

        String permission = pouch.getPermission();
        if (pouch.isPermissionRequired() && (permission == null || !player.hasPermission(permission))) {
            plugin.getManager(LocaleManager.class).sendMessage(player, "pouch-no-permission");
            return false;
        }
        return true;
    }

    /**
     * Groups the digits with the configured separator (e.g. 1,924,281 or 1.924.281),
     * independent of the server's locale.
     */
    public static String formatNumber(long value, String separator) {
        if (separator == null || separator.isEmpty()) {
            return String.valueOf(value);
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(separator.charAt(0));
        return new DecimalFormat("#,###", symbols).format(value);
    }

    /**
     * Plays a sound by its old enum name ({@code BLOCK_CHEST_OPEN}) or its key ({@code block.chest.open},
     * also custom resource-pack sounds). Uses the key-based method, which exists on every version,
     * since Sound stopped being an enum in 1.21.3.
     */
    protected void playSound(Player player, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = name.trim();
        if (!key.contains(".") && !key.contains(":")) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(key.toUpperCase(Locale.ROOT)), 3, 1);
                return;
            } catch (Throwable ignored) {
                // unknown name, or Sound isn't an enum on this version: try it as a key below
            }
            key = key.toLowerCase(Locale.ROOT).replace('_', '.');
        }
        try {
            player.playSound(player.getLocation(), key, 3, 1);
        } catch (Throwable ignored) { }
    }

    /**
     * Opens {@code count} pouches at once: each one draws its own amount from the range, and the
     * player gets the total in a single payment, with one reveal and one message.
     */
    protected void usePouch(Player player, Pouch pouch, int count) {
        long total = 0;
        for (int i = 0; i < count; i++) {
            // + 1: nextLong's upper bound is exclusive, and the configured maximum must be winnable
            long random = ThreadLocalRandom.current().nextLong(pouch.getMinRange(), pouch.getMaxRange() + 1);
            total = total > Long.MAX_VALUE - random ? Long.MAX_VALUE : total + random;
        }
        playSound(player, plugin.getConfig().getString("pouches.sound.opensound"));

        new PaymentRunnable(plugin, total, count, player, pouch)
                .start(10, Math.max(1, plugin.getConfig().getInt("pouches.title.speed-in-tick", 10)));
    }

    private class PaymentRunnable implements Runnable {

        private final Player player;
        private final Pouch pouch;
        private final long payment;
        private final int count;

        private final String prefixColour;
        private final String suffixColour;
        private final String revealColour;
        private final String obfuscateColour;
        private final String obfuscateDigitChar;
        private final String obfuscateDelimiterChar;
        private final String separator;
        private final boolean delimiter;
        private final boolean revealComma;
        private final String number;
        private final boolean reversePouchReveal;

        private int position;
        private boolean paid;
        private ScheduledTask task;

        public PaymentRunnable(MoneyPouchDeluxe plugin, long payment, int count, Player player, Pouch pouch) {
            opening.add(player.getUniqueId());

            this.player = player;
            this.payment = payment;
            this.count = count;
            this.pouch = pouch;

            this.prefixColour = Text.color(
                    plugin.getConfig().getString("pouches.title.prefix-colour", ""));
            this.suffixColour = Text.color(
                    plugin.getConfig().getString("pouches.title.suffix-colour", ""));
            this.revealColour = Text.color(
                    plugin.getConfig().getString("pouches.title.reveal-colour", ""));
            this.obfuscateColour = Text.color(
                    plugin.getConfig().getString("pouches.title.obfuscate-colour", ""));
            this.obfuscateDigitChar = plugin.getConfig().getString("pouches.title.obfuscate-digit-char", "#");
            this.separator = plugin.getConfig().getString("pouches.title.format.separator", ",");
            this.obfuscateDelimiterChar = plugin.getConfig().getString("pouches.title.obfuscate-format-char", separator);
            this.delimiter = plugin.getConfig().getBoolean("pouches.title.format.enabled", false);
            this.revealComma = plugin.getConfig().getBoolean("pouches.title.format.reveal-comma", false);
            this.number = delimiter ? formatNumber(payment, separator) : String.valueOf(payment);
            this.reversePouchReveal = plugin.getConfig().getBoolean("reverse-pouch-reveal");
        }

        public void start(long delay, long period) {
            // Runs on the player's own thread (Folia). If Folia drops the task because the player
            // left mid-reveal, stop() still pays them, just like the online check below does on Paper.
            this.task = plugin.getScheduler().runTaskTimerAtEntity(player, this, this::stop, delay, period);
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                stop();
                return;
            }

            playSound(player, plugin.getConfig().getString("pouches.sound.revealsound"));
            String prefix = prefixColour + Text.color(pouch.getEconomyType().getPrefix());
            StringBuilder viewedTitle = new StringBuilder();
            String suffix = suffixColour + Text.color(pouch.getEconomyType().getSuffix());
            for (int i = 0; i < position; i++) {
                if (reversePouchReveal) {
                    viewedTitle.insert(0, number.charAt(number.length() - i - 1)).insert(0, revealColour);
                } else {
                    viewedTitle.append(revealColour).append(number.charAt(i));
                }
                if ((i == (position - 1)) && (position != number.length())
                        && (reversePouchReveal
                        ? (revealComma && isSeparator(number.charAt(number.length() - i - 1)))
                        : (revealComma && isSeparator(number.charAt(i + 1))))) {
                    position++;
                }
            }
            for (int i = position; i < number.length(); i++) {
                if (reversePouchReveal) {
                    char at = number.charAt(number.length() - i - 1);
                    if (isSeparator(at)) {
                        if (revealComma) {
                            viewedTitle.insert(0, at).insert(0, revealColour);
                        } else viewedTitle.insert(0, obfuscateDelimiterChar).insert(0, ChatColor.MAGIC).insert(0, obfuscateColour);
                    } else viewedTitle.insert(0, obfuscateDigitChar).insert(0, ChatColor.MAGIC).insert(0, obfuscateColour);;
                } else {
                    char at = number.charAt(i);
                    if (isSeparator(at)) {
                        if (revealComma) viewedTitle.append(revealColour).append(at);
                        else viewedTitle.append(obfuscateColour).append(ChatColor.MAGIC).append(obfuscateDelimiterChar);
                    } else viewedTitle.append(obfuscateColour).append(ChatColor.MAGIC).append(obfuscateDigitChar);
                }
            }
            Text.sendTitle(player, prefix + viewedTitle + suffix,
                    Text.color(plugin.getConfig().getString("pouches.title.subtitle", "")), 0, 50, 20);
            position++;

            if (position > number.length()) {
                stop();
            }
        }

        private boolean isSeparator(char c) {
            return !Character.isDigit(c);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
            pay();
        }

        public void pay() {
            // stop() can be reached both from the last reveal tick and from Folia retiring the task
            if (paid) return;
            this.paid = true;

            opening.remove(player.getUniqueId());

            CompletableFuture<Void> result;
            try {
                result = pouch.getEconomyType().processPayment(player, payment);
            } catch (Throwable t) {
                result = CompletableFuture.failedFuture(t);
            }
            // Some economies finish later (a console command runs on the global thread on Folia), so
            // the player is only told once the outcome is known: the prize, or the error, never both
            result.whenComplete((ignored, error) -> {
                if (error != null) {
                    logFailure(unwrap(error));
                } else {
                    // Counted even if the player left meanwhile: they were paid
                    plugin.getManager(DataManager.class).record(player.getUniqueId(), pouch.getId(),
                            plugin.getEconomyId(pouch.getEconomyType()), count, payment);
                }
                runForPlayer(() -> finish(error == null));
            });
        }

        private void finish(boolean success) {
            StringPlaceholders placeholders = StringPlaceholders.builder()
                    .addAll(pouch.getEconomyType().placeholders(formatNumber(payment, separator)))
                    .add("amount", count)
                    .build();
            if (success) {
                playSound(player, plugin.getConfig().getString("pouches.sound.endsound"));
                plugin.getManager(LocaleManager.class).sendMessage(player,
                        count > 1 ? "prize-message-stack" : "prize-message", placeholders);
                return;
            }
            if (plugin.getConfig().getBoolean("error-handling.refund-pouch", false)) {
                plugin.giveOrDrop(player, pouch.getItemStack(), count);
            }
            plugin.getManager(LocaleManager.class).sendMessage(player, "reward-error", placeholders);
        }

        /**
         * Runs on the player's thread, right away if already on it. Skipped if the player left:
         * there's no one to message or refund (the failure itself has already been logged).
         */
        private void runForPlayer(Runnable action) {
            if (player.isOnline()) {
                plugin.runAtPlayer(player, action);
            }
        }

        private void logFailure(Throwable error) {
            if (!plugin.getConfig().getBoolean("error-handling.log-failed-transactions", true)) {
                return;
            }
            String message = "Failed to process payment from pouch '" + pouch.getId() + "' for player '"
                    + player.getName() + "' (" + player.getUniqueId() + ") amount " + payment + (count > 1 ? " (" + count + " pouches)" : "")
                    + " " + pouch.getEconomyType() + ": " + error.getMessage();
            if (error instanceof PaymentFailedException && error.getCause() == null) {
                plugin.getLogger().severe(message); // an expected failure: the reason is enough
            } else {
                plugin.getLogger().log(Level.SEVERE, message, error);
            }
        }

        private Throwable unwrap(Throwable error) {
            while (error instanceof CompletionException && error.getCause() != null) {
                error = error.getCause();
            }
            return error;
        }
    }
}