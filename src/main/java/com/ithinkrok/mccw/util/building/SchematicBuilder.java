package com.ithinkrok.mccw.util.building;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Schematic;
import com.ithinkrok.mccw.data.SchematicRotation;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.BoundingBox;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 02/11/15.
 * <p>
 * Builds schematics in the game world
 */
public class SchematicBuilder {

    private static final DecimalFormat percentFormat = new DecimalFormat("00%");

    public static boolean pasteSchematic(WarsPlugin plugin, Schematic schemData, Location loc, int rotation,
                                         TeamColor teamColor) {
        return doSchematic(plugin, schemData, loc, teamColor, rotation, true);
    }

    private static boolean doSchematic(WarsPlugin plugin, Schematic schemData, Location loc, TeamColor teamColor,
                                       int rotation, boolean instant) {

        SchematicRotation schem = schemData.getSchematicRotation(rotation);

        BoundingBox bounds = schem.calcBounds(loc);

        if (!plugin.getGameInstance().canBuild(bounds)) return false;

        List<Location> locations = new ArrayList<>();

        Location centerBlock = null;

        HashMap<Location, BlockState> oldBlocks = new HashMap<>();

        BlockState oldState;

        for (int x = 0; x < schem.getWidth(); ++x) {
            for (int y = 0; y < schem.getHeight(); ++y) {
                for (int z = 0; z < schem.getLength(); ++z) {
                    Location l = new Location(loc.getWorld(), x + loc.getX() + schem.getOffsetX(),
                            y + loc.getY() + schem.getOffsetY(), z + loc.getZ() + schem.getOffsetZ());

                    oldState = l.getBlock().getState();
                    if (oldState.getType() == Material.LAPIS_ORE) oldState.setType(Material.AIR);
                    else if (oldState.getType() == Material.BEDROCK || oldState.getType() == Material.BARRIER) {
                        //prevent building over map boundaries
                        return false;
                    }

                    int bId = schem.getBlock(x, y, z);
                    if (bId == 0) continue;
                    if(bId == Material.BARRIER.getId()) bId = 0;

                    if (bId == Material.OBSIDIAN.getId()) centerBlock = l;

                    locations.add(l);

                    oldBlocks.put(l, oldState);
                }
            }
        }

        Collections.sort(locations, (o1, o2) -> {
            if (o1.getY() != o2.getY()) return Double.compare(o1.getY(), o2.getY());
            if (o1.getX() != o2.getX()) return Double.compare(o1.getX(), o2.getX());

            return Double.compare(o1.getZ(), o2.getZ());
        });

        Building result =
                new Building(plugin, schemData.getTransformName(), teamColor, centerBlock, bounds, rotation, locations,
                        oldBlocks);

        plugin.getGameInstance().addBuilding(result);
        SchematicBuilderTask task = new SchematicBuilderTask(plugin, loc, result, schem, instant ? -1 : 2);

        if (!instant) {
            result.setBuildTask(task.schedule(plugin));
        } else {
            task.run();
        }

        return true;


    }

    public static boolean buildSchematic(WarsPlugin plugin, Schematic schemData, Location loc, int rotation,
                                         TeamColor teamColor) {
        return doSchematic(plugin, schemData, loc, teamColor, rotation, false);
    }

    private static byte rotateData(Material type, int rotation, byte data) {
        switch (type) {
            case ACACIA_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case BRICK_STAIRS:
            case COBBLESTONE_STAIRS:
            case DARK_OAK_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case NETHER_BRICK_STAIRS:
            case QUARTZ_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case SANDSTONE_STAIRS:
            case SMOOTH_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case WOOD_STAIRS:
                return (byte) ((data & 0x4) | Facing.rotateStairs(data & 3, rotation));
            case LADDER:
            case CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
                return (byte) Facing.rotateLadderFurnaceChest(data, rotation);
            case LOG:
            case LOG_2:
                return (byte) ((data & 3) | Facing.rotateLogs(data & 12, rotation));
            default:
                return data;
        }

    }

    private static class SchematicBuilderTask implements Runnable {

        int index = 0;

        Location origin;
        int taskId;
        Hologram hologram;
        private Building building;
        private SchematicRotation schem;
        private WarsPlugin plugin;
        private int buildSpeed;

        private boolean clearedOrigin = false;

        public SchematicBuilderTask(WarsPlugin plugin, Location origin, Building building, SchematicRotation schem,
                                    int buildSpeed) {
            this.plugin = plugin;
            this.origin = origin;
            this.building = building;
            this.schem = schem;
            this.buildSpeed = buildSpeed;

            Location holoLoc;
            if (building.getCenterBlock() != null) holoLoc = building.getCenterBlock().clone().add(0.5d, 1.5d, 0.5d);
            else holoLoc = origin.clone().add(0.5d, 1.5d, 0.5d);

            hologram = HologramAPI.createHologram(holoLoc, "Building: 0%");

            hologram.spawn();

            building.addHologram(hologram);
        }

        @Override
        public void run() {
            int count = 0;

            if (!clearedOrigin) {
                origin.getBlock().setType(Material.AIR);
                clearedOrigin = true;
            }

            List<Location> locations = building.getBuildingBlocks();

            while (index < locations.size()) {
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - schem.getOffsetX();
                int y = loc.getBlockY() - origin.getBlockY() - schem.getOffsetY();
                int z = loc.getBlockZ() - origin.getBlockZ() - schem.getOffsetZ();


                int bId = schem.getBlock(x, y, z);
                byte bData = schem.getData(x, y, z);

                Block block = loc.getBlock();

                if (bId == Material.WOOL.getId()) bData = building.getTeamColor().getDyeColor().getWoolData();
                else if (bId == Material.STAINED_CLAY.getId())
                    bData = building.getTeamColor().getDyeColor().getWoolData();
                else if(bId == Material.BARRIER.getId()) bId = 0;
                else if(bId == Material.DIAMOND_ORE.getId()) bId = Material.GOLD_ORE.getId();

                block.setTypeIdAndData(bId, rotateData(Material.getMaterial(bId), schem.getRotation(), bData), false);

                ++index;

                ++count;
                if (buildSpeed != -1 && count > buildSpeed) {
                    loc.getWorld().playEffect(loc, Effect.STEP_SOUND, bId);
                    hologram.setText("Building: " + percentFormat.format((double) index / (double) locations.size()));
                    return;
                }
            }

            building.setBuildTask(0);
            plugin.getGameInstance().cancelTask(taskId);

            hologram.despawn();
            building.removeHologram(hologram);

            if (building.getCenterBlock() != null) {
                building.getCenterBlock().getWorld().playSound(building.getCenterBlock(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }

            building.setFinished(true);
            plugin.getGameInstance().finishBuilding(building);

            building = null;
        }

        public int schedule(WarsPlugin plugin) {
            return taskId = plugin.getGameInstance().scheduleRepeatingTask(this, 1, 1);
        }
    }
}
