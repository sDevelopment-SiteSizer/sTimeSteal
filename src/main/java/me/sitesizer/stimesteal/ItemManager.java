package me.sitesizer.stimesteal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ItemManager {

    private final JavaPlugin plugin;
    public static ItemStack temporalInfusion;
    public static ItemStack reviveBeacon;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    private void loadRecipes() {
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);

        if (config.getBoolean("revive-beacon.enabled")) {
            createReviveBeacon(config);
            registerReviveBeaconRecipe(config);
        }
    }

    private void createTemporalInfusion(YamlConfiguration config) {
        String materialName = config.getString("temporal-infusion.result.material", "NETHER_STAR");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.NETHER_STAR;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString("temporal-infusion.result.name", "<gold>Temporal Infusion");
        meta.displayName(mm.deserialize(name));

        List<String> loreList = config.getStringList("temporal-infusion.result.lore");
        List<Component> lore = new ArrayList<>();
        for (String line : loreList) {
            lore.add(mm.deserialize(line));
        }
        meta.lore(lore);

        item.setItemMeta(meta);
        temporalInfusion = item;
    }

    private void createReviveBeacon(YamlConfiguration config) {
        String materialName = config.getString("revive-beacon.result.material", "BEACON");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.BEACON;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString("revive-beacon.result.name", "<gold>Revive Beacon");
        meta.displayName(mm.deserialize(name));

        List<String> loreList = config.getStringList("revive-beacon.result.lore");
        List<Component> lore = new ArrayList<>();
        for (String line : loreList) {
            lore.add(mm.deserialize(line));
        }
        meta.lore(lore);

        // Mark as Beacon using PDC to prevent random beacons usage
        NamespacedKey key = new NamespacedKey(plugin, "is_revive_beacon");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        reviveBeacon = item;
    }

    private void registerReviveBeaconRecipe(YamlConfiguration config) {
        NamespacedKey key = new NamespacedKey(plugin, "revive_beacon");
        Bukkit.removeRecipe(key);

        if (reviveBeacon == null) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, reviveBeacon);

        List<String> shapeList = config.getStringList("revive-beacon.recipe.shape");
        recipe.shape(shapeList.toArray(new String[0]));

        ConfigurationSection ingredients = config.getConfigurationSection("revive-beacon.recipe.ingredients");
        if (ingredients != null) {
            for (String charKey : ingredients.getKeys(false)) {
                String matName = ingredients.getString(charKey);
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    recipe.setIngredient(charKey.charAt(0), mat);
                }
            }
        }

        Bukkit.addRecipe(recipe);
    }

    public boolean isReviveBeacon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_revive_beacon"), PersistentDataType.BYTE);
    }

    public ItemStack createTimeVoucher(long minutes) {
        String matName = plugin.getConfig().getString("voucher.material", "PAPER");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) {
            mat = Material.PAPER;
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String name = plugin.getConfig().getString("voucher.name", "<green>Time Voucher");
        meta.displayName(mm.deserialize(name));

        List<String> rawLore = plugin.getConfig().getStringList("voucher.lore");
        List<Component> lore = new ArrayList<>();
        String timeString = formatMinutes(minutes);

        for (String line : rawLore) {
            lore.add(mm.deserialize(line.replace("%time%", timeString)));
        }
        meta.lore(lore);

        // Store value in PDC
        NamespacedKey key = new NamespacedKey(plugin, "voucher_value");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.LONG, minutes);

        item.setItemMeta(meta);
        return item;
    }

    public long getVoucherValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        NamespacedKey key = new NamespacedKey(plugin, "voucher_value");
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(key, PersistentDataType.LONG)) {
            return pdc.get(key, PersistentDataType.LONG);
        }
        return 0;
    }

    private String formatMinutes(long minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        return String.format("%02d:%02d:00", h, m);
    }
}
