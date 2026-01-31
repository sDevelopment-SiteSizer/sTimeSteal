package me.sitesizer.stimesteal;

import java.time.Duration;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.title.Title;

public class TimeManager {

    private final JavaPlugin plugin;
    private final NamespacedKey timeKey;
    private final net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

    public TimeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.timeKey = new NamespacedKey(plugin, "remaining_time");
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public long getTime(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(timeKey, PersistentDataType.LONG)) {
            return -1; // Not started yet
        }
        return data.get(timeKey, PersistentDataType.LONG);
    }

    public void setTime(Player player, long seconds) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(timeKey, PersistentDataType.LONG, seconds);
    }

    public void initializePlayer(Player player) {
        if (getTime(player) == -1) {
            long startTimeMinutes = plugin.getConfig().getLong("starting-time", 1440);
            setTime(player, startTimeMinutes * 60);
        }
    }

    public void modifyTime(Player player, long secondsChange) {
        long currentTime = getTime(player);
        if (currentTime == -1) {
            return; // Should be initialized
        }
        long newTime = currentTime + secondsChange;

        if (newTime < 0) {
            newTime = 0;
        }

        long maxTimeMinutes = plugin.getConfig().getLong("max-time", -1);
        if (maxTimeMinutes != -1) {
            long maxTimeSeconds = maxTimeMinutes * 60;
            if (newTime > maxTimeSeconds) {
                newTime = maxTimeSeconds;
            }
        }

        setTime(player, newTime);
        checkTime(player);
    }

    public void checkTime(Player player) {
        long time = getTime(player);
        if (time <= 0 && player.getGameMode() != GameMode.SPECTATOR) {
            eliminatePlayer(player);
        } else if (time > 0 && player.getGameMode() == GameMode.SPECTATOR) {
            revivePlayer(player);
        }
    }

    private void eliminatePlayer(Player player) {
        player.setGameMode(GameMode.SPECTATOR);

        String titleStr = plugin.getConfig().getString("messages.time-expired", "<red>TIME EXPIRED");
        String subTitleStr = plugin.getConfig().getString("messages.time-expired-subtitle", "<gray>You have run out of time.");

        player.showTitle(Title.title(
                mm.deserialize(titleStr),
                mm.deserialize(subTitleStr),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000))
        ));

        player.sendMessage(mm.deserialize(subTitleStr));
    }

    public void revivePlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        String titleStr = plugin.getConfig().getString("messages.revived-title", "<green>REVIVED");
        String subTitleStr = plugin.getConfig().getString("messages.revived-subtitle", "<gray>Do not waste your breath.");

        player.sendMessage(mm.deserialize(titleStr)); // Simple message
        player.showTitle(Title.title(
                mm.deserialize(titleStr),
                mm.deserialize(subTitleStr)
        ));
    }

    public void grantReviveTime(Player player) {
        long reviveTimeMinutes = plugin.getConfig().getLong("revive-time", 1440);
        if (reviveTimeMinutes == -1) {
            reviveTimeMinutes = plugin.getConfig().getLong("starting-time", 1440);
        }
        setTime(player, reviveTimeMinutes * 60);
    }

    public String formatTime(long totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void processPvpKill(Player killer, Player victim) {
        long stealAmountMinutes = plugin.getConfig().getLong("kill-steal-time", 120);
        long stealAmountSeconds = stealAmountMinutes * 60;

        modifyTime(killer, stealAmountSeconds);
        modifyTime(victim, -stealAmountSeconds);

        String timeFormatted = formatTime(stealAmountSeconds);
        killer.sendMessage(mm.deserialize("<green>You stole " + timeFormatted + " from " + victim.getName() + "!"));
        victim.sendMessage(mm.deserialize("<red>You lost " + timeFormatted + " to " + killer.getName() + "!"));
    }

    public void processPveDeath(Player victim) {
        long lossAmountMinutes = plugin.getConfig().getLong("death-loss-time", 30);
        long lossAmountSeconds = lossAmountMinutes * 60;

        modifyTime(victim, -lossAmountSeconds);

        String timeFormatted = formatTime(lossAmountSeconds);
        victim.sendMessage(mm.deserialize("<red>You lost " + timeFormatted + " due to death!"));
    }
}
