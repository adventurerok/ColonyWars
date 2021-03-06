package com.ithinkrok.cw.metadata;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.schematic.SchematicOptions;
import com.ithinkrok.minigames.api.schematic.SchematicPaster;
import com.ithinkrok.minigames.api.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.api.schematic.event.SchematicFinishedEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.HologramUtils;
import com.ithinkrok.minigames.api.util.LocationChecker;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;

/**
 * Created by paul on 08/01/16.
 */
public class BuildingController extends Metadata
        implements CustomListener, LocationChecker, SchematicPaster.BoundsChecker {

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


    public Building buildBuilding(String name, TeamIdentifier team, Location location, int rotation, boolean instant,
                                  boolean force) {
        return buildBuilding(name, team, location, rotation, instant, force, -1);
    }


    public Building buildBuilding(String name, TeamIdentifier team, Location location, int rotation, boolean instant,
                                  boolean force, int speed) {
        Schematic schem = gameGroup.getSchematic(name);

        SchematicOptions options = createSchematicOptions(team, instant, speed, schem);

        GameMap map = gameGroup.getCurrentMap();

        //No bounds checker means the check automatically passes
        SchematicPaster.BoundsChecker boundsChecker = force ? null : this;

        PastedSchematic pasted = SchematicPaster
                .buildSchematic(schem, map, location, boundsChecker, gameGroup, gameGroup, rotation, options);

        if (pasted == null) return null;

        Building building = new Building(name, team, pasted);
        addBuilding(building);

        return building;
    }


    private SchematicOptions createSchematicOptions(TeamIdentifier team, boolean instant, int speed, Schematic schem) {
        SchematicOptions options = new SchematicOptions(gameGroup.getSharedObject("schematic_options"));
        options.withOverrideDyeColor(team.getDyeColor());
        options.withMapBoundsCheck(false); //we do our own Map bounds check
        if (speed > 0) options.withBuildSpeed(speed);

        if (schem.getConfig() != null && schem.getConfig().contains("center_block")) {
            options.withCenterBlockType(Material.matchMaterial(schem.getConfig().getString("center_block")));
        }

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

        if (building.getConfig() == null || building.getConfig().getBoolean("hologram", true)) {

            gameGroup.doInFuture(task -> {
                Location holoLoc = building.getCenterBlock().clone().add(0.5d, 1.8d, 0.5d);
                Hologram hologram = HologramUtils.createHologram(holoLoc);

                hologram.appendTextLine(gameGroup.getLocale(shopLocale, building.getBuildingName()));
                hologram.appendTextLine(gameGroup.getLocale(shopInfoLocale));

                building.getSchematic().addHologram(hologram);

            }, 10);

        }

        getTeamBuildingStats(building).buildingFinished(building);

        Team team = gameGroup.getTeam(building.getTeamIdentifier());
        gameGroup.teamEvent(new BuildingBuiltEvent(team, building));
    }


    @CustomEventHandler
    public void onSchematicDestroyed(SchematicDestroyedEvent event) {
        Building building = buildings.remove(event.getSchematic());
        buildingCentres.values().remove(building);

        if (building == null) {
            System.out.println("Null building destroyed of Type" + event.getSchematic().getName());
            return;
        }

        getTeamBuildingStats(building).buildingRemoved(building);
    }


    /**
     * Checks if the location is part of a building
     */
    @Override
    public boolean check(Location loc) {
        return gameGroup.getCurrentMap().canPaste(new BoundingBox(loc.toVector(), loc.toVector()));
    }


    @Override
    public boolean canPaste(BoundingBox bounds) {
        GameMap map = gameGroup.getCurrentMap();

        boolean containsIllegalBlocks = bounds.getBlockPoints()
                .map(point -> map.getBlock(point).getType())
                .anyMatch(material -> material == Material.BEDROCK
                                      || material == Material.BARRIER);

        if(containsIllegalBlocks) {
            return false;
        }

        return map.canPaste(bounds);
    }
}
