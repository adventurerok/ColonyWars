package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.cw.scoreboard.CWScoreboardHandler;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.TeamIdentifier;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.event.user.world.UserPickupItemEvent;
import com.ithinkrok.minigames.event.user.world.UserPlaceBlockEvent;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.schematic.*;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.SoundEffect;
import com.ithinkrok.minigames.util.TreeFeller;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.math.SingleValueVariables;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    private Random random = new Random();

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

    private GoldConfig getGoldConfig(ConfigurationSection config) {
        GoldConfig gold = goldConfigMap.get(config);

        if (gold == null) {
            gold = new GoldConfig(config);
            goldConfigMap.put(config, gold);
        }

        return gold;
    }

    @EventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        if (!event.getNewGameState().isGameStateListener(this)) return;

        GameGroup gameGroup = event.getGameGroup();
        for (User user : gameGroup.getUsers()) {

            //TODO this is just a test
            user.setTeam(event.getGameGroup().getTeam("red"));

            user.setScoreboardHandler(new CWScoreboardHandler(user));
            user.updateScoreboard();
        }
    }

    @EventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {

        Color armorColor = event.getNewTeam() != null ? event.getNewTeam().getArmorColor() : null;
        event.getUser().giveColoredArmor(armorColor, true);
    }

    @EventHandler
    public void onUserPickupItem(UserPickupItemEvent event) {
        GoldConfig goldConfig = getGoldConfig(event.getUserGameGroup().getSharedObject(goldSharedConfig));
        Material material = event.getItem().getItemStack().getType();

        event.setCancelled(true);

        if (goldConfig.allowItemPickup(material)) {
            int amount = event.getItem().getItemStack().getAmount();

            int userGold = goldConfig.getUserGold(material) * amount;
            Money userMoney = Money.getOrCreate(event.getUser());
            userMoney.addMoney(userGold, false);

            int teamGold = goldConfig.getTeamGold(material) * amount;
            Money teamMoney = Money.getOrCreate(event.getUser().getTeam());
            teamMoney.addMoney(teamGold, false);

            SoundEffect sound = new SoundEffect(goldConfig.getPickupSound(), 1.0f, 0.8f + (random.nextFloat()) * 0.4f);
            event.getUser().playSound(event.getUser().getLocation(), sound);
        }

        event.getItem().remove();
    }

    @EventHandler
    public void onUserPlaceBlock(UserPlaceBlockEvent event) {
        if (event.getBlock().getType() != Material.LAPIS_ORE) return;

        event.getBlock().setType(Material.AIR);

        int rotation = Facing.getFacing(event.getUser().getLocation().getYaw());

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());
        controller.buildBuilding("Base", event.getUser().getTeamIdentifier(), event.getBlock().getLocation(),
                rotation, false);
    }



    @EventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        ConfigurationSection goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        if (!gold.allowItemPickup(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreakNaturally(MapBlockBreakNaturallyEvent event) {
        ConfigurationSection goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        gold.onBlockBreak(event.getBlock());
    }

    private static class GoldConfig {
        HashMap<Material, ItemStack> oreBlocks = new HashMap<>();

        HashMap<Material, Integer> userGold = new HashMap<>();
        HashMap<Material, Integer> teamGold = new HashMap<>();

        boolean treesEnabled;
        Material treeItemMaterial;
        ExpressionCalculator treeItemAmount;
        Set<Material> logMaterials = new HashSet<>();

        Sound pickupSound;

        public GoldConfig(ConfigurationSection config) {
            ConfigurationSection ores = config.getConfigurationSection("ore_blocks");
            if (ores != null) {
                for (String matName : ores.getKeys(false)) {
                    Material material = Material.matchMaterial(matName);
                    ItemStack item = InventoryUtils.parseItem(ores.getString(matName));
                    oreBlocks.put(material, item);
                }
            }

            ConfigurationSection items = config.getConfigurationSection("items");
            for (String matName : items.getKeys(false)) {
                Material material = Material.matchMaterial(matName);
                ConfigurationSection matConfig = items.getConfigurationSection(matName);

                userGold.put(material, matConfig.getInt("user"));
                teamGold.put(material, matConfig.getInt("team"));
            }

            pickupSound = Sound.valueOf(config.getString("pickup_sound").toUpperCase());

            ConfigurationSection trees = config.getConfigurationSection("trees");
            treesEnabled = trees != null && trees.getBoolean("enabled");

            if (!treesEnabled) return;

            treeItemMaterial = Material.matchMaterial(trees.getString("item_material"));

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

        public Sound getPickupSound() {
            return pickupSound;
        }

        public boolean allowItemPickup(Material material) {
            return userGold.containsKey(material);
        }

        public int getUserGold(Material material) {
            Integer result = userGold.get(material);
            return result == null ? 0 : result;
        }

        public int getTeamGold(Material material) {
            Integer result = teamGold.get(material);
            return result == null ? 0 : result;
        }
    }
}
