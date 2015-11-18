package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.ShowdownArena;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.CountdownType;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.listener.WarsGameListener;
import com.ithinkrok.mccw.listener.WarsLobbyListener;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.BoundingBox;
import com.ithinkrok.mccw.util.DirectoryUtils;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
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
 *
 * Handles the game itself
 */
public class GameInstance {

    private String map;
    private TeamColor winningTeam;
    private ShowdownArena showdownArena;

    private boolean inShowdown, inAftermath;

    private WarsPlugin plugin;
    private CountdownHandler countdownHandler;

    private List<Building> buildings = new ArrayList<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

    public GameInstance(WarsPlugin plugin, String map) {
        this.map = map;
        this.plugin = plugin;
        this.countdownHandler = plugin.getCountdownHandler();
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

    public void preEndGame() {
        buildings.forEach(Building::clearHolograms);
    }

    public void endGame() {
        for (User user : plugin.getUsers()) {
            plugin.playerTeleportLobby(user.getPlayer());
        }

        plugin.getUsers().forEach(User::decloak);

        plugin.resetTeams();

        buildingCentres.clear();

        buildings.forEach(Building::clearHolograms);

        buildings.clear();

        showdownArena = null;
        winningTeam = null;

        plugin.changeListener(new WarsLobbyListener(plugin));

        Bukkit.unloadWorld("playing", false);

        try {
            DirectoryUtils.delete(Paths.get("./playing/"));
        } catch (IOException e) {
            plugin.getLogger().info("Failed to unload old world");
            e.printStackTrace();
        }

        System.gc();

        plugin.setInGame(false);
        setInAftermath(false);
        setInShowdown(false);

        plugin.getLobbyMinigames().forEach(LobbyMinigame::resetMinigame);

        for (User user : plugin.getUsers()) {
            plugin.playerJoinLobby(user.getPlayer());
        }

        countdownHandler.startLobbyCountdown();


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

    public void startGame() {
        String mapFolder = plugin.getConfig().getString("maps." + map + ".folder");
        try {
            DirectoryUtils.copy(Paths.get("./" + mapFolder + "/"), Paths.get("./playing/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.createWorld(new WorldCreator("playing"));

        plugin.changeListener(new WarsGameListener(plugin));
        setupPlayers();

        calculateShowdownArena();

        setupBases();

        checkVictory(false);
    }

    public void setupPlayers() {
        for (User info : plugin.getUsers()) {

            info.setMapVote(null);

            if (info.getTeamColor() == null) {
                info.setTeamColor(assignPlayerTeam());
            }

            if (info.getPlayerClass() == null) {
                info.setPlayerClass(assignPlayerClass());
            }

            info.getPlayer().teleport(getMapSpawn(info.getTeamColor()));

            info.getPlayer().setGameMode(GameMode.SURVIVAL);

            info.resetPlayerStats(true);

            info.setInGame(true);

            info.getPlayer().getInventory().clear();
            info.updateTeamArmor();
            info.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));

            info.updateScoreboard();

            PlayerClassHandler classHandler = plugin.getPlayerClassHandler(info.getPlayerClass());
            classHandler.onGameBegin(info, getTeam(info.getTeamColor()));

            plugin.givePlayerHandbook(info.getPlayer());

            info.message(ChatColor.GOLD + "You are playing on the " + info.getTeamColor().name + ChatColor.GOLD +
                    " Team");

            info.message(ChatColor.GOLD + "You are playing as the class " + ChatColor.DARK_AQUA +
                    info.getPlayerClass().name);
        }

        plugin.getUsers().forEach(User::decloak);
    }

    private void calculateShowdownArena() {
        FileConfiguration config = plugin.getConfig();
        String base = "maps." + map + ".showdown-size";
        int radiusX = config.getInt(base + ".x");
        int radiusZ = config.getInt(base + ".z");

        Location center = getMapSpawn(null);
        Vector showdownMin = center.toVector().add(new Vector(-radiusX - 5, 0, -radiusZ - 5));
        Vector showdownMax = center.toVector().add(new Vector(radiusX + 5, 0, radiusZ + 5));

        BoundingBox bounds = new BoundingBox(showdownMin, showdownMax);

        showdownArena = new ShowdownArena(radiusX, radiusZ, center, bounds);
    }

    public void setupBases() {
        World world = plugin.getServer().getWorld("playing");
        FileConfiguration config = plugin.getConfig();

        for (TeamColor team : TeamColor.values()) {
            String base = "maps." + map + "." + team.toString().toLowerCase() + ".base";

            Location build = new Location(world, config.getInt(base + ".x"), config.getInt(base + ".y"),
                    config.getInt(base + ".z"));


            SchematicBuilder.pasteSchematic(plugin, plugin.getSchematicData(Buildings.BASE), build, 0, team);

            if (getTeam(team).getPlayerCount() == 0) {
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
            plugin.messageAll(ChatColor.GOLD + "Oh dear. Everyone is dead!");
            setInAftermath(true);
            countdownHandler.startEndCountdown();
            return;
        } else if (teamsInGame.size() > 1) {
            if (checkShowdown) checkShowdownStart(teamsInGame.size());
            return;
        }

        TeamColor winner = teamsInGame.iterator().next();
        this.winningTeam = winner;

        plugin.messageAll(ChatColor.GOLD + "The " + winner.name + ChatColor.GOLD + " Team has won the game!");

        setInAftermath(true);

        countdownHandler.startEndCountdown();
    }

    public TeamColor assignPlayerTeam() {
        ArrayList<TeamColor> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for (TeamColor team : TeamColor.values()) {
            Team info = getTeam(team);
            if (info.getPlayerCount() < leastCount) {
                leastCount = info.getPlayerCount();

                smallest.clear();
            }

            if (info.getPlayerCount() == leastCount) {
                smallest.add(team);
            }
        }

        return smallest.get(plugin.getRandom().nextInt(smallest.size()));
    }

    public PlayerClass assignPlayerClass() {
        return PlayerClass.values()[plugin.getRandom().nextInt(PlayerClass.values().length)];
    }

    public Location getMapSpawn(TeamColor team) {
        World world = plugin.getServer().getWorld("playing");
        FileConfiguration config = plugin.getConfig();

        String base;
        if (team == null) base = "maps." + map + ".center";
        else base = "maps." + map + "." + team.toString().toLowerCase() + ".spawn";

        return new Location(world, config.getDouble(base + ".x"), config.getDouble(base + ".y"),
                config.getDouble(base + ".z"));
    }

    public Team getTeam(TeamColor teamColor) {
        return plugin.getTeam(teamColor);
    }

    public boolean isInAftermath() {
        return inAftermath;
    }

    public void setInAftermath(boolean inAftermath) {
        this.inAftermath = inAftermath;
    }

    public void checkShowdownStart(int teamsInGame) {
        if (isInShowdown() || countdownHandler.getCountdownType() == CountdownType.SHOWDOWN_START) return;
        if (teamsInGame > 2 && plugin.getPlayersInGame() > 4) return;

        countdownHandler.startShowdownCountdown();
    }

    public boolean isInShowdown() {
        return inShowdown;
    }

    public void setInShowdown(boolean inShowdown) {
        this.inShowdown = inShowdown;
    }

    public void startShowdown() {
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

            user.getPlayer().teleport(teleport);
        }

        setInShowdown(true);

        plugin.messageAll(ChatColor.BOLD.toString() + ChatColor.GOLD + "Showdown starts NOW!");
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
        getTeam(building.getTeamColor()).removeBuilding(building);

        buildingCentres.remove(building.getCenterBlock());

        for (User info : plugin.getUsers()) {
            if (info.getTeamColor() != building.getTeamColor()) continue;

            info.redoShopInventory();
        }

        building.clearHolograms();
    }

    public boolean isInBuilding(Location loc){
        for(Building building : buildings){
            if(building.getBounds().containsLocation(loc)) return true;
        }

        return false;
    }

    public void updateScoutCompass(ItemStack item, Player player, TeamColor exclude) {
        InventoryUtils.setItemNameAndLore(item, "Locating closest player...");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location closest = null;
            double minDist = 99999999999d;
            String closestName = null;

            for (User info : plugin.getUsers()) {
                if (!info.isInGame() || info.getTeamColor() == exclude) continue;

                double dist = player.getLocation().distanceSquared(info.getPlayer().getLocation());

                if (dist < minDist) {
                    minDist = dist;
                    closest = info.getPlayer().getLocation();
                    closestName = info.getPlayer().getName();
                }
            }


            if (closest != null) player.setCompassTarget(closest);
            else closestName = "No One";

            int compassIndex = player.getInventory().first(Material.COMPASS);
            ItemStack newCompass = player.getInventory().getItem(compassIndex);

            InventoryUtils.setItemNameAndLore(newCompass, "Player Compass", "Oriented at: " + closestName);

            player.getInventory().setItem(compassIndex, newCompass);
        }, 60);
    }

    public void onPlayerUpgrade(UserUpgradeEvent event) {
        PlayerClassHandler classHandler = plugin.getPlayerClassHandler(event.getUser().getPlayerClass());

        classHandler.onPlayerUpgrade(event);
    }

    public void handleSpectatorInventory(InventoryClickEvent event) {
        event.setCancelled(true);

        plugin.getSpectatorInventoryHandler()
                .onInventoryClick(event.getCurrentItem(), null, plugin.getUser((Player) event.getWhoClicked()), null);
    }

    public void updateSpectatorInventories() {
        for (User info : plugin.getUsers()) {
            if (info.isInGame() && info.getTeamColor() != null) return;

            setupSpectatorInventory(info.getPlayer());
        }
    }

    public void setupSpectatorInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        int slot = 9;

        List<ItemStack> items = new ArrayList<>();
        plugin.getSpectatorInventoryHandler().addInventoryItems(items, null, plugin.getUser(player), null);

        for (ItemStack item : items) {
            inv.setItem(slot++, item);
        }
    }

    public void finishBuilding(Building building) {
        if (!getTeam(building.getTeamColor()).everHadBuilding(building.getBuildingName())) {
            for (User info : plugin.getUsers()) {
                if (info.getTeamColor() != building.getTeamColor()) continue;

                info.redoShopInventory();

                PlayerClassHandler playerClassHandler = plugin.getPlayerClassHandler(info.getPlayerClass());
                playerClassHandler.onBuildingBuilt(building.getBuildingName(), info, getTeam(info.getTeamColor()));
            }
        }

        getTeam(building.getTeamColor()).buildingFinished(building);
    }

}
