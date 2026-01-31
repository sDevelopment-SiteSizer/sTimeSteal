package me.sitesizer.stimesteal.listeners;

import me.sitesizer.stimesteal.TimeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final TimeManager timeManager;

    public JoinListener(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        timeManager.initializePlayer(event.getPlayer());
    }
}
