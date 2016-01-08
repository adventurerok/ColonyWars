package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.metadata.Metadata;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 08/01/16.
 */
public class TeamBuildingStats extends Metadata {

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

    public TeamBuildingStats(Team team) {
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

        buildingNowCounts.put(building.getBuildingName(), getBuildingCount(building.getBuildingName()) + 1);

        team.updateUserScoreboards();
    }

    public void buildingFinished(Building building) {
        buildingsConstructingNow -= 1;

        int buildingNowOfType = getBuildingNowCount(building.getBuildingName()) - 1;
        if(buildingNowOfType > 0) buildingNowCounts.put(building.getBuildingName(), buildingNowOfType);
        else buildingNowCounts.remove(building.getBuildingName());

        buildingCounts.put(building.getBuildingName(), getBuildingCount(building.getBuildingName()) + 1);
        hadBuildings.put(building.getBuildingName(), true);

        //TODO setup church, cathedral, base, cannon tower, fortress

        team.updateUserScoreboards();
    }

    public boolean everHadBuilding(String buildingName){
        Boolean bool = hadBuildings.get(buildingName);

        return bool == null ? false : bool;
    }

    public void buildingRemoved(Building building) {
        buildingCounts.put(building.getBuildingName(), Math.max(getBuildingCount(building.getBuildingName()) - 1, 0));

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

    public static TeamBuildingStats getOrCreate(Team team) {
        TeamBuildingStats stats = team.getMetadata(TeamBuildingStats.class);

        if(stats == null) {
            stats = new TeamBuildingStats(team);
            team.setMetadata(stats);
        }

        return stats;
    }
}
