package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.metadata.Metadata;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 08/01/16.
 */
public class CWTeamStats extends Metadata {

    private final Team team;

    private HashMap<String, Integer> buildingCounts = new HashMap<>();
    private HashMap<String, Integer> buildingNowCounts = new HashMap<>();
    private HashMap<String, Boolean> hadBuildings = new HashMap<>();

    private int buildingsConstructingNow = 0;

    private List<Location> churchLocations = new ArrayList<>();
    private int respawnChance;

    private Location baseLocation;

    public Location getBaseLocation() {
        return baseLocation;
    }

    public int getRespawnChance() {
        return respawnChance;
    }

    public HashMap<String, Integer> getBuildingNowCounts() {
        return buildingNowCounts;
    }

    public CWTeamStats(Team team) {
        this.team = team;
    }

    public int getTotalBuildingNowCount() {
        return buildingsConstructingNow;
    }

    public int getBuildingCount(String buildingType){
        Integer integer = buildingCounts.get(buildingType);

        return integer == null ? 0 : integer;
    }

    public int getBuildingNowCount(String buildingType){
        Integer integer = buildingNowCounts.get(buildingType);

        return integer == null ? 0 : integer;
    }

    public void buildingStarted(Building building) {
        buildingsConstructingNow += 1;

        buildingNowCounts.put(building.getBuildingName(), getBuildingNowCount(building.getBuildingName()) + 1);

        team.updateUserScoreboards();
    }

    public void buildingFinished(Building building) {
        buildingsConstructingNow -= 1;

        int buildingNowOfType = getBuildingNowCount(building.getBuildingName()) - 1;
        if(buildingNowOfType > 0) buildingNowCounts.put(building.getBuildingName(), buildingNowOfType);
        else buildingNowCounts.remove(building.getBuildingName());

        buildingCounts.put(building.getBuildingName(), getBuildingCount(building.getBuildingName()) + 1);
        hadBuildings.put(building.getBuildingName(), true);

        ConfigurationSection config = building.getSchematic().getConfig();
        if(config != null) {
            if(config.contains("base")) baseLocation = building.getCenterBlock();
            if(config.contains("revival_rate")) {
                setRespawnChance(Math.max(respawnChance, config.getInt("revival_rate")), true);
                churchLocations.add(building.getCenterBlock());
            }
        }

        team.updateUserScoreboards();
    }

    public boolean everHadBuilding(String buildingName){
        Boolean bool = hadBuildings.get(buildingName);

        return bool == null ? false : bool;
    }

    public void buildingRemoved(Building building) {
        buildingCounts.put(building.getBuildingName(), Math.max(getBuildingCount(building.getBuildingName()) - 1, 0));

        if (churchLocations.remove(building.getCenterBlock())) {
            if (churchLocations.isEmpty()) setRespawnChance(0, true);
        }

        //TODO stop cannon towers
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    public static CWTeamStats getOrCreate(Team team) {
        CWTeamStats stats = team.getMetadata(CWTeamStats.class);

        if(stats == null) {
            stats = new CWTeamStats(team);
            team.setMetadata(stats);
        }

        return stats;
    }

    public void setRespawnChance(int respawnChance, boolean message) {
        this.respawnChance = respawnChance;

        //TODO message players
    }
}
