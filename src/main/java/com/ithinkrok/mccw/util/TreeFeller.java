package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.handler.GameInstance;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by paul on 01/11/15.
 *
 * Destroys all logs in a tree and drops ingots for them
 */
public class TreeFeller {

    public static int fellTree(GameInstance game, Location location){
        LinkedList<Location> locations = new LinkedList<>();

        addSurrounding(locations, location);

        ArrayList<Location> ingotsToSpawn = new ArrayList<>();
        
        while(locations.size() > 0){
            Location pos = locations.removeFirst();

            Block block = pos.getBlock();
            
            if(block.getType() != Material.LOG && block.getType() != Material.LOG_2) continue;
            if(game.isInBuilding(pos)) continue;
            
            block.setType(Material.AIR);
            ingotsToSpawn.add(pos);

            addSurrounding(locations, pos);
        }

        double part1 = ingotsToSpawn.size() + (Math.log(ingotsToSpawn.size()) / Math.log(1.5d));

        double part2 = Math.sqrt(Math.pow(ingotsToSpawn.size(), 3) / 500d);

        double total = part1 - Math.min(part2, Math.pow(ingotsToSpawn.size(), 0.9d));

        return (int) total;
    }

    private static void addSurrounding(List<Location> locations, Location l){
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() - 1d));
    }
}
