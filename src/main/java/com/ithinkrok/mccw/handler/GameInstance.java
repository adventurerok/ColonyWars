package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.*;
import com.ithinkrok.mccw.enumeration.CountdownType;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserBeginGameEvent;
import com.ithinkrok.mccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.listener.WarsGameListener;
import com.ithinkrok.mccw.listener.WarsLobbyListener;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.BoundingBox;
import com.ithinkrok.mccw.util.building.SchematicBuilder;
import com.ithinkrok.mccw.util.io.DirectoryUtils;
import com.ithinkrok.mccw.util.io.MapConfig;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Handles the game itself
 */
public class GameInstance {

    private String map;
    private TeamColor winningTeam;
    private ShowdownArena showdownArena;

    private WarsPlugin plugin;
    private CountdownHandler countdownHandler;

    private List<Building> buildings = new ArrayList<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

    private GameState gameState = GameState.LOBBY;

    private ArrayList<Integer> gameTasks = new ArrayList<>();
    private Map<String, Schematic> schematicDataHashMap = new HashMap<>();

    private Map<String, Integer> forceShowdownTimer = new HashMap<>();
    private int forceShowdownTask;

    private MapConfig mapConfig;

    public GameInstance(WarsPlugin plugin, String map) {
        this.map = map;
        this.plugin = plugin;
        this.countdownHandler = plugin.getCountdownHandler();

        mapConfig = new MapConfig(plugin, map);
        loadSchematics();
    }

    private void loadSchematics() {
        ConfigurationSection config = getMapConfig();
        schematicDataHashMap.put(Buildings.BASE, new Schematic(plugin, Buildings.BASE, config));
        schematicDataHashMap.put(Buildings.FORTRESS, new Schematic(plugin, Buildings.FORTRESS, config));
        schematicDataHashMap.put(Buildings.FARM, new Schematic(plugin, Buildings.FARM, config));
        schematicDataHashMap.put(Buildings.BLACKSMITH, new Schematic(plugin, Buildings.BLACKSMITH, config));
        schematicDataHashMap.put(Buildings.MAGETOWER, new Schematic(plugin, Buildings.MAGETOWER, config));
        schematicDataHashMap.put(Buildings.LUMBERMILL, new Schematic(plugin, Buildings.LUMBERMILL, config));
        schematicDataHashMap.put(Buildings.CHURCH, new Schematic(plugin, Buildings.CHURCH, config));
        schematicDataHashMap.put(Buildings.CATHEDRAL, new Schematic(plugin, Buildings.CATHEDRAL, config));
        schematicDataHashMap.put(Buildings.GREENHOUSE, new Schematic(plugin, Buildings.GREENHOUSE, config));
        schematicDataHashMap.put(Buildings.SCOUTTOWER, new Schematic(plugin, Buildings.SCOUTTOWER, config));
        schematicDataHashMap.put(Buildings.CANNONTOWER, new Schematic(plugin, Buildings.CANNONTOWER, config));
        schematicDataHashMap.put(Buildings.WALL, new Schematic(plugin, Buildings.WALL, config));
        schematicDataHashMap.put(Buildings.LANDMINE, new Schematic(plugin, Buildings.LANDMINE, config));
        schematicDataHashMap.put(Buildings.WIRELESSBUFFER, new Schematic(plugin, Buildings.WIRELESSBUFFER, config));
        schematicDataHashMap.put(Buildings.TIMERBUFFER, new Schematic(plugin, Buildings.TIMERBUFFER, config));
    }

    public MapConfig getMapConfig() {
        return mapConfig;
    }

    public TeamColor getWinningTeam() {
        return winningTeam;
    }

    public ShowdownArena getShowdownArena() {
        return showdownArena;
    }

    public String getMap() {
        return map;
    }

    private void startAftermath() {
        buildings.forEach(Building::removed);
        countdownHandler.startEndCountdown();
    }

    public Schematic getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public GameState getGameState() {
        return gameState;
    }

