package com.ithinkrok.oldmccw.util.building;

import com.ithinkrok.minigames.schematic.SchematicPaster;
import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.data.Building;
import com.ithinkrok.oldmccw.data.Schematic;
import com.ithinkrok.oldmccw.data.SchematicRotation;
import com.ithinkrok.oldmccw.data.TeamColor;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

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
        return SchematicPaster.rotateData(type, rotation, data);

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

            HologramAPI.removeHologram(hologram);
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
