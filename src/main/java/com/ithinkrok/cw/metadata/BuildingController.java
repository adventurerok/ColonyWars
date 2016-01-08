package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.GameGroup;
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

    private GameGroup gameGroup;

    private HashMap<PastedSchematic, Building> buildings = new HashMap<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

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

    }

    public void addBuilding(Building building) {
        buildings.put(building.getSchematic(), building);

        if(building.getSchematic().getCenterBlock() != null) {
            buildingCentres.put(building.getSchematic().getCenterBlock(), building);
        }

        if(building.getSchematic().isFinished()) {
            onSchematicFinished(new SchematicFinishedEvent(building.getSchematic()));
        }
    }

    @EventHandler
    public void onSchematicDestroyed(SchematicDestroyedEvent event) {
        Building building = buildings.remove(event.getSchematic());
        buildingCentres.values().remove(building);
    }
}
