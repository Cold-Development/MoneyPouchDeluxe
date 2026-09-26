package dev.padrewin.moneypouchdeluxe.Listener;

import java.util.concurrent.ConcurrentHashMap;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import dev.padrewin.moneypouchdeluxe.Title.Title_Other;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import dev.padrewin.colddev.scheduler.task.ScheduledTask;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
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
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            onRightClickInMainHand(player, event);
        }
    }

    protected void onRightClickInMainHand(Player player, Cancellable event) {
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            return;
        }

        boolean pouchMatched = false;

        String itemPouchId = getPouchId(itemInHand);

        for (Pouch pouch : plugin.getPouches()) {
            String pouchId = pouch.getId();

            if (itemPouchId != null && itemPouchId.equals(pouchId)) {
                event.setCancelled(true);
                if (!canOpen(player, pouch)) {
                    return;
                }
                usePouch(player, pouch);
                removeOrReduceItem(player);
                pouchMatched = true;
                break;
            }
        }

        if (!pouchMatched) {
            //Bukkit.getLogger().info("No matching pouch found.");
        }
    }

    String getPouchId(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            return meta.getPersistentDataContainer().get(new NamespacedKey(MoneyPouchDeluxe.getInstance(), "pouch-id"), PersistentDataType.STRING);
        }
        return null;
    }

    private void removeOrReduceItem(Player player) {
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().removeItem(itemInHand);
        }
        player.updateInventory();
    }

    private boolean compareSkullTextures(SkullMeta skullMeta1, SkullMeta skullMeta2) {
        try {
            Field profileField = skullMeta1.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile1 = (GameProfile) profileField.get(skullMeta1);
            GameProfile profile2 = (GameProfile) profileField.get(skullMeta2);

            if (profile1 == null || profile2 == null) {
                return false;
            }

            Property property1 = profile1.getProperties().get("textures").iterator().next();
            Property property2 = profile2.getProperties().get("textures").iterator().next();

            return property1 != null && property2 != null && property1.equals(property2);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks shared by every way of opening a pouch. Tells the player why when it can't be opened.
     */
    protected boolean canOpen(Player player, Pouch pouch) {
        if (opening.contains(player.getUniqueId())) {
            Text.send(player, plugin.getMessage(MoneyPouchDeluxe.Message.ALREADY_OPENING));
            return false;
        }

        String permission = pouch.getPermission();
        if (pouch.isPermissionRequired() && (permission == null || !player.hasPermission(permission))) {
            Text.send(player, plugin.getMessage(MoneyPouchDeluxe.Message.NO_PERMISSION));
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

    protected void playSound(Player player, String name) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name), 3, 1);
        } catch (Exception ignored) { }
    }

    protected void usePouch(Player player, Pouch pouch) {
        // + 1: nextLong's upper bound is exclusive, and the configured maximum must be winnable
        long random = ThreadLocalRandom.current().nextLong(pouch.getMinRange(), pouch.getMaxRange() + 1);
        playSound(player, plugin.getConfig().getString("pouches.sound.opensound"));

        PaymentRunnable paymentRunnable = new PaymentRunnable(plugin, random, player, pouch);
        if (plugin.getTitleHandle() instanceof Title_Other) {
            paymentRunnable.pay();
        } else {
            paymentRunnable.start(10, plugin.getConfig().getInt("pouches.title.speed-in-tick"));
        }
    }

    private class PaymentRunnable implements Runnable {

        private final Player player;
        private final Pouch pouch;
        private final long payment;

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

        public PaymentRunnable(MoneyPouchDeluxe plugin, long payment, Player player, Pouch pouch) {
            opening.add(player.getUniqueId());

            this.player = player;
            this.payment = payment;
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
            plugin.getTitleHandle().sendTitle(player, prefix + viewedTitle + suffix,
                    Text.color(plugin.getConfig().getString("pouches.title.subtitle", "")));
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
            if (paid) throw new IllegalStateException("player already paid!");
            this.paid = true;

            opening.remove(player.getUniqueId());

            try {
                pouch.getEconomyType().processPayment(player, payment);

                if (player.isOnline()) {
                    playSound(player, plugin.getConfig().getString("pouches.sound.endsound"));
                    Text.send(player, pouch.getEconomyType().applyPlaceholders(
                            plugin.getMessage(MoneyPouchDeluxe.Message.PRIZE_MESSAGE), formatNumber(payment, separator)));
                }

            } catch (Throwable t) {
                if (plugin.getConfig().getBoolean("error-handling.log-failed-transactions", true)) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Failed to process payment from pouch '" + pouch.getId()
                                    + "' for player '" + player.getName()
                                    + "' amount " + payment, t);
                }

                if (player.isOnline()) {
                    if (plugin.getConfig().getBoolean("error-handling.refund-pouch", false)) {
                        plugin.giveOrDrop(player, pouch.getItemStack().clone());
                    }
                    Text.send(player, pouch.getEconomyType().applyPlaceholders(
                            plugin.getMessage(MoneyPouchDeluxe.Message.REWARD_ERROR), formatNumber(payment, separator)));
                }
            }
        }
    }
}