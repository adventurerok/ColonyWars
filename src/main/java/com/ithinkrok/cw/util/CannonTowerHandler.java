package com.ithinkrok.cw.util;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
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
        Material spongeTurret = Material.SPONGE;
        Material coalTurret = Material.COAL_ORE;

        Config config = building.getConfig();

        TurretType spongeTurretType = TurretType.valueOf(config.getString("sponge_turret_type", "ARROW").toUpperCase());
        TurretType coalTurretType = TurretType.valueOf(config.getString("coal_turret_type", "FIRE").toUpperCase());

        List<Turret> turrets = new ArrayList<>();

        for (Location loc : building.getSchematic().getBuildingBlocks()) {
            Block block = loc.getBlock();
            if (block.getType() != coalTurret && block.getType() != spongeTurret) continue;

            for (BlockFace face : SHOOTING_DIRECTIONS) {
                if (!block.getRelative(face).isEmpty()) continue;

                turrets.add(new Turret(loc, face, block.getType() == coalTurret ? coalTurretType : spongeTurretType));
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

    private enum TurretType {
        ARROW, FIRE
    }

    private static class Turret {
        Location loc;
        BlockFace dir;
        TurretType turretType;

        public Turret(Location loc, BlockFace dir, TurretType turretType) {
            this.loc = loc;
            this.dir = dir;
            this.turretType = turretType;
        }

        public void fire(GameGroup gameGroup, Building building) {
            Location from = loc.clone().add(dir.getModX() + 0.5, dir.getModY() + 0.5, dir.getModZ() + 0.5);

            Entity entity;
            Vector velocity;

            switch (turretType) {
                case FIRE:
                    velocity = new Vector(dir.getModX(), dir.getModY() - 0.05, dir.getModZ());
                    entity = from.getWorld().spawnEntity(from, EntityType.SMALL_FIREBALL);
                    ((Fireball) entity).setDirection(velocity);
                    break;
                case ARROW:
                    velocity = new Vector(dir.getModX(), dir.getModY() + 0.1, dir.getModZ());
                    entity = from.getWorld().spawnEntity(from, EntityType.ARROW);
                    break;
                default:
                    return;
            }
            entity.setVelocity(velocity);
            gameGroup.getTeam(building.getTeamIdentifier()).makeEntityRepresentTeam(entity);
        }
    }
}
