package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.util.CannonTowerHandler;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.team.Team;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by paul on 08/01/16.
 */
public class CWTeamStats extends Metadata {

    private final Random random = new Random();

    private final Team team;

    private final HashMap<String, Integer> buildingCounts = new HashMap<>();
    private final HashMap<String, Integer> buildingNowCounts = new HashMap<>();
    private final HashMap<String, Boolean> hadBuildings = new HashMap<>();

    private int buildingsConstructingNow = 0;

    private final List<Location> churchLocations = new ArrayList<>();
    private int respawnChance;

    private Location baseLocation;

    private final Location spawnLocation;

    private final String respawnChanceLocale;
    private final String eliminatedLocale;

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

        Config spawnLocations = team.getSharedObject("spawn_locations");

        Vector spawnLocation = ConfigUtils.getVector(spawnLocations, team.getName());
        this.spawnLocation = new Location(team.getGameGroup().getCurrentMap().getWorld(), spawnLocation.getX(),
                spawnLocation.getY(), spawnLocation.getZ());

        Config metadata = team.getSharedObjectOrEmpty("team_stats_metadata");

        respawnChanceLocale = metadata.getString("respawn_chance_locale", "respawn.chance");
        eliminatedLocale = metadata.getString("team_eliminated_locale", "team.eliminated");
    }

    public Location getSpawnLocation() {
        return spawnLocation;
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

        Config config = building.getSchematic().getConfig();
        if(config != null) {
            if(config.contains("base")) baseLocation = building.getCenterBlock();
            if(config.contains("revival_rate")) {
                addChurchLocation(building.getCenterBlock(), config.getInt("revival_rate"));

                //The baseLocation is only used as a church while a cathedral is building
                removeChurchLocation(baseLocation);
            }
            if(config.getBoolean("cannons", false)) {
                CannonTowerHandler.startCannonTowerTask(team.getGameGroup(), building);
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

        removeChurchLocation(building.getCenterBlock());
    }

    public void removeChurchLocation(Location church) {
        if (churchLocations.remove(church)) {
            if (churchLocations.isEmpty()) setRespawnChance(0, true);
        }
    }

    public void addChurchLocation(Location church, int minRevivalRate) {
        churchLocations.add(church);

        if(minRevivalRate <= respawnChance) return;

        setRespawnChance(minRevivalRate, true);
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

        if(message) team.sendLocale(respawnChanceLocale, respawnChance);

        team.updateUserScoreboards();
    }

    public void respawnUser(User died) {
        Location loc;
        if(churchLocations.size() > 0) loc = churchLocations.get(random.nextInt(churchLocations.size()));
        else loc = baseLocation;

        died.teleport(loc);

        died.decloak();
    }

    public void eliminate() {
        team.getGameGroup().sendLocale(eliminatedLocale, team.getFormattedName());

        if(baseLocation != null) {
            Building base = BuildingController.getOrCreate(team.getGameGroup()).getBuilding(baseLocation);
            if(base != null) base.explode();
            else System.out.println("Team " + team.getName() + " missing base for destruction");
            baseLocation = null;
        }

        TeamStatsHolderGroup.getOrCreate(team).addGameLoss();
    }


}
