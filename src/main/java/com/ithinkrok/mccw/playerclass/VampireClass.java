package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserBeginGameEvent;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Disguises;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

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
            if (event.getFinalDamage() < 1) return;
            PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, true);
            event.getUser().getPlayer().addPotionEffect(effect);
        });

        vampireSword.withDescriptionLocale("items.vampire-sword.desc");

        vampireSword.withUpgradables(new ClassItem.Upgradable("vampire", "upgrades.vampire.name", 2));

        addExtraClassItems(vampireSword);
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if (event.getUser().getPlayer().isFlying()) {
            event.setCancelled(true);
            return;
        }
        super.onUserAttack(event);
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        UserFlyingChanger changer = new UserFlyingChanger(event.getUser());

        changer.startTask(event.getUser().getPlugin());
    }

    private static class UserFlyingChanger implements Runnable{

        private int taskId;
        private User user;
        private boolean bat = false;
        private boolean allowFlight = false;

        private UserFlyingChanger(User user) {
            this.user = user;

            user.getPlayer().setExp(1f);
        }

        @Override
        public void run() {
            if(!user.isInGame()) Bukkit.getScheduler().cancelTask(taskId);

            if(user.getPlayer().isFlying()){
                float exp = user.getPlayer().getExp();
                exp = Math.max(exp - 0.005f, 0);
                user.getPlayer().setExp(exp);

                if(!bat){
                    Disguises.disguise(user, DisguiseType.Bat);
                    bat = true;
                }

                if(exp > 0) return;
                user.getPlayer().setAllowFlight(allowFlight = false);
            } else {
                float exp = user.getPlayer().getExp();
                exp = Math.min(exp + 0.003f, 1);
                user.getPlayer().setExp(exp);

                if(exp > 0.1f && !allowFlight) user.getPlayer().setAllowFlight(allowFlight = true);

                if(bat) {
                    Disguises.unDisguise(user);
                    bat = false;
                }
            }
        }

        public void startTask(WarsPlugin plugin) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
