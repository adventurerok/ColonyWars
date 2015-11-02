package com.ithinkrok.mccw.util;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paul on 02/11/15.
 *
 * Builds schematics in the game world
 */
public class SchematicBuilder {

    public static List<Location> pasteSchematic(File schemFile, Location loc){
        try(NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))){
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag)nbt.get("Width")).getValue();
            short height = ((ShortTag)nbt.get("Height")).getValue();
            short length = ((ShortTag)nbt.get("Length")).getValue();

            int offsetX = ((IntTag)nbt.get("WEOffsetX")).getValue();
            int offsetY = ((IntTag)nbt.get("WEOffsetY")).getValue();
            int offsetZ = ((IntTag)nbt.get("WEOffsetZ")).getValue();

            byte[] blocks = ((ByteArrayTag)nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag)nbt.get("Data")).getValue();

            List<Location> locations = new ArrayList<>();

            for(int x = 0; x < width; ++x){
                for(int y = 0; y < height; ++y){
                    for(int z = 0; z < length; ++z){
                        int index = width * (y * length + z) + x;

                        Location l = new Location(loc.getWorld(), x + loc.getX() + offsetX, y + loc.getY() + offsetY,
                                z + loc.getZ() + offsetZ);

                        int bId = blocks[index] & 0xFF;
                        if(bId == 0) continue;
                        Block block = l.getBlock();

                        block.setTypeId(bId);
                        block.setData(data[index]);

                        locations.add(l);
                    }
                }
            }

            return locations;
        } catch (IOException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }

    }

    public static List<Location> buildSchematic(Plugin plugin, File schemFile, Location loc){
        try(NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))){
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag)nbt.get("Width")).getValue();
            short height = ((ShortTag)nbt.get("Height")).getValue();
            short length = ((ShortTag)nbt.get("Length")).getValue();

            int offsetX = ((IntTag)nbt.get("WEOffsetX")).getValue();
            int offsetY = ((IntTag)nbt.get("WEOffsetY")).getValue();
            int offsetZ = ((IntTag)nbt.get("WEOffsetZ")).getValue();

            byte[] blocks = ((ByteArrayTag)nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag)nbt.get("Data")).getValue();

            List<Location> locations = new ArrayList<>();

            for(int x = 0; x < width; ++x){
                for(int y = 0; y < height; ++y){
                    for(int z = 0; z < length; ++z){
                        int index = width * (y * length + z) + x;

                        Location l = new Location(loc.getWorld(), x + loc.getX() + offsetX, y + loc.getY() + offsetY,
                                z + loc.getZ() + offsetZ);

                        int bId = blocks[index] & 0xFF;
                        if(bId == 0) continue;

                        locations.add(l);
                    }
                }
            }

            Collections.sort(locations, (o1, o2) -> {
                if(o1.getY() != o2.getY()) return Double.compare(o1.getY(), o2.getY());
                if(o1.getX() != o2.getX()) return Double.compare(o1.getX(), o2.getX());

                return Double.compare(o1.getZ(), o2.getZ());
            });

            SchematicBuilderTask task = new SchematicBuilderTask(loc, width, height, length, offsetX, offsetY,
                    offsetZ, blocks, data, locations);

            task.schedule(plugin);

            return locations;
        } catch (IOException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }

    }

    private static class SchematicBuilderTask implements Runnable {

        int index = 0;
        short width, height, length;
        int offsetX, offsetY, offsetZ;
        byte[] blocks, data;
        Location origin;

        int taskId;

        List<Location> locations = new ArrayList<>();

        public SchematicBuilderTask(Location origin, short width, short height, short length, int offsetX, int offsetY,
                                    int offsetZ, byte[] blocks, byte[] data, List<Location> locations) {
            this.origin = origin;
            this.width = width;
            this.height = height;
            this.length = length;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.blocks = blocks;
            this.data = data;
            this.locations = locations;
        }

        @Override
        public void run() {
            int count = 0;

            while(index < locations.size()){
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - offsetX;
                int y = loc.getBlockY() - origin.getBlockY() - offsetY;
                int z = loc.getBlockZ() - origin.getBlockZ() - offsetZ;

                int blockInd = width * (y * length + z) + x;

                int bId = blocks[blockInd] & 0xFF;

                Block block = loc.getBlock();
                block.setTypeId(bId);
                block.setData(data[blockInd]);

                loc.getWorld().playEffect(loc, Effect.STEP_SOUND, bId);

                ++index;

                ++count;
                if(count > 2) return;
            }

            Bukkit.getScheduler().cancelTask(taskId);
        }

        public void schedule(Plugin plugin){
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
