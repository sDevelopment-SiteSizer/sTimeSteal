package me.sitesizer.stimesteal.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.sitesizer.stimesteal.ItemManager;
import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VoucherListener implements Listener {

    private final TimeManager timeManager;
    private final ItemManager itemManager;
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public VoucherListener(JavaPlugin plugin, TimeManager timeManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onRedeem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        long minutes = itemManager.getVoucherValue(item);
        if (minutes > 0) {
            event.setCancelled(true); // Prevent placing if it's a block, though it's likely paper.

            // Consume
            item.subtract(1);

            // Add time (Minutes to Seconds)
            timeManager.modifyTime(player, minutes * 60);

            String msg = plugin.getConfig().getString("messages.voucher-redeemed", "<green>You redeemed a voucher for <yellow>%time%<green>!");
            player.sendMessage(mm.deserialize(msg.replace("%time%", timeManager.formatTime(minutes * 60))));
        }
    }
}
