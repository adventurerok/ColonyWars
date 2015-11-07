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

            byte[] blocks = ((ByteArrayTag) nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag) nbt.get("Data")).getValue();

            SchematicRotation schem =
                    new SchematicRotation(width, height, length, offsetX, offsetY, offsetZ, blocks, data, 0);

            Vector[] bounds = schem.calcBounds(loc);

            if (!plugin.canBuild(bounds[0], bounds[1])) return false;

            List<Location> locations = new ArrayList<>();

            Location centerBlock = null;

            for (int x = 0; x < schem.getWidth(); ++x) {
                for (int y = 0; y < schem.getHeight(); ++y) {
                    for (int z = 0; z < schem.getLength(); ++z) {
                        Location l = new Location(loc.getWorld(), x + loc.getX() + schem.getOffsetX(),
                                y + loc.getY() + schem.getOffsetY(), z + loc.getZ() + schem.getOffsetZ());

                        int bId = schem.getBlock(x, y, z);
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

            BuildingInfo result =
                    new BuildingInfo(plugin, schemData.getBuildingName(), teamColor, centerBlock, locations);

            plugin.addBuilding(result);

            SchematicBuilderTask task = new SchematicBuilderTask(plugin, loc, result, schem, instant ? -1 : 2);

            if (!instant) {
                task.schedule(plugin);
            } else {
                task.run();
            }

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

    private static class SchematicRotation {
        byte[] blocks, data;
        boolean xzSwap = false;
        boolean xFlip = false;
        boolean zFlip = false;
        private short width, height, length;
        private int offsetX, offsetY, offsetZ;

        public SchematicRotation(short width, short height, short length, int offsetX, int offsetY, int offsetZ,
                                 byte[] blocks, byte[] data, int rotation) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.blocks = blocks;
            this.data = data;

            if (rotation == 1 || rotation == 3) xzSwap = true;
            if (rotation == 2 || rotation == 3) xFlip = true;
            if (rotation == 1 || rotation == 2) zFlip = true;
        }

        public int getBlock(int x, int y, int z) {
            return blocks[calcIndex(x, y, z)] & 0xFF;
        }

        private int calcIndex(int x, int y, int z) {
            if (xzSwap) {
                int i = x;
                x = z;
                z = i;
            }

            if (xFlip) x = width - x - 1;
            if (zFlip) z = length - z - 1;

            return width * (y * length + z) + x;
        }

        public byte getData(int x, int y, int z) {
            return data[calcIndex(x, y, z)];
        }

        public Vector[] calcBounds(Location loc) {
            Vector minBB = new Vector(loc.getX() + getOffsetX(), loc.getY() + getOffsetY(), loc.getZ() + getOffsetZ());
            Vector maxBB =
                    new Vector(minBB.getX() + getWidth(), minBB.getY() + getHeight(), minBB.getZ() + getLength());

            return new Vector[]{minBB, maxBB};
        }

        public int getOffsetX() {
            int base = xzSwap ? offsetZ : offsetX;

            if (xFlip) base = 1 - base - getWidth();
            return base;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public int getOffsetZ() {
            int base = xzSwap ? offsetX : offsetZ;

            if (zFlip) base = 1 - base - getLength();
            return base;
        }

        public short getWidth() {
            return xzSwap ? length : width;
        }

        public short getHeight() {
            return height;
        }

        public short getLength() {
            return xzSwap ? width : length;
        }
    }

    private static class SchematicBuilderTask implements Runnable {

        int index = 0;

        Location origin;
        int taskId;
        Hologram hologram;
        private BuildingInfo buildingInfo;
        private SchematicRotation schem;
        private WarsPlugin plugin;
        private int buildSpeed;

        private boolean clearedOrigin = false;

        public SchematicBuilderTask(WarsPlugin plugin, Location origin, BuildingInfo buildingInfo,
                                    SchematicRotation schem, int buildSpeed) {
            this.plugin = plugin;
            this.origin = origin;
            this.buildingInfo = buildingInfo;
            this.schem = schem;
            this.buildSpeed = buildSpeed;

            Location holoLoc;
            if (buildingInfo.getCenterBlock() != null)
                holoLoc = buildingInfo.getCenterBlock().clone().add(0.5d, 1.5d, 0.5d);
            else holoLoc = origin.clone().add(0.5d, 1.5d, 0.5d);

            hologram = HologramAPI.createHologram(holoLoc, "Building: 0%");

            hologram.spawn();
        }

        @Override
        public void run() {
            int count = 0;

            if (!clearedOrigin) {
                origin.getBlock().setType(Material.AIR);
                clearedOrigin = true;
            }

            List<Location> locations = buildingInfo.getBuildingBlocks();

            while (index < locations.size()) {
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - schem.getOffsetX();
                int y = loc.getBlockY() - origin.getBlockY() - schem.getOffsetY();
                int z = loc.getBlockZ() - origin.getBlockZ() - schem.getOffsetZ();


                int bId = schem.getBlock(x, y, z);
                byte bData = schem.getData(x, y, z);

                Block block = loc.getBlock();

                if (bId == Material.WOOL.getId()) bData = buildingInfo.getTeamColor().dyeColor.getWoolData();
                else if (bId == Material.STAINED_CLAY.getId())
                    bData = buildingInfo.getTeamColor().dyeColor.getWoolData();

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

            if (buildingInfo.getCenterBlock() != null) {
                buildingInfo.getCenterBlock().getWorld()
                        .playSound(buildingInfo.getCenterBlock(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }

            buildingInfo.setFinished(true);
            plugin.finishBuilding(buildingInfo);
        }

        public void schedule(Plugin plugin) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
