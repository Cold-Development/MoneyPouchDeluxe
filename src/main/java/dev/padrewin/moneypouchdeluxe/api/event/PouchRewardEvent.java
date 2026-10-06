package dev.padrewin.moneypouchdeluxe.api.event;

import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called once the reward of opened pouches has been paid, or has failed to be paid. Useful for logs,
 * quests or announcements; it can't change the outcome.
 * <p>
 * The player may have left during the reveal. Some economies finish their payment off the main
 * thread, in which case this event is called asynchronously ({@link #isAsynchronous()}).
 */
public class PouchRewardEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Pouch pouch;
    private final int count;
    private final long amount;
    private final String failureReason;

    public PouchRewardEvent(@NotNull Player player, @NotNull Pouch pouch, int count, long amount, @Nullable String failureReason) {
        super(player, !Bukkit.isPrimaryThread());
        this.pouch = pouch;
        this.count = count;
        this.amount = amount;
        this.failureReason = failureReason;
    }

    /**
     * @return the kind of pouch that was opened
     */
    @NotNull
    public Pouch getPouch() {
        return this.pouch;
    }

    /**
     * @return how many pouches were opened at once
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @return the total amount paid (or that failed to be paid), in the pouch's economy
     */
    public long getAmount() {
        return this.amount;
    }

    /**
     * @return true if the player received the reward
     */
    public boolean isSuccessful() {
        return this.failureReason == null;
    }

    /**
     * @return why the payment failed, or null if it succeeded
     */
    @Nullable
    public String getFailureReason() {
        return this.failureReason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
