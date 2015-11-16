package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Building;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 16/11/15.
 * <p>
 * Creates sync repeating cannon tower tasks
 */
public class CannonTowerHandler {

    private static BlockFace[] SHOOTING_DIRECTIONS =
            new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public static int startCannonTowerTask(WarsPlugin plugin, Building building) {

        List<Turret> turrets = new ArrayList<>();

        for (Location loc : building.getBuildingBlocks()) {
            Block block = loc.getBlock();
            if (block.getType() != Material.COAL_ORE && block.getType() != Material.SPONGE) continue;

            for(BlockFace face : SHOOTING_DIRECTIONS){
                if(!block.getRelative(face).isEmpty()) continue;

                turrets.add(new Turret(loc, face, block.getType() == Material.COAL_ORE));
            }
        }

        return plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            boolean player = false;
            for(Entity e : building.getCenterBlock().getWorld().getNearbyEntities(building.getCenterBlock(), 25, 25,
                    25)) {
                if(!(e instanceof Player)) continue;
                if(plugin.getUser((Player) e).getTeamColor() == building.getTeamColor()) continue;

                player = true;
                break;
             }

            if(!player) return;

            for(Turret turret : turrets){
                Location from = turret.loc.clone().add(turret.dir.getModX() + 0.5, turret.dir.getModY() + 0.5,
                        turret.dir.getModZ() + 0.5);
                Entity entity;
                Vector velocity;
                if(turret.isFire){
                    velocity = new Vector(turret.dir.getModX(), turret.dir.getModY(), turret.dir.getModZ());
                    entity = from.getWorld().spawnEntity(from, EntityType.SMALL_FIREBALL);
                } else {
                    velocity = new Vector(turret.dir.getModX(), turret.dir.getModY() + 0.1, turret.dir.getModZ());
                    entity = from.getWorld().spawnEntity(from, EntityType.ARROW);
                }

                entity.setVelocity(velocity);
                entity.setMetadata("team", new FixedMetadataValue(plugin, building.getTeamColor()));
            }
        }, 20, 80);
    }

    private static class Turret {
        Location loc;
        BlockFace dir;
        boolean isFire;

        public Turret(Location loc, BlockFace dir, boolean isFire) {
            this.loc = loc;
            this.dir = dir;
            this.isFire = isFire;
        }
    }
}
