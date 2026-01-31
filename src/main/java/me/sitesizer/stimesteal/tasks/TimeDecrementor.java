package me.sitesizer.stimesteal.tasks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TimeDecrementor extends BukkitRunnable {

    private final TimeManager timeManager;

    public TimeDecrementor(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Decrement time if not in spectator
            if (player.getGameMode() != GameMode.SPECTATOR) {
                timeManager.modifyTime(player, -1);
            }

            long time = timeManager.getTime(player);

            // Determine Color based on remaining time
            NamedTextColor color = NamedTextColor.GREEN; // > 6 hours
            if (time <= 3600) { // <= 1 hour
                color = NamedTextColor.RED;
            } else if (time <= 21600) { // <= 6 hours
                color = NamedTextColor.GOLD; // Orange-ish
            }

            // Update Tab List Name
            player.playerListName(Component.text(player.getName(), color));

            // Update Action Bar
            player.sendActionBar(Component.text(timeManager.formatTime(time), color));

            // Apply Status Effects
            applyStatusEffects(player, time);
        }
    }

    private void applyStatusEffects(Player player, long timeSeconds) {
        if (!timeManager.getPlugin().getConfig().getBoolean("status-effects.enabled", true)) {
            return;
        }

        long lowThreshold = timeManager.getPlugin().getConfig().getLong("status-effects.low-time.threshold", 60) * 60;
        long highThreshold = timeManager.getPlugin().getConfig().getLong("status-effects.high-time.threshold", 1440) * 60;

        // Low Time Effects (< 1 Hour default)
        if (timeSeconds < lowThreshold) {
            addEffects(player, "status-effects.low-time.effects");
        }

        // High Time Effects (> 24 Hours default)
        if (timeSeconds > highThreshold) {
            addEffects(player, "status-effects.high-time.effects");
        }
    }

    private void addEffects(Player player, String configPath) {
        java.util.List<String> effectNames = timeManager.getPlugin().getConfig().getStringList(configPath);
        for (String effectName : effectNames) {
            org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(effectName);
            if (type != null) {
                // Apply for 5 seconds (100 ticks) to ensure it stays active but updates quickly
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, 100, 0, false, false, true));
            }
        }
    }
}
