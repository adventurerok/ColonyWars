package com.ithinkrok.mccw.util;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.enumeration.TeamColor;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 02/11/15.
 * <p>
 * Builds schematics in the game world
 */
public class SchematicBuilder {

    private static final DecimalFormat percentFormat = new DecimalFormat("00%");

    public static boolean pasteSchematic(WarsPlugin plugin, SchematicData schemData, Location loc,
                                                TeamColor teamColor) {
        return doSchematic(plugin, schemData, loc, teamColor, true);
    }

    private static boolean doSchematic(WarsPlugin plugin, SchematicData schemData, Location loc, TeamColor teamColor,
                                            boolean instant) {

        File schemFile = new File(plugin.getDataFolder(), schemData.getSchematicFile());

        try (NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))) {
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag) nbt.get("Width")).getValue();
            short height = ((ShortTag) nbt.get("Height")).getValue();
            short length = ((ShortTag) nbt.get("Length")).getValue();

            int offsetX = ((IntTag) nbt.get("WEOffsetX")).getValue();
            int offsetY = ((IntTag) nbt.get("WEOffsetY")).getValue();
            int offsetZ = ((IntTag) nbt.get("WEOffsetZ")).getValue();

            Vector minBB = new Vector(loc.getX() + offsetX, loc.getY() + offsetY, loc.getZ() + offsetZ);
            Vector maxBB = new Vector(minBB.getX() + width, minBB.getY() + height, minBB.getZ() + length);

            if(!plugin.canBuild(minBB, maxBB)) return false;

            byte[] blocks = ((ByteArrayTag) nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag) nbt.get("Data")).getValue();

            List<Location> locations = new ArrayList<>();

            Location centerBlock = null;

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    for (int z = 0; z < length; ++z) {
                        int index = width * (y * length + z) + x;

                        Location l = new Location(loc.getWorld(), x + loc.getX() + offsetX, y + loc.getY() + offsetY,
                                z + loc.getZ() + offsetZ);

                        int bId = blocks[index] & 0xFF;
                        if (bId == 0) continue;

                        if (bId == Material.OBSIDIAN.getId()) centerBlock = l;

                        locations.add(l);
                    }
                }
            }

            Collections.sort(locations, (o1, o2) -> {
                if (o1.getY() != o2.getY()) return Double.compare(o1.getY(), o2.getY());
                if (o1.getX() != o2.getX()) return Double.compare(o1.getX(), o2.getX());

                return Double.compare(o1.getZ(), o2.getZ());
            });

            BuildingInfo result = new BuildingInfo(plugin, schemData.getBuildingName(), teamColor, centerBlock,
                    locations);

            SchematicBuilderTask task =
                    new SchematicBuilderTask(loc, result, width, height, length, offsetX, offsetY,
                            offsetZ, blocks, data, instant ? -1 : 2);

            if (!instant) {
                task.schedule(plugin);
            } else {
                task.run();
            }

            plugin.addBuilding(result);

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

    }

    public static boolean buildSchematic(WarsPlugin plugin, SchematicData schemData, Location loc,
                                                TeamColor teamColor) {
        return doSchematic(plugin, schemData, loc, teamColor, false);
    }

    private static class SchematicBuilderTask implements Runnable {

        int index = 0;
        private BuildingInfo buildingInfo;
        short width;
        short height;
        short length;
        int offsetX, offsetY, offsetZ;
        byte[] blocks, data;
        Location origin;

        int taskId;
        Hologram hologram;
        private int buildSpeed;

        private boolean clearedOrigin = false;

        public SchematicBuilderTask(Location origin, BuildingInfo buildingInfo, short width, short height,
                                    short length, int offsetX, int offsetY, int offsetZ, byte[] blocks, byte[] data,
                                    int buildSpeed) {
            this.origin = origin;
            this.buildingInfo = buildingInfo;
            this.width = width;
            this.height = height;
            this.length = length;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.blocks = blocks;
            this.data = data;
            this.buildSpeed = buildSpeed;

            Location holoLoc;
            if (buildingInfo.getCenterBlock() != null) holoLoc = buildingInfo.getCenterBlock().clone().add(0.5d, 1.5d, 0.5d);
            else holoLoc = origin.clone().add(0.5d, 1.5d, 0.5d);

            hologram = HologramAPI.createHologram(holoLoc, "Building: 0%");

            hologram.spawn();
        }

        @Override
        public void run() {
            int count = 0;

            if(!clearedOrigin){
                origin.getBlock().setType(Material.AIR);
                clearedOrigin = true;
            }

            List<Location> locations = buildingInfo.getBuildingBlocks();

            while (index < locations.size()) {
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - offsetX;
                int y = loc.getBlockY() - origin.getBlockY() - offsetY;
                int z = loc.getBlockZ() - origin.getBlockZ() - offsetZ;

                int blockInd = width * (y * length + z) + x;

                int bId = blocks[blockInd] & 0xFF;
                byte bData = data[blockInd];

                Block block = loc.getBlock();

                if (bId == Material.WOOL.getId()) bData = buildingInfo.getTeamColor().dyeColor.getWoolData();

                block.setTypeId(bId);
                block.setData(bData);

                loc.getWorld().playEffect(loc, Effect.STEP_SOUND, bId);

                ++index;

                ++count;
                if (buildSpeed != -1 && count > buildSpeed) {
                    hologram.setText("Building: " + percentFormat.format((double) index / (double) locations.size()));
                    return;
                }
            }

            Bukkit.getScheduler().cancelTask(taskId);

            hologram.despawn();

            if(buildingInfo.getCenterBlock() != null){
                buildingInfo.getCenterBlock().getWorld().playSound(buildingInfo.getCenterBlock(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }

            buildingInfo.setFinished(true);
        }

        public void schedule(Plugin plugin) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
