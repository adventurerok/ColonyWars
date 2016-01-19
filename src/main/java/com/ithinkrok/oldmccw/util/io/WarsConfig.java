package com.ithinkrok.oldmccw.util.io;

import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.minigames.util.ConfigUtils;
import com.ithinkrok.oldmccw.data.TeamColor;
import com.ithinkrok.oldmccw.enumeration.PlayerClass;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by paul on 28/11/15.
 * <p>
 * Represents a Colony Wars configuration
 */
public class WarsConfig {

    private ConfigAccessor configAccessor;

    public WarsConfig(ConfigAccessor configAccessor) {
        this.configAccessor = configAccessor;
    }

    public int getSpleefExtraRadius() {
        return config().getInt("lobby-games.spleef.extra-radius");
    }

    private ConfigurationSection config() {
        return configAccessor.getConfig();
    }

    public String getRandomMapName() {
        return config().getString("random-map");
    }

    public List<String> getMapList() {
        return config().getStringList("map-list");
    }

    public String getGameMapFolder() {
        return config().getString("playing-map");
    }

    public String getLobbyMapFolder() {
        return config().getString("lobby-map");
    }

    public String getLanguageName() {
        return config().getString("language");
    }

    public int getTeamCount() {
        return config().getInt("team-count");
    }

    public int getShowdownStartTeams() {
        return config().getInt("force-showdown.teams");
    }

    public int getShowdownStartPlayers() {
        return config().getInt("force-showdown.players");
    }

    public int getShowdownStartTimeNoAttackSinceStart() {
        return config().getInt("force-showdown.times.start");
    }

    public int getShowdownStartTimeSinceLastAttack() {
        return config().getInt("force-showdown.times.attack");
    }

    public int getShowdownStartTimeSinceLastDeath() {
        return config().getInt("force-showdown.times.death");
    }

    public String getMapFolder(String mapName) {
        return config().getString("maps." + mapName + ".folder");
    }

    public World.Environment getMapEnvironment(String mapName) {
        String envName = config().getString("maps." + mapName + ".environment", "normal").toUpperCase();

        return World.Environment.valueOf(envName);
    }

    public int getBuildingCost(String buildingName) {
        return getCost("buildings", buildingName);
    }

    public int getCost(String itemGroup, String item) {
        return config().getInt("costs." + itemGroup + "." + item);
    }

    public int getBuildingItemCost(String buildingName, String item) {
        return getCost(buildingName, item);
    }

    public int getClassItemCost(PlayerClass playerClass, String item) {
        return getClassItemCost(playerClass.getName(), item);
    }

    public int getClassItemCost(String playerClassName, String item) {
        return getCost(playerClassName, item);
    }

    public int getBuildingItemAmount(String buildingName, String item) {
        return config().getInt("amounts." + buildingName + "." + item);
    }

    public Vector getShowdownSize(String mapName) {
        return getVector("maps." + mapName + ".showdown-size");
    }

    public Vector getVector(String path) {
        return new Vector(config().getDouble(path + ".x"), config().getDouble(path + ".y"),
                config().getDouble(path + ".z"));
    }

    public Vector getBaseLocation(String mapName, TeamColor team) {
        return getVector("maps." + mapName + "." + team.getName() + ".base");
    }

    public Vector getTeamSpawnLocation(String mapName, TeamColor team) {
        return getVector("maps." + mapName + "." + team.getName() + ".spawn");
    }

    public Vector getMapCenter(String mapName) {
        return getVector("maps." + mapName + ".center");
    }

    public boolean hasPersistence() {
        return config().getBoolean("persistence");
    }

    public int getDeathScoreModifier() {
        return getScoreModifier("death");
    }

    public int getScoreModifier(String event) {
        return config().getInt("score." + event);
    }

    public int getKillScoreModifier() {
        return getScoreModifier("kill");
    }

    public int getWinScoreModifier() {
        return getScoreModifier("win");
    }

    public int getLossScoreModifier() {
        return getScoreModifier("loss");
    }

    public int getParkourMoneyCap() {
        return config().getInt("lobby-games.parkour.cap");
    }

    public int getLastAttackerTimer() {
        return config().getInt("last-attacker-timer");
    }

    public List<Vector> getSpleefQueueButtonLocations() {
        return getVectorList("lobby-games.spleef.queue-buttons");
    }

    private List<Vector> getVectorList(String path) {
        return ConfigUtils.getVectorList(config(), path);
    }

    public List<ConfigurationSection> getParkourConfigs() {
        return getConfigList("lobby-games.parkour.runs");
    }

    @SuppressWarnings("unchecked")
    private List<ConfigurationSection> getConfigList(String path) {
        return ConfigUtils.getConfigList(config(), path);
    }


    public List<Vector> getSpleefSpawnLocations() {
        return getVectorList("lobby-games.spleef.spawn-locations");
    }

    public Vector getSpleefExitLocation() {
        return getVector("lobby-games.spleef.exit-location");
    }

    public BoundingBox getSpleefSnowBounds() {
        return getBounds("lobby-games.spleef.snow");
    }

    private BoundingBox getBounds(String path) {
        Vector min = getVector(path + ".min");
        Vector max = getVector(path + ".max");

        if (min == null || max == null) return null;
        return new BoundingBox(min, max);
    }

    public interface ConfigAccessor {
        ConfigurationSection getConfig();
    }
}