    @SuppressWarnings("unchecked")
    private void endGame() {
        List<Integer> oldTasks = (List<Integer>) gameTasks.clone();
        oldTasks.forEach(this::cancelTask);

        for (User user : plugin.getUsers()) {
            plugin.playerTeleportLobby(user);
        }

        plugin.getUsers().forEach(User::decloak);

        plugin.resetTeams();

        buildingCentres.clear();

        buildings.forEach(Building::removed);

        buildings.clear();

        showdownArena = null;
        winningTeam = null;

        plugin.changeListener(new WarsLobbyListener(plugin));

        String playingFolder = plugin.getPlayingWorldName();

        Bukkit.unloadWorld(playingFolder, false);

        try {
            DirectoryUtils.delete(Paths.get("./" + playingFolder + "/"));
        } catch (IOException e) {
            plugin.getLogger().warning(plugin.getLocale("server.world-unload-failed"));
            e.printStackTrace();
        }

        System.gc();
    }

    public void addBuilding(Building building) {
        buildings.add(building);

        if (building.getCenterBlock() != null) buildingCentres.put(building.getCenterBlock(), building);

        plugin.getTeam(building.getTeamColor()).buildingStarted(building.getBuildingName());
    }

    public boolean canBuild(BoundingBox bounds) {
        for (Building building : buildings) {
            if (!building.canBuild(bounds)) return false;
        }

        return !bounds.interceptsXZ(showdownArena.getBounds());
    }

    public Building getBuildingInfo(Location center) {
        return buildingCentres.get(center);
    }

