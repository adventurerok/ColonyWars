package com.ithinkrok.mccw.util.io;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by paul on 28/11/15.
 * <p>
 * Represents a Colony Wars configuration
 */
public class WarsConfig {

    private WarsPlugin plugin;

    public WarsConfig(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    public String getRandomMapName() {
        return config().getString("random-map");
    }

    private ConfigurationSection config() {
        return plugin.getMapConfig();
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

    public String getMapFolder(String mapName) {
        return config().getString("maps." + mapName + ".folder");
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

    public Vector getVector(String path) {
        return new Vector(config().getDouble(path + ".x"), config().getDouble(path + ".y"),
                config().getDouble(path + ".z"));
    }

    public Vector getShowdownSize(String mapName){
        return getVector("maps." + mapName + ".showdown-size");
    }

    public Vector getBaseLocation(String mapName, TeamColor team){
        return getVector("maps." + mapName + "." + team.getName() + ".base");
    }

    public Vector getTeamSpawnLocation(String mapName, TeamColor team){
        return getVector("maps." + mapName + "." + team.getName() + ".spawn");
    }

    public Vector getMapCenter(String mapName){
        return getVector("maps." + mapName + ".center");
    }

    public boolean hasPersistence(){
        return config().getBoolean("persistence");
    }

    public int getDeathScoreModifier(){
        return getScoreModifier("death");
    }

    public int getKillScoreModifier(){
        return getScoreModifier("kill");
    }

    public int getWinScoreModifier(){
        return getScoreModifier("win");
    }

    public int getLossScoreModifier(){
        return getScoreModifier("loss");
    }

    public int getScoreModifier(String event){
        return config().getInt("score." + event);
    }
}
