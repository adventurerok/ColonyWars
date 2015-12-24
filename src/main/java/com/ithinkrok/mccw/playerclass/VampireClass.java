package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserBeginGameEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.disguisecraft.Disguises;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import com.ithinkrok.mccw.util.item.TeamCompass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 05/12/15.
 * <p>
 * Handles the vampire class
 */
public class VampireClass extends ClassItemClassHandler {

    public VampireClass(WarsPlugin plugin, PlayerClass playerClass) {

        ClassItem vampireSword =
                new ClassItem(plugin, playerClass.getName(), Material.GOLD_SWORD, "items.vampire-sword.name");

        vampireSword.withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true);

        vampireSword.withEnchantmentEffects(
                new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "vampire", new LinearCalculator(0, 1)));

        vampireSword.withAttackAction(event -> {
            if (event.getFinalDamage() < 1 || event.getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                return;
            PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, true);
            event.getUser().addPotionEffect(effect);
        });

        vampireSword.withDescriptionLocale("items.vampire-sword.desc");

        vampireSword.withUpgradables(new ClassItem.Upgradable("vampire", "upgrades.vampire.name", 2));

        addExtraClassItems(vampireSword);

        addExtraClassItems(TeamCompass.createTeamCompass(plugin));

        for (int level = 1; level <= 2; ++level) {
            addExtraBuyables(new UpgradeBuyable(InventoryUtils
                    .createItemWithNameAndLore(Material.MONSTER_EGG, 1, EntityType.BAT.getTypeId(),
                            plugin.getLocale("upgrades.bat-flight.name", level)), Buildings.MAGETOWER,
                    plugin.getWarsConfig().getClassItemCost(playerClass, "bat" + level), "bat", level));
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if (event.getDamageCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getUser().isFlying()) {
            event.setCancelled(true);
            return;
        }
        super.onUserAttack(event);
    }

    @Override
    public boolean onInteract(UserInteractEvent event) {
        return event.getUser().isFlying() || super.onInteract(event);

    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        UserFlyingChanger changer = new UserFlyingChanger(event.getUser());

        changer.startTask(event.getUser().getPlugin());
    }

    private static class UserFlyingChanger implements Runnable {

        private int taskId;
        private User user;
        private boolean bat = false;
        private boolean allowFlight = false;
        private boolean mageTower = false;
        private double oldHealth = 0;
        private int blocksAboveGround = 0;
        private int maxBlocksAboveGround = 0;

        private UserFlyingChanger(User user) {
            this.user = user;

            user.setExp(0f);
        }

        @Override
        public void run() {
            if (!user.isInGame() || user.getTeam() == null) {
                Bukkit.getScheduler().cancelTask(taskId);
                return;
            }

            if (!mageTower) {
                if (user.getTeam().getBuildingCount(Buildings.MAGETOWER) < 1) return;
                mageTower = true;
                user.setExp(1f);
                user.sendLocale("unlock.bat-flight.message");

                oldHealth = user.getHealth();
            }

            blocksAboveGround = calculateBlocksAboveGround();

            double newHealth = user.getHealth();
            float change = 0f;

            if (newHealth < oldHealth) change = -0.07f;
            else if (user.hasPotionEffect(PotionEffectType.REGENERATION)) change = 0.001f;

            if (change != 0) {
                float exp = user.getExp();
                exp = Math.max(Math.min(exp + change, 1f), 0f);
                user.setExp(exp);
            }

            oldHealth = newHealth;

            Block block = user.getLocation().getBlock();
            boolean inWater = block.getRelative(0, 1, 0).isLiquid() ||
                    (block.isLiquid() && block.getRelative(0, -1, 0).isLiquid());

            if (user.isFlying() && !inWater) {
                float exp = user.getExp();
                exp = Math.max(exp - flightDecreaseAmount(user.getUpgradeLevel("bat")), 0);
                user.setExp(exp);

                if (!bat) {
                    Disguises.disguise(user, EntityType.BAT);
                    bat = true;
                }

                if (exp > 0) return;
                user.setAllowFlight(allowFlight = false);
                user.sendLocale("timeouts.batting.finished");
            } else {
                if (inWater) user.setFlying(false);

                if(!user.isOnGround()) {
                    if(blocksAboveGround > maxBlocksAboveGround) maxBlocksAboveGround = blocksAboveGround;
                    else if(maxBlocksAboveGround > 3 && blocksAboveGround < 2) user.setAllowFlight(allowFlight = false);
                }

                float exp = user.getExp();
                exp = Math.min(exp + 0.001f, 1);
                user.setExp(exp);


                if (exp > 0.2f && !allowFlight && user.isOnGround()) {
                    user.setAllowFlight(allowFlight = true);
                    if(maxBlocksAboveGround == 0) user.sendLocale("cooldowns.bat.finished");
                    user.setFlySpeed(0.05f);
                }

                if(user.isOnGround()) maxBlocksAboveGround = 0;

                if (bat) {
                    Disguises.unDisguise(user);
                    bat = false;
                }
            }
        }

        public int calculateBlocksAboveGround() {
            int yMod = 0;
            Block block = user.getLocation().getBlock();
            while (block.getLocation().getBlockY() - yMod > 1) {
                ++yMod;

                if (block.getRelative(0, -yMod, 0).getType().isSolid()) break;
            }

            return yMod;
        }

        private float flightDecreaseAmount(int level) {
            float base;

            switch (level) {
                case 1:
                    base = 0.006f;
                    break;
                case 2:
                    base = 0.004f;
                    break;
                default:
                    base = 0.01f;
                    break;
            }

            if(blocksAboveGround >= 10) return base * (blocksAboveGround / 10f);
            else return base;
        }

        public void startTask(WarsPlugin plugin) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
