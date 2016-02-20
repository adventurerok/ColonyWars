package com.ithinkrok.cw.metadata;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.schematic.PastedSchematic;
import com.ithinkrok.minigames.base.schematic.Schematic;
import com.ithinkrok.minigames.base.schematic.SchematicOptions;
import com.ithinkrok.minigames.base.schematic.SchematicPaster;
import com.ithinkrok.minigames.base.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.base.schematic.event.SchematicFinishedEvent;
import com.ithinkrok.minigames.api.Team;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.util.BoundingBox;
import com.ithinkrok.minigames.base.util.LocationChecker;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;

import java.util.HashMap;

/**
 * Created by paul on 08/01/16.
 */
public class BuildingController extends Metadata implements CustomListener, LocationChecker, SchematicPaster.BoundsChecker {

    private final GameGroup gameGroup;

    private final HashMap<PastedSchematic, Building> buildings = new HashMap<>();
    private final HashMap<Location, Building> buildingCentres = new HashMap<>();

    private final String shopLocale;
    private final String shopInfoLocale;

    public BuildingController(GameGroup gameGroup) {
        this.gameGroup = gameGroup;

        Config config = gameGroup.getSharedObjectOrEmpty("building_controller_metadata");

        shopLocale = config.getString("shop_name_locale", "building.shop.name");
        shopInfoLocale = config.getString("shop_description_locale", "building.shop.desc");
    }

    @Override
    public void removed() {
        for (Building building : buildings.values()) {
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

    public Building getBuilding(Location center) {
        return buildingCentres.get(center);
    }

    public boolean buildBuilding(String name, TeamIdentifier team, Location location, int rotation, boolean instant) {
        SchematicOptions options = createSchematicOptions(team, instant);

        Schematic schem = gameGroup.getSchematic(name);
        GameMap map = gameGroup.getCurrentMap();

        PastedSchematic pasted =
                SchematicPaster.buildSchematic(schem, map, location, this, gameGroup, gameGroup, rotation, options);

        if (pasted == null) return false;

        Building building = new Building(name, team, pasted);
        addBuilding(building);

        return true;
    }

    private SchematicOptions createSchematicOptions(TeamIdentifier team, boolean instant) {
        SchematicOptions options = new SchematicOptions(gameGroup.getSharedObject("schematic_options"));
        options.withOverrideDyeColor(team.getDyeColor());
        options.withMapBoundsCheck(false); //we do our own Map bounds check

        if (instant) options.withBuildSpeed(-1);

        options.withDefaultListener(BuildingController.getOrCreate(gameGroup));

        return options;
    }

    public void addBuilding(Building building) {
        buildings.put(building.getSchematic(), building);

        if (building.getSchematic().getCenterBlock() != null) {
            buildingCentres.put(building.getSchematic().getCenterBlock(), building);
        }

        getTeamBuildingStats(building).buildingStarted(building);

        if (building.getSchematic().isFinished()) {
            onSchematicFinished(new SchematicFinishedEvent(building.getSchematic()));
        }
    }

    public static BuildingController getOrCreate(GameGroup gameGroup) {
        BuildingController result = gameGroup.getMetadata(BuildingController.class);

        if (result == null) {
            result = new BuildingController(gameGroup);
            gameGroup.setMetadata(result);
        }

        return result;
    }

    private CWTeamStats getTeamBuildingStats(Building building) {
        Team team = gameGroup.getTeam(building.getTeamIdentifier());

        return CWTeamStats.getOrCreate(team);
    }

    @CustomEventHandler
    public void onSchematicFinished(SchematicFinishedEvent event) {
        Building building = buildings.get(event.getSchematic());
        if (building == null || building.getCenterBlock() == null) return;

        Location holo1 = building.getCenterBlock().clone().add(0.5d, 2.2d, 0.5d);
        Hologram hologram1 =
                HologramAPI.createHologram(holo1, gameGroup.getLocale(shopLocale, building.getBuildingName()));
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

    @CustomEventHandler
    public void onSchematicDestroyed(SchematicDestroyedEvent event) {
        Building building = buildings.remove(event.getSchematic());
        buildingCentres.values().remove(building);

        getTeamBuildingStats(building).buildingRemoved(building);
    }

    @Override
    /**
     * Checks if the location is part of a building
     */ public boolean check(Location loc) {
        return gameGroup.getCurrentMap().canPaste(new BoundingBox(loc.toVector(), loc.toVector()));
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        GameMap map = gameGroup.getCurrentMap();

        for (int x = bounds.min.getBlockX(); x <= bounds.max.getBlockX(); ++x) {
            for (int y = bounds.min.getBlockY(); y <= bounds.max.getBlockY(); ++y) {
                for (int z = bounds.min.getBlockZ(); z <= bounds.max.getBlockZ(); ++z) {
                    switch (map.getBlock(x, y, z).getType()) {
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
