package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.scoreboard.CWScoreboardHandler;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.listener.GiveCustomItemsOnJoin;
import com.ithinkrok.minigames.metadata.MapVote;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.schematic.Facing;
import com.ithinkrok.minigames.util.ConfigUtils;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.SoundEffect;
import com.ithinkrok.minigames.util.TreeFeller;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.math.SingleValueVariables;
import org.bukkit.Color;
import org.bukkit.GameMode;
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
public class GameListener extends BaseGameStateListener {

    private Random random = new Random();

    private String goldSharedConfig;
    private WeakHashMap<ConfigurationSection, GoldConfig> goldConfigMap = new WeakHashMap<>();

    private String unknownBuildingLocale;
    private String cannotBuildHereLocale;
    private String buildingNotFinishedLocale;
    private String buildingNotYoursLocale;

    private String cannotDestroyOwnBuildingLocale;
    private String cannotDestroyLocale;
    private String buildingDestroyedLocale;

    private int buildingDestroyWait;

    private String randomMapName;
    private List<String> mapList;
    private List<String> teamList;
    private List<String> kitList;

    private String teamInfoLocale, kitInfoLocale;

    private GiveCustomItemsOnJoin.CustomItemGiver customItemGiver;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?> event) {
        ConfigurationSection config = event.getConfig();

        teamList = config.getStringList("choosable_teams");
        kitList = config.getStringList("choosable_kits");

        configureMapVoting(config.getConfigurationSection("map_voting"));

        goldSharedConfig = config.getString("gold_shared_object");

        unknownBuildingLocale = config.getString("buildings.unknown_locale", "building.unknown");
        cannotBuildHereLocale = config.getString("buildings.invalid_location_locale", "building.invalid_loc");
        buildingNotFinishedLocale = config.getString("buildings.not_finished_locale", "building.not_finished");
        buildingNotYoursLocale = config.getString("buildings.not_yours_locale", "building.not_yours");

        cannotDestroyOwnBuildingLocale =
                config.getString("buildings.destroy_own_team_locale", "building.destroy.own_team");
        cannotDestroyLocale = config.getString("buildings.destroy_protected_locale", "building.destroy.protected");
        buildingDestroyedLocale = config.getString("buildings.destroyed_locale", "building.destroy.success");

        buildingDestroyWait = (int) (config.getDouble("buildings.destroy_wait", 3.0d) * 20d);

        customItemGiver = new GiveCustomItemsOnJoin.CustomItemGiver(config.getConfigurationSection("start_items"));

        teamInfoLocale = config.getString("team_info_locale", "start_info.team");
        kitInfoLocale = config.getString("kit_info_locale", "start_info.kit");
    }

    private void configureMapVoting(ConfigurationSection config) {
        randomMapName = config.getString("random_map");

        mapList = new ArrayList<>(config.getStringList("map_list"));
        mapList.remove(randomMapName);

        if(mapList.size() < 1) throw new RuntimeException("The game requires at least one map!");
    }

    @EventHandler
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if (event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK || !event.hasBlock()) return;

        if (event.getClickedBlock().getType() != Material.OBSIDIAN) return;

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        Building building = controller.getBuilding(event.getClickedBlock().getLocation());
        if (building == null) return;

        if (!building.getTeamIdentifier().equals(event.getUser().getTeamIdentifier())) {
            event.getUser().sendLocale(buildingNotYoursLocale);
            return;
        }

        if (!building.isFinished()) {
            event.getUser().sendLocale(buildingNotFinishedLocale);
            return;
        }

        ClickableInventory shop = building.createShop();
        if (shop == null) return;

        event.getUser().showInventory(shop, event.getClickedBlock().getLocation());
    }

    @EventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if (event.getBlock().getType() != Material.OBSIDIAN){
            ConfigurationSection goldShared = event.getUserGameGroup().getSharedObject(goldSharedConfig);
            GoldConfig gold = getGoldConfig(goldShared);

            gold.onBlockBreak(event.getBlock());
            return;
        }

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());
        Building building = controller.getBuilding(event.getBlock().getLocation());

        if (building == null) return;

        if (building.getTeamIdentifier().equals(event.getUser().getTeamIdentifier())) {
            event.getUser().sendLocale(cannotDestroyOwnBuildingLocale);
            event.setCancelled(true);
        } else if(building.isProtected()) {
            event.getUser().sendLocale(cannotDestroyLocale, building.getBuildingName());
            event.setCancelled(true);
        } else {
            event.getUserGameGroup().sendLocale(buildingDestroyedLocale, event.getUser().getFormattedName(), building
                    .getBuildingName(), building.getTeamIdentifier().getFormattedName());
            event.getUserGameGroup().doInFuture(task -> building.explode(), buildingDestroyWait);
        }
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

        startGame(event.getGameGroup());

        GameGroup gameGroup = event.getGameGroup();
        gameGroup.getUsers().forEach(this::setupUser);
    }

    private void setupUser(User user) {
        user.decloak();

        if(user.getTeam() == null) {
            user.setTeam(assignUserTeam(user.getGameGroup()));
        }

        if(user.getKit() == null) {
            user.setKit(assignUserKit(user.getGameGroup()));
        }


        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());
        user.teleport(teamStats.getSpawnLocation());

        user.setGameMode(GameMode.SURVIVAL);
        user.setAllowFlight(false);
        user.setCollidesWithEntities(true);

        user.resetUserStats(true);

        customItemGiver.giveToUser(user);

        user.setScoreboardHandler(new CWScoreboardHandler(user));
        user.updateScoreboard();

        //TODO give handbook (will be done from configs)

        user.sendLocale(teamInfoLocale, user.getTeamIdentifier().getFormattedName());
        user.sendLocale(kitInfoLocale, user.getKit().getFormattedName());

        //TODO add a game to stats
    }

    private Kit assignUserKit(GameGroup gameGroup) {
        String kitName = kitList.get(random.nextInt(kitList.size()));

        return gameGroup.getKit(kitName);
    }

    private Team assignUserTeam(GameGroup gameGroup) {
        ArrayList<Team> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for(String teamName : teamList) {
            Team team = gameGroup.getTeam(teamName);
            if(team.getUserCount() < leastCount) {
                leastCount = team.getUserCount();
                smallest.clear();
            }

            if(team.getUserCount() == leastCount) smallest.add(team);
        }

        return smallest.get(random.nextInt(smallest.size()));
    }

    private void startGame(GameGroup gameGroup) {
        String winningVote = MapVote.getWinningVote(gameGroup.getUsers());

        if(winningVote == null || winningVote.equals(randomMapName)) {
            winningVote = mapList.get(random.nextInt(mapList.size()));
        }

        gameGroup.changeMap(winningVote);
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

        String buildingType = InventoryUtils.getDisplayName(event.getItemPlaced());

        if (buildingType == null || event.getUserGameGroup().getSchematic(buildingType) == null) {
            event.getUser().sendLocale(unknownBuildingLocale);
            return;
        }

        int rotation = Facing.getFacing(event.getUser().getLocation().getYaw());

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        if (!controller.buildBuilding(buildingType, event.getUser().getTeamIdentifier(), event.getBlock().getLocation(),
                rotation, false)) {
            event.getUser().sendLocale(cannotBuildHereLocale);
            event.setCancelled(true);
        }
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
                    ItemStack item = ConfigUtils.getItemStack(ores, matName);
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
