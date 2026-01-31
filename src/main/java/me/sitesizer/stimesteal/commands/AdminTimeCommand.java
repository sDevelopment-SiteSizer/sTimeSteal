package me.sitesizer.stimesteal.commands;

import org.bukkit.Bukkit;
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

public class AdminTimeCommand implements CommandExecutor {

    private final TimeManager timeManager;
    private final ItemManager itemManager;
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AdminTimeCommand(JavaPlugin plugin, TimeManager timeManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("stimesteal.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<red>Usage: /stimesteal <givevoucher|set|npc> ..."));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(mm.deserialize("<red>Usage: /stimesteal set <player> <minutes>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(mm.deserialize("<red>Player not found."));
                return true;
            }
            try {
                long minutes = Long.parseLong(args[2]);
                timeManager.setTime(target, minutes * 60);
                sender.sendMessage(mm.deserialize("<green>Set " + target.getName() + "'s time to " + minutes + " minutes."));
            } catch (NumberFormatException e) {
                sender.sendMessage(mm.deserialize("<red>Invalid number."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("npc")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(mm.deserialize("<red>Only players can summon NPCs."));
                return true;
            }
            Player player = (Player) sender;

            if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
                player.sendMessage(mm.deserialize("<red>Citizens plugin is not installed!"));
                return true;
            }

            // Summon NPC
            try {
                // Use Reflection to avoid compile-time dependency issues if Citizens is missing from classpath
                Class<?> citizensApiClass = Class.forName("net.citizensnpcs.api.CitizensAPI");
                Object npcRegistry = citizensApiClass.getMethod("getNPCRegistry").invoke(null);

                // createNPC(EntityType type, String name)
                java.lang.reflect.Method createNPCMethod = npcRegistry.getClass().getMethod("createNPC", org.bukkit.entity.EntityType.class, String.class);
                Object npc = createNPCMethod.invoke(npcRegistry, org.bukkit.entity.EntityType.PLAYER, "Time Merchant");

                // spawn(Location loc)
                java.lang.reflect.Method spawnMethod = npc.getClass().getMethod("spawn", org.bukkit.Location.class);
                spawnMethod.invoke(npc, player.getLocation());

                sender.sendMessage(mm.deserialize("<green>Summoned Time Merchant!"));
            } catch (Exception e) {
                sender.sendMessage(mm.deserialize("<red>Failed to summon NPC. Ensure Citizens is installed. Error: " + e.getMessage()));
                e.printStackTrace();
            }
            return true;
        }

        // Usage: /stimesteal givevoucher <player> <minutes>
        return true;
    }
}
