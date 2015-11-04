package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by paul on 01/11/15.
 *
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    private HashMap<UUID, PlayerInfo> playerInfoHashMap = new HashMap<>();
    private EnumMap<TeamColor, TeamInfo> teamInfoEnumMap = new EnumMap<>(TeamColor.class);
    private HashMap<String, SchematicData> schematicDataHashMap = new HashMap<>();
    private List<BuildingInfo> buildings = new ArrayList<>();
    private Random random = new Random();

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        WarsListener pluginListener = new WarsListener(this);
        getServer().getPluginManager().registerEvents(pluginListener, this);

        for(TeamColor c : TeamColor.values()){
            teamInfoEnumMap.put(c, new TeamInfo(this, c));
        }

        schematicDataHashMap.put("Base", new SchematicData("Base", "mccw_base.schematic"));
        schematicDataHashMap.put("Farm", new SchematicData("Farm", "mccw_farm.schematic"));
    }

    public SchematicData getSchematicData(String buildingName){
        return schematicDataHashMap.get(buildingName);
    }

    public PlayerInfo getPlayerInfo(Player player){
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo){
        if(playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

    public Random getRandom() {
        return random;
    }

    public TeamInfo getTeamInfo(TeamColor teamColor){
        return teamInfoEnumMap.get(teamColor);
    }

    public void setPlayerTeam(Player player, TeamColor teamColor){
        PlayerInfo playerInfo = getPlayerInfo(player);

        if(playerInfo.getTeamColor() != null){
            getTeamInfo(playerInfo.getTeamColor()).removePlayer(player);
        }

        playerInfo.setTeamColor(teamColor);
        getTeamInfo(teamColor).addPlayer(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute Colony Wars commands");
            return true;
        }

        Player player = (Player) sender;

        switch(command.getName().toLowerCase()){
            case "transfer":
                if(args.length < 1) return false;

                try {
                    int amount = Integer.parseInt(args[0]);

                    PlayerInfo playerInfo = getPlayerInfo(player);
                    if(!playerInfo.subtractPlayerCash(amount)){
                        player.sendMessage("You do not have that amount of money");
                        return true;
                    }

                    TeamInfo teamInfo = getTeamInfo(playerInfo.getTeamColor());
                    teamInfo.addTeamCash(amount);

                    return true;
                } catch(NumberFormatException e){
                    return false;
                }

            default:
                return false;
        }

    }
}
