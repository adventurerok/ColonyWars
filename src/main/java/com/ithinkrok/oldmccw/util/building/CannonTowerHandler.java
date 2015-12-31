package com.ithinkrok.oldmccw.util.building;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.data.Building;
import com.ithinkrok.oldmccw.data.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
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

        return plugin.getGameInstance().scheduleRepeatingTask( () -> {
            boolean player = false;
            for(Entity e : building.getCenterBlock().getWorld().getNearbyEntities(building.getCenterBlock(), 35, 35,
                    35)) {
                if(!(e instanceof Player)) continue;

                User user = plugin.getUser((Player) e);
                if(!user.isInGame() || user.getTeamColor() == building.getTeamColor()) continue;

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
                    velocity = new Vector(turret.dir.getModX(), turret.dir.getModY() - 0.05, turret.dir.getModZ());
                    entity = from.getWorld().spawnEntity(from, EntityType.SMALL_FIREBALL);
                    ((SmallFireball)entity).setDirection(velocity);
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