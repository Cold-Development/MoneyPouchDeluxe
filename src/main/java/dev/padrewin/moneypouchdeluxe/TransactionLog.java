package dev.padrewin.moneypouchdeluxe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Writes every pouch opened (and given with /mp give) to logs/&lt;date&gt;.log, one file per day,
 * so owners can check what a player got. Lines are written on a background thread, so the server
 * never waits for the disk, and files older than {@code transaction-log.keep-days} are deleted.
 */
public final class TransactionLog {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MoneyPouchDeluxe plugin;
    private final File folder;
    private volatile boolean enabled;
    private volatile int keepDays;
    private ExecutorService executor;

    // Only used on the executor's thread
    private BufferedWriter writer;
    private LocalDate writerDate;

    public TransactionLog(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "logs");
    }

    /**
     * Applies the settings from config.yml; called on every reload.
     */
    public synchronized void configure(boolean enabled, int keepDays) {
        this.enabled = enabled;
        this.keepDays = keepDays;
        if (!enabled) {
            return;
        }
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "MoneyPouchDeluxe-TransactionLog");
                thread.setDaemon(true);
                return thread;
            });
        }
        this.executor.execute(this::deleteOldFiles);
    }

    /**
     * Pouches a player opened, and whether they were paid.
     *
     * @param failureReason why the payment failed, or null if it succeeded
     */
    public void logOpen(String player, UUID uuid, String pouch, int count, String amount, String economy, String failureReason) {
        this.log("OPEN " + player + " (" + uuid + ") " + pouch + " x" + count + " -> " + amount + " " + economy + ": "
                + (failureReason == null ? "OK" : "FAILED (" + failureReason + ")"));
    }

    /**
     * Pouches given with /mp give (or through the API).
     */
    public void logGive(String giver, String player, UUID uuid, String pouch, int amount) {
        this.log("GIVE " + giver + " -> " + player + " (" + uuid + ") " + pouch + " x" + amount);
    }

    /**
     * Pouches given to every online player with /mp give &lt;pouch&gt; *.
     */
    public void logGiveAll(String giver, int players, String pouch, int amount) {
        this.log("GIVE " + giver + " -> * (" + players + " players) " + pouch + " x" + amount);
    }

    private void log(String entry) {
        if (!this.enabled) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String line = "[" + now.format(TIME) + "] " + entry;
        synchronized (this) {
            if (this.executor != null) {
                this.executor.execute(() -> this.write(now.toLocalDate(), line));
            }
        }
    }

    private void write(LocalDate date, String line) {
        try {
            if (this.writer == null || !date.equals(this.writerDate)) {
                this.closeWriter();
                this.folder.mkdirs();
                this.writer = Files.newBufferedWriter(new File(this.folder, date + ".log").toPath(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                this.writerDate = date;
                this.deleteOldFiles();
            }
            this.writer.write(line);
            this.writer.newLine();
            this.writer.flush();
        } catch (IOException e) {
            this.plugin.getLogger().warning("Could not write to the transaction log: " + e.getMessage());
            this.closeWriter();
        }
    }

    private void deleteOldFiles() {
        if (this.keepDays <= 0) {
            return;
        }
        File[] files = this.folder.listFiles((dir, name) -> name.endsWith(".log"));
        if (files == null) {
            return;
        }
        LocalDate oldestKept = LocalDate.now().minusDays(this.keepDays);
        for (File file : files) {
            try {
                LocalDate date = LocalDate.parse(file.getName().substring(0, file.getName().length() - 4));
                if (date.isBefore(oldestKept)) {
                    file.delete();
                }
            } catch (DateTimeParseException ignored) {
                // not one of our files
            }
        }
    }

    private void closeWriter() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException ignored) {
            }
            this.writer = null;
            this.writerDate = null;
        }
    }

    /**
     * Writes what's still queued and closes the file (server stop).
     */
    public void close() {
        ExecutorService executor;
        synchronized (this) {
            executor = this.executor;
            this.executor = null;
        }
        if (executor == null) {
            return;
        }
        executor.execute(this::closeWriter);
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
