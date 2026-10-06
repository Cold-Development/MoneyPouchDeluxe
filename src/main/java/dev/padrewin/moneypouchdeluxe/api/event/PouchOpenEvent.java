package dev.padrewin.moneypouchdeluxe.api.event;

import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player opens one or more pouches (several with sneak + right click on a stack),
 * before the pouches are taken from their hand and before the reveal starts.
 * <p>
 * Cancelling it keeps the pouches in the player's hand and nothing is paid. The amount can be
 * changed, e.g. by a booster plugin; it's the total for all {@link #getCount()} pouches.
 */
public class PouchOpenEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Pouch pouch;
    private final int count;
    private long amount;
    private boolean cancelled;

    public PouchOpenEvent(@NotNull Player player, @NotNull Pouch pouch, int count, long amount) {
        super(player);
        this.pouch = pouch;
        this.count = count;
        this.amount = amount;
    }

    /**
     * @return the kind of pouch being opened
     */
    @NotNull
    public Pouch getPouch() {
        return this.pouch;
    }

    /**
     * @return how many pouches are opened at once (1, or the stack size with sneak + right click)
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @return the total amount the player will receive, in the pouch's economy
     */
    public long getAmount() {
        return this.amount;
    }

    /**
     * @param amount the total amount the player will receive (0 or more)
     */
    public void setAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
