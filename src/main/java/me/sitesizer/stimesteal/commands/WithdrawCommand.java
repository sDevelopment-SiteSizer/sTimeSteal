package me.sitesizer.stimesteal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.sitesizer.stimesteal.ItemManager;
import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class WithdrawCommand implements CommandExecutor {

    private final TimeManager timeManager;
    private final ItemManager itemManager;
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WithdrawCommand(JavaPlugin plugin, TimeManager timeManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can withdraw time.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /withdraw <minutes>", NamedTextColor.RED));
            return true;
        }

        long minutes;
        try {
            minutes = Long.parseLong(args[0]);
            if (minutes <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount valid positive integer minutes.", NamedTextColor.RED));
            return true;
        }

        long secondsToWithdraw = minutes * 60;
        long currentTime = timeManager.getTime(player);

        // Check if player has enough time
        // Should we leave a minimum? Maybe they can suicide via withdraw?
        // Let's assume they can't withdraw if it kills them immediately unless they want to.
        // But usually plugins prevent withdrawal if < X time left.
        // Let's safe guard: Must have at least 1 second left? 
        if (currentTime - secondsToWithdraw <= 0) {
            String msg = plugin.getConfig().getString("messages.insufficient-time", "<red>You do not have enough time.");
            player.sendMessage(mm.deserialize(msg));
            return true;
        }

        // Deduct time
        timeManager.modifyTime(player, -secondsToWithdraw);

        // Give Voucher
        ItemStack voucher = itemManager.createTimeVoucher(minutes);
        // If inventory full, drop naturally
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), voucher);
            player.sendMessage(Component.text("Inventory full! Voucher dropped on ground.", NamedTextColor.GOLD));
        } else {
            player.getInventory().addItem(voucher);
        }

        player.sendMessage(Component.text("Withdrew " + minutes + " minutes successfully.", NamedTextColor.GREEN));
        return true;
    }
}
