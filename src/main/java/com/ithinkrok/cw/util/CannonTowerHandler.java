package com.ithinkrok.cw.util;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
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

        Config coalConfig = config.getConfigOrNull("coal_turret");
        Config spongeConfig = config.getConfigOrNull("sponge_turret");

        List<Turret> turrets = new ArrayList<>();

        for (Location loc : building.getSchematic().getBuildingBlocks()) {
            Block block = loc.getBlock();
            if (block.getType() != coalTurret && block.getType() != spongeTurret) continue;

            for (BlockFace face : SHOOTING_DIRECTIONS) {
                if (!block.getRelative(face).isEmpty()) continue;

                if (block.getType() == coalTurret) {
                    if (coalConfig != null) {
                        turrets.add(new Turret(loc, face, coalConfig));
                    } else {
                        turrets.add(new Turret(loc, face, TurretType.FIRE));
                    }
                } else {
                    if (spongeConfig != null) {
                        turrets.add(new Turret(loc, face, spongeConfig));
                    } else {
                        turrets.add(new Turret(loc, face, TurretType.ARROW));
                    }
                }
            }
        }

        int reloadTime = config.getInt("reload_time", 4) * 20;
        int range = config.getInt("range", 35);

        boolean friendly = config.getBoolean("friendly", false);

        GameTask result = gameGroup.repeatInFuture(task -> {
            if (building.isRemoved()) task.finish();

            boolean fire = false;

            for (Entity e : building.getCenterBlock().getWorld()
                    .getNearbyEntities(building.getCenterBlock(), range, range, range)) {
                User rep = EntityUtils.getRepresentingUser(gameGroup, e);
                if (rep == null || !rep.isInGame()) continue;
                if (Objects.equals(rep.getTeamIdentifier(), building.getTeamIdentifier()) != friendly) continue;

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
        ARROW, FIRE, POTION
    }

    private static class Turret {
        Location loc;
        Material material;
        BlockFace dir;
        TurretType turretType;
        Config config;
        double horizSpeed = 1;
        double vertVelo = 0;

        public Turret(Location loc, BlockFace dir, TurretType type) {
            this.loc = loc;
            this.dir = dir;
            this.turretType = type;
            this.config = new MemoryConfig();

            this.material = loc.getBlock().getType();
        }

        public Turret(Location loc, BlockFace dir, Config config) {
            this.loc = loc;
            this.dir = dir;
            this.turretType = TurretType.valueOf(config.getString("type").toUpperCase());
            this.config = config;

            horizSpeed = config.getDouble("speed_horizontal", 1);
            vertVelo = config.getDouble("velocity_vertical", 0);

            this.material = loc.getBlock().getType();
        }

        public void fire(GameGroup gameGroup, Building building) {
            if(loc.getBlock().getType() != material) {
                return;
            }

            Location from = loc.clone().add(dir.getModX() + 0.5, dir.getModY() + 0.5, dir.getModZ() + 0.5);

            Entity entity;
            Vector velocity;

            switch (turretType) {
                case FIRE:
                    velocity = new Vector(dir.getModX() * horizSpeed, dir.getModY() - 0.05 + vertVelo,
                                          dir.getModZ() * horizSpeed);
                    entity = from.getWorld().spawnEntity(from, EntityType.SMALL_FIREBALL);
                    ((Fireball) entity).setDirection(velocity);
                    break;
                case ARROW:
                    velocity = new Vector(dir.getModX() * horizSpeed, dir.getModY() + 0.1 + vertVelo,
                                          dir.getModZ() * horizSpeed);
                    entity = from.getWorld().spawnEntity(from, EntityType.ARROW);
                    break;
                case POTION:
                    velocity = new Vector(dir.getModX() * horizSpeed, dir.getModY() + vertVelo,
                                          dir.getModZ() * horizSpeed);
                    entity = from.getWorld().spawnEntity(from, EntityType.SPLASH_POTION);

                    LingeringPotion potion = (LingeringPotion) entity;

                    ItemStack potionItem = new ItemStack(Material.SPLASH_POTION, 1);

                    PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();

                    boolean doneMain = false;

                    if (config.contains("effects")) {

                        for (Config effectConfig : config.getConfigList("effects")) {
                            PotionEffectType type = PotionEffectType.getByName(effectConfig.getString("name"));
                            int duration = (int) (effectConfig.getDouble("duration_seconds") * 20);
                            int amp = effectConfig.getInt("level", 1) - 1;

                            if (!doneMain) {
                                potionMeta.setBasePotionData(new PotionData(PotionType.getByEffect(type)));
                                doneMain = true;
                            }

                            PotionEffect effect = new PotionEffect(type, duration, amp);
                            potionMeta.addCustomEffect(effect, true);
                        }

                    }

                    potionItem.setItemMeta(potionMeta);
                    potion.setItem(potionItem);

                    break;
                default:
                    return;
            }
            entity.setVelocity(velocity);
            gameGroup.getTeam(building.getTeamIdentifier()).makeEntityRepresentTeam(entity);
        }
    }
}