    private void startGame() {
        String mapFolder = plugin.getWarsConfig().getMapFolder(map);
        String playingFolder = plugin.getPlayingWorldName();
        try {
            DirectoryUtils.copy(Paths.get("./" + mapFolder + "/"), Paths.get("./" + playingFolder + "/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        World.Environment environment = plugin.getWarsConfig().getMapEnvironment(map);

        Bukkit.createWorld(new WorldCreator(plugin.getPlayingWorldName()).environment(environment));

        plugin.changeListener(new WarsGameListener(plugin));
        setupPlayers();

        calculateShowdownArena();

        setupBases();

        int startTime = plugin.getWarsConfig().getShowdownStartTimeNoAttackSinceStart();
        forceShowdownTimer.put("death", startTime);
        forceShowdownTimer.put("attack", startTime);

        forceShowdownTask = scheduleRepeatingTask(() -> {
            if (gameState != GameState.GAME) return;

            forceShowdownTimer.replaceAll((s, integer) -> integer - 1);

            for (int time : forceShowdownTimer.values()) {
                if (time <= 0) {
                    plugin.changeGameState(GameState.SHOWDOWN);
                    break;
                }

            }

        }, 20, 20);

        startPotionStrengthTask();
        startDecrementAttackerTask();
        startZombieRevalidationTask();

        checkVictory(false);
    }

    private void startZombieRevalidationTask() {
        int ticks = 20;

        scheduleRepeatingTask(() -> plugin.getUsers().forEach(User::revalidateZombie), ticks, ticks);

    }

    private void startDecrementAttackerTask() {
        int ticks = 20;

        scheduleRepeatingTask(() -> {
            plugin.getUsers().forEach(User::decrementAttackerTimers);
        }, ticks, ticks);
    }

    private void startPotionStrengthTask() {
        int ticks = 10;

        scheduleRepeatingTask(() -> {
            for (User user : plugin.getUsers()) {
                user.setPotionStrengthModifier(Math.min(user.getPotionStrengthModifier() + 0.05d, 1));
            }
        }, ticks, ticks);
    }

    private void setupPlayers() {
        plugin.getUsers().forEach(this::setupUser);

        plugin.getUsers().forEach(User::decloak);
    }

    public void onUserAttacked() {
        updateForceShowdownTimer("attack", plugin.getWarsConfig().getShowdownStartTimeSinceLastAttack());
    }

    private void updateForceShowdownTimer(String field, int min) {
        int time = forceShowdownTimer.get(field);
        time = Math.max(time, min);

        forceShowdownTimer.put(field, time);
    }

    public void onUserDeath() {
        updateForceShowdownTimer("death", plugin.getWarsConfig().getShowdownStartTimeSinceLastDeath());
    }

    public void setupUser(User info) {
        info.decloak();
        info.setMapVote(null);

        if (info.getTeamColor() == null) {
            info.setTeamColor(assignPlayerTeam());
        }

        if (info.getPlayerClass() == null) {
            info.setPlayerClass(assignPlayerClass());
        }

        info.teleport(getMapSpawn(info.getTeamColor()));

        info.setGameMode(GameMode.SURVIVAL);
        info.setAllowFlight(false);
        info.setCollidesWithEntities(true);

        info.resetPlayerStats(true);

        info.setInGame(true);

        info.getPlayerInventory().clear();
        info.updateTeamArmor();
        info.getPlayerInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));

        info.updateScoreboard();

        PlayerClassHandler classHandler = info.getPlayerClassHandler();
        classHandler.onUserBeginGame(new UserBeginGameEvent(info));

        plugin.giveUserHandbook(info);

        info.sendLocale("game.start.team", info.getTeamColor().getFormattedName());

        info.sendLocale("game.start.class", info.getPlayerClass().getFormattedName());

        info.getStatsHolder().addGame();
    }

    public TeamColor assignPlayerTeam() {
        ArrayList<TeamColor> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for (TeamColor team : TeamColor.values()) {
            Team info = getTeam(team);
            if (info.getUserCount() < leastCount) {
                leastCount = info.getUserCount();

                smallest.clear();
            }

            if (info.getUserCount() == leastCount) {
                smallest.add(team);
            }
        }

        return smallest.get(plugin.getRandom().nextInt(smallest.size()));
    }

    public PlayerClass assignPlayerClass() {
        return PlayerClass.values().get(plugin.getRandom().nextInt(PlayerClass.values().size()));
    }

    public Location getMapSpawn(TeamColor team) {
        World world = plugin.getServer().getWorld(plugin.getPlayingWorldName());

        Vector base;
        if (team == null) base = plugin.getWarsConfig().getMapCenter(map);
        else base = plugin.getWarsConfig().getTeamSpawnLocation(map, team);

        return new Location(world, base.getX(), base.getY(), base.getZ());
    }

    public Team getTeam(TeamColor teamColor) {
        return plugin.getTeam(teamColor);
    }

    private void calculateShowdownArena() {
        Vector showdownSize = plugin.getWarsConfig().getShowdownSize(map);
        int radiusX = showdownSize.getBlockX();
        int radiusZ = showdownSize.getBlockZ();

        Location center = getMapSpawn(null);
        Vector showdownMin = center.toVector().add(new Vector(-radiusX - 5, 0, -radiusZ - 5));
        Vector showdownMax = center.toVector().add(new Vector(radiusX + 5, 0, radiusZ + 5));

        BoundingBox bounds = new BoundingBox(showdownMin, showdownMax);

        showdownArena = new ShowdownArena(radiusX, radiusZ, center, bounds);
    }

    private void setupBases() {
        World world = plugin.getServer().getWorld(plugin.getPlayingWorldName());

        for (TeamColor team : TeamColor.values()) {
            Vector vectorLoc = plugin.getWarsConfig().getBaseLocation(map, team);

            Location build = new Location(world, vectorLoc.getX(), vectorLoc.getY(), vectorLoc.getZ());


            SchematicBuilder.pasteSchematic(plugin, plugin.getSchematicData(Buildings.BASE), build, 0, team);

            if (getTeam(team).getUserCount() == 0) {
                getTeam(team).eliminate();
            }

        }
    }

    public void checkVictory(boolean checkShowdown) {
        if (isInAftermath()) return;
        Set<TeamColor> teamsInGame = new HashSet<>();

        for (User info : plugin.getUsers()) {
            if (!info.isInGame()) continue;
            if (info.getTeamColor() == null) continue;

            teamsInGame.add(info.getTeamColor());
        }

        if (teamsInGame.size() == 0) {
            plugin.messageAllLocale("game.player.all-dead");
            changeGameState(GameState.AFTERMATH);
            return;
        } else if (teamsInGame.size() > 1) {
            if (checkShowdown) checkShowdownStart(teamsInGame.size());
            return;
        }

        TeamColor winner = teamsInGame.iterator().next();
        this.winningTeam = winner;

        plugin.messageAllLocale("game.team.winner", winner.getFormattedName());

        for (StatsHolder statsHolder : plugin.getTeam(winningTeam).getStatsHolders()) {
            statsHolder.addGameWin();
            statsHolder.saveStats();
        }

        changeGameState(GameState.AFTERMATH);
    }

    public boolean isInAftermath() {
        return gameState == GameState.AFTERMATH;
    }


    public void checkShowdownStart(int teamsInGame) {
        if (isInShowdown() || countdownHandler.getCountdownType() == CountdownType.SHOWDOWN) return;
        if (teamsInGame > plugin.getWarsConfig().getShowdownStartTeams() &&
                plugin.getNonZombiePlayersInGame() > plugin.getWarsConfig().getShowdownStartPlayers()) return;

        countdownHandler.startShowdownCountdown();
    }

    public boolean isInShowdown() {
        return gameState == GameState.SHOWDOWN;
    }


    private void startShowdown() {
        int x = showdownArena.getRadiusX();
        int z = showdownArena.getRadiusZ();

        Random random = plugin.getRandom();

        for (User user : plugin.getUsers()) {
            int offsetX = (-x / 2) + random.nextInt(x);
            int offsetZ = (-z / 2) + random.nextInt(z);
            int offsetY = 2;

            Location teleport = showdownArena.getCenter().clone();
            teleport.setX(teleport.getX() + offsetX);
            teleport.setY(teleport.getY() + offsetY);
            teleport.setZ(teleport.getZ() + offsetZ);

            user.teleport(teleport);
        }

        changeGameState(GameState.SHOWDOWN);

        plugin.messageAllLocale("countdowns.showdown.warning.now");

        showdownArena.startShrinkTask(plugin);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
        getTeam(building.getTeamColor()).removeBuilding(building);

        buildingCentres.remove(building.getCenterBlock());

        for (User info : plugin.getUsers()) {
            if (info.getTeamColor() != building.getTeamColor()) continue;

            info.redoShopInventory();
        }

        building.removed();
    }

    public boolean isInBuilding(Location loc) {
        for (Building building : buildings) {
            if (building.getBounds().containsLocation(loc)) return true;
        }

        return false;
    }

    public void onPlayerUpgrade(UserUpgradeEvent event) {
        PlayerClassHandler classHandler = event.getUser().getPlayerClassHandler();

        classHandler.onPlayerUpgrade(event);
    }

    public void handleSpectatorInventory(InventoryClickEvent event) {
        event.setCancelled(true);

        plugin.getSpectatorInventoryHandler()
                .onInventoryClick(event.getCurrentItem(), null, plugin.getUser((Player) event.getWhoClicked()), null);

    }

    public void updateSpectatorInventories() {
        for (User info : plugin.getUsers()) {
            if (info.isInGame() && info.getTeamColor() != null) continue;

            setupSpectatorInventory(info);
        }
    }

    public void setupSpectatorInventory(User user) {
        user.closeInventory();

        PlayerInventory inv = user.getPlayerInventory();
        inv.clear();

        inv.setItem(0, InventoryUtils.setItemNameAndLore(new ItemStack(Material.IRON_LEGGINGS),
                plugin.getLocale("items.spectator-toggle.name")));

        int slot = 9;

        List<ItemStack> items = new ArrayList<>();
        plugin.getSpectatorInventoryHandler().addInventoryItems(items, null, user, null);

        for (ItemStack item : items) {
            inv.setItem(slot++, item);
        }
    }

    public void finishBuilding(Building building) {
        if (!getTeam(building.getTeamColor()).everHadBuilding(building.getBuildingName())) {
            for (User info : plugin.getUsers()) {
                if (info.getTeamColor() != building.getTeamColor()) continue;

                info.redoShopInventory();

                PlayerClassHandler playerClassHandler = info.getPlayerClassHandler();
                playerClassHandler.onBuildingBuilt(new UserTeamBuildingBuiltEvent(info, building));
            }
        }

        getTeam(building.getTeamColor()).buildingFinished(building);
    }

    public void cancelTask(int task) {
        if (!gameTasks.contains(task)) return;

        Bukkit.getScheduler().cancelTask(task);
        gameTasks.remove(gameTasks.indexOf(task)); //Fix exception due to remove(int) also removing at index
    }

    public int scheduleTask(Runnable runnable, long ticksDelay) {
        int task = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, ticksDelay);

        gameTasks.add(task);
        return task;
    }

    public int scheduleRepeatingTask(Runnable runnable, long ticksDelay, long ticksPeriod) {
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, ticksDelay, ticksPeriod);

        gameTasks.add(task);
        return task;
    }

    public void changeGameState(GameState state) {
        if (this.gameState == state) return;
        GameState oldState = this.gameState;
        this.gameState = state;
        switch (state) {
            case LOBBY:
                endGame();
                break;
            case GAME:
                if (oldState == GameState.LOBBY) startGame();
                else cancelTask(forceShowdownTask);
                break;
            case SHOWDOWN:
                cancelTask(forceShowdownTask);
                startShowdown();
                break;
            case AFTERMATH:
                cancelTask(forceShowdownTask);
                startAftermath();
                break;
        }
    }

}
