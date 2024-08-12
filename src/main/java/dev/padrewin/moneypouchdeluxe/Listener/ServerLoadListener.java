package dev.padrewin.moneypouchdeluxe.Listener;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoadListener implements Listener {

    private final MoneyPouchDeluxe plugin;

    public ServerLoadListener(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.getUpdater().checkForUpdateAndLog();
    }
}
