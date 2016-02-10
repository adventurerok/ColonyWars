package com.ithinkrok.cw.util;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.util.EntityUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by paul on 17/01/16.
 */
public class CannonTowerHandler {

    private static final BlockFace[] SHOOTING_DIRECTIONS =
            new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public static GameTask startCannonTowerTask(GameGroup gameGroup, Building building) {
        Config config = gameGroup.getSharedObjectOrEmpty("cannon_tower");

        Material arrowTurret = Material.matchMaterial(config.getString("arrow_turret_material", "SPONGE"));
        Material fireTurret = Material.matchMaterial(config.getString("fire_turret_material", "COAL_ORE"));

        List<Turret> turrets = new ArrayList<>();

        for (Location loc : building.getSchematic().getBuildingBlocks()) {
            Block block = loc.getBlock();
            if (block.getType() != fireTurret && block.getType() != arrowTurret) continue;

            for (BlockFace face : SHOOTING_DIRECTIONS) {
                if (!block.getRelative(face).isEmpty()) continue;

                turrets.add(new Turret(loc, face, block.getType() == fireTurret));
            }
        }

        int reloadTime = config.getInt("reload_time", 4) * 20;
        int range = config.getInt("range", 35);

        GameTask result = gameGroup.repeatInFuture(task -> {
            if (building.isRemoved()) task.finish();

            boolean fire = false;

            for (Entity e : building.getCenterBlock().getWorld()
                    .getNearbyEntities(building.getCenterBlock(), range, range, range)) {
                User rep = EntityUtils.getRepresentingUser(gameGroup, e);
                if (rep == null || !rep.isInGame() ||
                        Objects.equals(rep.getTeamIdentifier(), building.getTeamIdentifier())) continue;

                fire = true;
                break;
            }

            if (!fire) return;

            for (Turret turret : turrets) {
                turret.fire(gameGroup, building);
            }
        }, reloadTime, reloadTime);

        gameGroup.bindTaskToCurrentMap(result);

        return result;
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

        public void fire(GameGroup gameGroup, Building building) {
            Location from = loc.clone().add(dir.getModX() + 0.5, dir.getModY() + 0.5, dir.getModZ() + 0.5);

            Entity entity;
            Vector velocity;

            if (isFire) {
                velocity = new Vector(dir.getModX(), dir.getModY() - 0.05, dir.getModZ());
                entity = from.getWorld().spawnEntity(from, EntityType.SMALL_FIREBALL);
                ((SmallFireball) entity).setDirection(velocity);
            } else {
                velocity = new Vector(dir.getModX(), dir.getModY() + 0.1, dir.getModZ());
                entity = from.getWorld().spawnEntity(from, EntityType.ARROW);
            }
            entity.setVelocity(velocity);
            gameGroup.getTeam(building.getTeamIdentifier()).makeEntityRepresentTeam(entity);
        }
    }
}
