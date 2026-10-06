package dev.padrewin.moneypouchdeluxe.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.database.DataMigration;
import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.manager.AbstractDataManager;
import dev.padrewin.moneypouchdeluxe.database.migrations._1_Create_Stats;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Player statistics (pouches opened, amounts won) for the PlaceholderAPI placeholders.
 * <p>
 * Placeholders are answered from memory only, never from the database. Statistics are loaded in the
 * background when a player joins (or the first time an offline player's are asked for), and new
 * results are written in batches every few seconds. All database work runs on one background
 * thread, in order, so a load never misses or double counts a result that is waiting to be written.
 */
public class DataManager extends AbstractDataManager implements Listener {

    /**
     * One row of the stats table, for one player: a pouch paid out in an economy.
     */
    public record PouchEconomy(String pouch, String economy) {
    }

    private static final long FLUSH_INTERVAL_SECONDS = 10;

    private final Object lock = new Object();
    /**
     * Results not written to the database yet. Guarded by {@link #lock}.
     */
    private final Map<UUID, Map<PouchEconomy, long[]>> pending = new HashMap<>();
    /**
     * Loaded statistics: saved values plus the pending results. Changed under {@link #lock}.
     */
    private final Cache<UUID, Map<PouchEconomy, long[]>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private final Set<UUID> loading = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService executor;

    public DataManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
        Bukkit.getPluginManager().registerEvents(this, coldPlugin);
    }

    @Override
    public List<Supplier<? extends DataMigration>> getDataMigrations() {
        return Collections.singletonList(_1_Create_Stats::new);
    }

    /**
     * Counts pouches a player opened and was paid for.
     */
    public void record(UUID uuid, String pouch, String economy, int opened, long won) {
        PouchEconomy key = new PouchEconomy(pouch, economy);
        synchronized (this.lock) {
            add(this.pending.computeIfAbsent(uuid, x -> new HashMap<>()), key, opened, won);
            Map<PouchEconomy, long[]> stats = this.cache.getIfPresent(uuid);
            if (stats != null) {
                add(stats, key, opened, won);
            }
        }
        this.ensureStarted();
    }

    /**
     * @return a copy of the player's statistics, or null while they're still being loaded (the load
     * is started if needed, so a later call has them)
     */
    public Map<PouchEconomy, long[]> getStats(UUID uuid) {
        synchronized (this.lock) {
            Map<PouchEconomy, long[]> stats = this.cache.getIfPresent(uuid);
            if (stats != null) {
                Map<PouchEconomy, long[]> copy = new HashMap<>();
                stats.forEach((key, values) -> copy.put(key, values.clone()));
                return copy;
            }
        }
        this.load(uuid);
        return null;
    }

    private void load(UUID uuid) {
        if (!this.isConnected() || !this.loading.add(uuid)) {
            return;
        }
        this.ensureStarted().execute(() -> {
            try {
                Map<PouchEconomy, long[]> stats = new HashMap<>();
                boolean[] loaded = new boolean[1];
                this.getDatabaseConnector().connect(connection -> {
                    String query = "SELECT pouch, economy, opened, won FROM " + this.getTablePrefix() + "stats WHERE uuid = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, uuid.toString());
                        ResultSet result = statement.executeQuery();
                        while (result.next()) {
                            add(stats, new PouchEconomy(result.getString(1), result.getString(2)), result.getLong(3), result.getLong(4));
                        }
                    }
                    loaded[0] = true;
                });
                if (!loaded[0]) {
                    return;
                }

                // Results still waiting to be written aren't in the database yet
                synchronized (this.lock) {
                    Map<PouchEconomy, long[]> waiting = this.pending.get(uuid);
                    if (waiting != null) {
                        waiting.forEach((key, values) -> add(stats, key, values[0], values[1]));
                    }
                    this.cache.put(uuid, stats);
                }
            } finally {
                this.loading.remove(uuid);
            }
        });
    }

    /**
     * Writes the pending results. If the database can't be reached, they're kept for the next try.
     */
    private void flush() {
        Map<UUID, Map<PouchEconomy, long[]>> batch;
        synchronized (this.lock) {
            if (this.pending.isEmpty()) {
                return;
            }
            batch = new HashMap<>(this.pending);
            this.pending.clear();
        }

        boolean[] written = new boolean[1];
        if (this.isConnected()) {
            String table = this.getTablePrefix() + "stats";
            String upsert = this.getDatabaseConnector() instanceof MySQLConnector
                    ? "INSERT INTO " + table + " (uuid, pouch, economy, opened, won) VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE opened = opened + VALUES(opened), won = won + VALUES(won)"
                    : "INSERT INTO " + table + " (uuid, pouch, economy, opened, won) VALUES (?, ?, ?, ?, ?) "
                    + "ON CONFLICT(uuid, pouch, economy) DO UPDATE SET opened = opened + excluded.opened, won = won + excluded.won";

            this.getDatabaseConnector().connect(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(upsert)) {
                    for (Map.Entry<UUID, Map<PouchEconomy, long[]>> player : batch.entrySet()) {
                        for (Map.Entry<PouchEconomy, long[]> row : player.getValue().entrySet()) {
                            statement.setString(1, player.getKey().toString());
                            statement.setString(2, row.getKey().pouch());
                            statement.setString(3, row.getKey().economy());
                            statement.setLong(4, row.getValue()[0]);
                            statement.setLong(5, row.getValue()[1]);
                            statement.addBatch();
                        }
                    }
                    statement.executeBatch();
                }
                written[0] = true;
            });
        }

        if (!written[0]) {
            synchronized (this.lock) {
                batch.forEach((uuid, rows) -> rows.forEach((key, values) ->
                        add(this.pending.computeIfAbsent(uuid, x -> new HashMap<>()), key, values[0], values[1])));
            }
        }
    }

    private void flushSafely() {
        try {
            this.flush();
        } catch (Throwable t) {
            this.coldPlugin.getLogger().severe("Failed to save the player statistics: " + t);
        }
    }

    /**
     * The background thread is (re)started on first use: ColdDev reloads this manager without calling
     * {@link #reload()}, so it can't be started there.
     */
    private synchronized ScheduledExecutorService ensureStarted() {
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "MoneyPouchDeluxe-Stats");
                thread.setDaemon(true);
                return thread;
            });
            this.executor.scheduleWithFixedDelay(this::flushSafely, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
        return this.executor;
    }

    /**
     * Writes everything still pending before the connection is closed (server stop or reload).
     */
    @Override
    public void disable() {
        synchronized (this) {
            if (this.executor != null) {
                this.executor.shutdown();
                try {
                    this.executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                this.executor = null;
            }
        }
        this.flushSafely();
        super.disable();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.load(event.getPlayer().getUniqueId());
    }

    private static void add(Map<PouchEconomy, long[]> stats, PouchEconomy key, long opened, long won) {
        long[] values = stats.computeIfAbsent(key, x -> new long[2]);
        values[0] += opened;
        values[1] += won;
    }

}
