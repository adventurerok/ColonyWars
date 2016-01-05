package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.TreeFeller;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.math.SingleValueVariables;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by paul on 05/01/16.
 */
public class GameListener implements Listener {

    private String goldSharedConfig;
    private WeakHashMap<ConfigurationSection, GoldConfig> goldConfigMap = new WeakHashMap<>();

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?> event) {
        ConfigurationSection config = event.getConfig();

        goldSharedConfig = config.getString("gold_shared_object");
    }

    @EventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        ConfigurationSection goldShared = event.getUserGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        gold.onBlockBreak(event.getBlock());
    }

    @EventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        ConfigurationSection goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        gold.onItemSpawn(event);
    }

    @EventHandler
    public void onBlockBreakNaturally(MapBlockBreakNaturallyEvent event) {
        ConfigurationSection goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        gold.onBlockBreak(event.getBlock());
    }

    private GoldConfig getGoldConfig(ConfigurationSection config) {
        GoldConfig gold = goldConfigMap.get(config);

        if (gold == null) {
            gold = new GoldConfig(config);
            goldConfigMap.put(config, gold);
        }

        return gold;
    }

    private static class GoldConfig {
        HashMap<Material, ItemStack> oreBlocks = new HashMap<>();
        Set<Material> dropMaterials = new HashSet<>();

        boolean treesEnabled;
        Material treeItemMaterial;
        ExpressionCalculator treeItemAmount;
        Set<Material> logMaterials = new HashSet<>();

        public GoldConfig(ConfigurationSection config) {
            ConfigurationSection ores = config.getConfigurationSection("ore_blocks");
            if (ores != null) {
                for (String matName : ores.getKeys(false)) {
                    Material material = Material.matchMaterial(matName);
                    ItemStack item = InventoryUtils.parseItem(ores.getString(matName));
                    oreBlocks.put(material, item);
                    dropMaterials.add(item.getType());
                }
            }

            ConfigurationSection trees = config.getConfigurationSection("trees");
            treesEnabled = trees != null && trees.getBoolean("enabled");

            if (!treesEnabled) return;

            treeItemMaterial = Material.matchMaterial(trees.getString("item_material"));
            dropMaterials.add(treeItemMaterial);

            treeItemAmount = new ExpressionCalculator(trees.getString("item_amount"));

            List<String> logMaterialNames = trees.getStringList("log_materials");
            logMaterials.addAll(logMaterialNames.stream().map(Material::matchMaterial).collect(Collectors.toList()));
        }

        public void onBlockBreak(Block block) {
            ItemStack drop = null;
            if (oreBlocks.containsKey(block.getType())) {
                drop = oreBlocks.get(block.getType()).clone();
            } else if (treesEnabled && logMaterials.contains(block.getType())) {
                int count = TreeFeller.fellTree(block.getLocation());
                count = (int) treeItemAmount.calculate(new SingleValueVariables(count));
                drop = new ItemStack(treeItemMaterial, count);
            }

            block.setType(Material.AIR);

            if (drop == null) return;
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5d, 0.1d, 0.5d), drop);
        }

        public void onItemSpawn(MapItemSpawnEvent event) {
            Material mat = event.getItem().getItemStack().getType();

            if(dropMaterials.contains(mat)) return;
            event.setCancelled(true);
        }
    }
}
