package me.sitesizer.stimesteal.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import me.sitesizer.stimesteal.TimeManager;

public class DeathListener implements Listener {

    private final TimeManager timeManager;

    public DeathListener(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && killer != victim) {
            // PvP
            timeManager.processPvpKill(killer, victim);
        } else {
            // PvE or suicide
            timeManager.processPveDeath(victim);
        }
    }
}
