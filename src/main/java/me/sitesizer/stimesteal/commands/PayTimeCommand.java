package me.sitesizer.stimesteal.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PayTimeCommand implements CommandExecutor {

    private final TimeManager timeManager;

    public PayTimeCommand(JavaPlugin plugin, TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /paytime <player> <minutes>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not found or not online.", NamedTextColor.RED));
            return true;
        }

        if (target == player) {
            player.sendMessage(Component.text("You cannot pay yourself.", NamedTextColor.RED));
            return true;
        }

        long minutes;
        try {
            minutes = Long.parseLong(args[1]);
            if (minutes <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount of minutes.", NamedTextColor.RED));
            return true;
        }

        long secondsToPay = minutes * 60;
        long playerTime = timeManager.getTime(player);

        if (playerTime - secondsToPay <= 0) {
            player.sendMessage(Component.text("You do not have enough time.", NamedTextColor.RED));
            return true;
        }

        // Transaction
        timeManager.modifyTime(player, -secondsToPay);
        timeManager.modifyTime(target, secondsToPay);

        String timeStr = timeManager.formatTime(secondsToPay);

        player.sendMessage(Component.text("You sent " + timeStr + " to " + target.getName(), NamedTextColor.GREEN));
        target.sendMessage(Component.text("You received " + timeStr + " from " + player.getName(), NamedTextColor.GREEN));

        return true;
    }
}
