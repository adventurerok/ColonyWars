package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.BaseInventory;
import com.ithinkrok.mccw.inventory.FarmInventory;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.GeneralClass;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;


/**
 * Created by paul on 01/11/15.
 * <p>
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    private HashMap<UUID, PlayerInfo> playerInfoHashMap = new HashMap<>();
    private EnumMap<TeamColor, TeamInfo> teamInfoEnumMap = new EnumMap<>(TeamColor.class);
    private HashMap<String, SchematicData> schematicDataHashMap = new HashMap<>();
    private List<BuildingInfo> buildings = new ArrayList<>();
    private HashMap<Location, BuildingInfo> buildingCentres = new HashMap<>();
    private HashMap<String, InventoryHandler> buildingInventories = new HashMap<>();
    private EnumMap<PlayerClass, PlayerClassHandler> classHandlerEnumMap = new EnumMap<>(PlayerClass.class);
    private Random random = new Random();

    public double getMaxHealth() {
        return (double) 40;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        WarsListener pluginListener = new WarsListener(this);
        getServer().getPluginManager().registerEvents(pluginListener, this);

        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new TeamInfo(this, c));
        }

        schematicDataHashMap.put("Base", new SchematicData("Base", "mccw_base.schematic"));
        schematicDataHashMap.put("Farm", new SchematicData("Farm", "mccw_farm.schematic"));
        schematicDataHashMap.put("Blacksmith", new SchematicData("Blacksmith", "mccw_blacksmith.schematic"));
        schematicDataHashMap.put("MageTower", new SchematicData("MageTower", "mccw_magetower.schematic"));
        schematicDataHashMap.put("Lumbermill", new SchematicData("Lumbermill", "mccw_lumbermill.schematic"));

        buildingInventories.put("Base", new BaseInventory());
        buildingInventories.put("Farm", new FarmInventory());

        classHandlerEnumMap.put(PlayerClass.GENERAL, new GeneralClass());
    }

    public SchematicData getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public InventoryHandler getInventoryHandler(String building) {
        return buildingInventories.get(building);
    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo) {
        if (playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

    public Random getRandom() {
        return random;
    }

    public void setPlayerTeam(Player player, TeamColor teamColor) {
        PlayerInfo playerInfo = getPlayerInfo(player);

        if (playerInfo.getTeamColor() != null) {
            getTeamInfo(playerInfo.getTeamColor()).removePlayer(player);
        }

        playerInfo.setTeamColor(teamColor);
        getTeamInfo(teamColor).addPlayer(player);
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public TeamInfo getTeamInfo(TeamColor teamColor) {
        return teamInfoEnumMap.get(teamColor);
    }

    public void addBuilding(BuildingInfo buildingInfo) {
        buildings.add(buildingInfo);

        if (getTeamInfo(buildingInfo.getTeamColor()).getBuildingCount(buildingInfo.getBuildingName()) == 0) {
            for (PlayerInfo info : playerInfoHashMap.values()) {
                if (info.getTeamColor() != buildingInfo.getTeamColor()) continue;

                PlayerClassHandler playerClassHandler = getPlayerClassHandler(info.getPlayerClass());
                playerClassHandler
                        .onBuildingBuilt(buildingInfo.getBuildingName(), info, getTeamInfo(info.getTeamColor()));
            }
        }

        getTeamInfo(buildingInfo.getTeamColor()).addBuilding(buildingInfo.getBuildingName());

        if (buildingInfo.getCenterBlock() != null) buildingCentres.put(buildingInfo.getCenterBlock(), buildingInfo);
    }

    public PlayerClassHandler getPlayerClassHandler(PlayerClass playerClass) {
        return classHandlerEnumMap.get(playerClass);
    }

    public BuildingInfo getBuildingInfo(Location center) {
        return buildingCentres.get(center);
    }

    public boolean canBuild(Vector minBB, Vector maxBB) {
        for (BuildingInfo building : buildings) {
            if (!building.canBuild(minBB, maxBB)) return false;
        }

        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute Colony Wars commands");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "transfer":
                if (args.length < 1) return false;

                try {
                    int amount = Integer.parseInt(args[0]);

                    PlayerInfo playerInfo = getPlayerInfo(player);
                    if (!playerInfo.subtractPlayerCash(amount)) {
                        player.sendMessage("You do not have that amount of money");
                        return true;
                    }

                    TeamInfo teamInfo = getTeamInfo(playerInfo.getTeamColor());
                    teamInfo.addTeamCash(amount);

                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }

            default:
                return false;
        }

    }

    public void removeBuilding(BuildingInfo buildingInfo) {
        buildings.remove(buildingInfo);
        getTeamInfo(buildingInfo.getTeamColor()).removeBuilding(buildingInfo.getBuildingName());

        buildingCentres.remove(buildingInfo.getCenterBlock());
    }

    public void setupPlayers() {
        for(PlayerInfo info : playerInfoHashMap.values()){
            PlayerClassHandler classHandler = getPlayerClassHandler(info.getPlayerClass());

            classHandler.onGameBegin(info, getTeamInfo(info.getTeamColor()));
        }
    }
}
