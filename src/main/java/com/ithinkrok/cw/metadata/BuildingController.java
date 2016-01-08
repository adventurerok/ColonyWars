package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.metadata.Metadata;
import com.ithinkrok.minigames.schematic.PastedSchematic;
import com.ithinkrok.minigames.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.schematic.event.SchematicFinishedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * Created by paul on 08/01/16.
 */
public class BuildingController extends Metadata implements Listener {

    private final GameGroup gameGroup;

    private HashMap<PastedSchematic, Building> buildings = new HashMap<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

    public BuildingController(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    @EventHandler
    public void onSchematicFinished(SchematicFinishedEvent event) {
        Building building = buildings.get(event.getSchematic());
        if(building == null) return;

        getTeamBuildingStats(building).buildingFinished(building);
    }

    public void addBuilding(Building building) {
        buildings.put(building.getSchematic(), building);

        if(building.getSchematic().getCenterBlock() != null) {
            buildingCentres.put(building.getSchematic().getCenterBlock(), building);
        }

        getTeamBuildingStats(building).buildingStarted(building);

        if(building.getSchematic().isFinished()) {
            onSchematicFinished(new SchematicFinishedEvent(building.getSchematic()));
        }
    }

    private TeamBuildingStats getTeamBuildingStats(Building building) {
        Team team = gameGroup.getTeam(building.getTeamIdentifier());

        return TeamBuildingStats.getOrCreate(team);
    }

    @EventHandler
    public void onSchematicDestroyed(SchematicDestroyedEvent event) {
        Building building = buildings.remove(event.getSchematic());
        buildingCentres.values().remove(building);

        getTeamBuildingStats(building).buildingRemoved(building);
    }

    public static BuildingController getOrCreate(GameGroup gameGroup) {
        BuildingController result = gameGroup.getMetadata(BuildingController.class);

        if(result == null) {
            result = new BuildingController(gameGroup);
            gameGroup.setMetadata(result);
        }

        return result;
    }
}
