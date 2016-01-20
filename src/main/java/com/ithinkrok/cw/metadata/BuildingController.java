package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.metadata.Metadata;
import com.ithinkrok.minigames.schematic.PastedSchematic;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.schematic.SchematicOptions;
import com.ithinkrok.minigames.schematic.SchematicPaster;
import com.ithinkrok.minigames.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.schematic.event.SchematicFinishedEvent;
import com.ithinkrok.minigames.team.Team;
import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.minigames.util.LocationChecker;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * Created by paul on 08/01/16.
 */
public class BuildingController extends Metadata implements Listener, LocationChecker, SchematicPaster.BoundsChecker {

    private final GameGroup gameGroup;

    private HashMap<PastedSchematic, Building> buildings = new HashMap<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

    private String shopLocale;
    private String shopInfoLocale;

    public BuildingController(GameGroup gameGroup) {
        this.gameGroup = gameGroup;

        ConfigurationSection config = gameGroup.getSharedObject("building_controller_metadata");
        if(config == null) config = new MemoryConfiguration();

        shopLocale = config.getString("shop_name_locale", "building.shop.name");
        shopInfoLocale = config.getString("shop_description_locale", "building.shop.desc");
    }

    @Override
    public void removed() {
        for(Building building : buildings.values()) {
            building.getSchematic().removed();
        }
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    @MinigamesEventHandler
    public void onSchematicFinished(SchematicFinishedEvent event) {
        Building building = buildings.get(event.getSchematic());
        if(building == null) return;

        Location holo1 = building.getCenterBlock().clone().add(0.5d, 2.2d, 0.5d);
        Hologram hologram1 = HologramAPI.createHologram(holo1, gameGroup.getLocale(shopLocale, building
                .getBuildingName()));
        hologram1.spawn();
        building.getSchematic().addHologram(hologram1);

        Location holo2 = building.getCenterBlock().clone().add(0.5d, 1.9d, 0.5d);
        Hologram hologram2 = HologramAPI.createHologram(holo2, gameGroup.getLocale(shopInfoLocale));
        hologram2.spawn();
        building.getSchematic().addHologram(hologram2);

        getTeamBuildingStats(building).buildingFinished(building);

        Team team = gameGroup.getTeam(building.getTeamIdentifier());
        gameGroup.teamEvent(new BuildingBuiltEvent(team, building));
    }

    public Building getBuilding(Location center) {
        return buildingCentres.get(center);
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

    public boolean buildBuilding(String name, TeamIdentifier team, Location location, int rotation, boolean instant) {
        SchematicOptions options = createSchematicOptions(team, instant);

        Schematic schem = gameGroup.getSchematic(name);
        GameMap map = gameGroup.getCurrentMap();

        PastedSchematic pasted = SchematicPaster
                .buildSchematic(schem, map, location, this, gameGroup, gameGroup, rotation,
                        options);

        if(pasted == null) return false;

        Building building = new Building(name, team, pasted);
        addBuilding(building);

        return true;
    }

    private SchematicOptions createSchematicOptions(TeamIdentifier team, boolean instant) {
        SchematicOptions options = new SchematicOptions(gameGroup.getSharedObject("schematic_options"));
        options.withOverrideDyeColor(team.getDyeColor());
        options.withMapBoundsCheck(false); //we do our own Map bounds check

        if(instant) options.withBuildSpeed(-1);

        options.withDefaultListener(BuildingController.getOrCreate(gameGroup));

        return options;
    }

    private CWTeamStats getTeamBuildingStats(Building building) {
        Team team = gameGroup.getTeam(building.getTeamIdentifier());

        return CWTeamStats.getOrCreate(team);
    }

    @MinigamesEventHandler
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

    @Override
    /**
     * Checks if the location is part of a building
     */
    public boolean check(Location loc) {
        return gameGroup.getCurrentMap().canPaste(new BoundingBox(loc.toVector(), loc.toVector()));
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        GameMap map = gameGroup.getCurrentMap();

        for(int x = bounds.min.getBlockX(); x <= bounds.max.getBlockX(); ++x) {
            for(int y = bounds.min.getBlockY(); y <= bounds.max.getBlockY(); ++y) {
                for(int z = bounds.min.getBlockZ(); z <= bounds.max.getBlockZ(); ++z) {
                    switch(map.getBlock(x, y, z).getType()) {
                        case BEDROCK:
                        case BARRIER:
                            return false;
                    }
                }
            }
        }

        return map.canPaste(bounds);
    }
}
