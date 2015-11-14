package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackUserEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.Buyable;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 14/11/15.
 *
 * Handles the dark knight class
 */
public class DarkKnightClass extends BuyableInventory implements PlayerClassHandler {

    public DarkKnightClass(FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils.createItemWithNameAndLore(Material.IRON_HELMET,
                1, 0, "Darkness Sword Upgrade 1", "Damage: 3.0 Hearts", "Nausea Duration: 7 seconds",
                "Wither Duration: 6 seconds"), Buildings.MAGETOWER, config.getInt("costs.dark_knight.sword1"),
                "sword", 1),
                new UpgradeBuyable(InventoryUtils.createItemWithNameAndLore(Material.IRON_HELMET,
                        1, 0, "Darkness Sword Upgrade 2", "Damage: 5.0 Hearts", "Nausea Duration: 8 seconds",
                        "Wither Duration: 10 seconds"), Buildings.MAGETOWER, config.getInt("costs.dark_knight.sword2"),
                        "sword", 2));
    }

    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        switch(buildingName){
            case Buildings.MAGETOWER:
                user.getPlayer().getInventory().addItem(InventoryUtils.createItemWithNameAndLore(Material.IRON_HELMET,
                        1, 0, "Darkness Sword", "Damage: 2.0 Hearts", "Nausea Duration: 5 seconds",
                        "Wither Duration: 3 seconds"));
                break;
        }
    }

    @Override
    public void onGameBegin(User user, Team team) {
        user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false,
                false), true);
    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        if(!event.getUpgradeName().equals("sword")) return;

        ItemStack sword;
        switch(event.getUpgradeLevel()){
            case 1:
                sword = InventoryUtils.createItemWithNameAndLore(Material.IRON_HELMET,
                        1, 0, "Darkness Sword", "Damage: 3.0 Hearts", "Nausea Duration: 7 seconds",
                        "Wither Duration: 6 seconds");
                break;
            case 2:
                sword = InventoryUtils.createItemWithNameAndLore(Material.IRON_HELMET,
                        1, 0, "Darkness Sword", "Damage: 5.0 Hearts", "Nausea Duration: 8 seconds",
                        "Wither Duration: 10 seconds");
                break;
            default:
                return;
        }

        InventoryUtils.replaceItem(event.getUserInventory(), sword);
    }

    @Override
    public void onUserAttackUser(UserAttackUserEvent event) {
        ItemStack item = event.getWeapon();
        if (item == null || item.getType() != Material.IRON_HELMET) return;

        double damage;
        int nausea;
        int wither;

        switch(event.getAttacker().getUpgradeLevel("sword")){
            case 0:
                damage = 4;
                nausea = 100;
                wither = 60;
                break;
            case 1:
                damage = 6;
                nausea = 140;
                wither = 120;
                break;
            case 2:
                damage = 10;
                nausea = 160;
                wither = 200;
                break;
            default:
                return;
        }

        event.setDamage(damage);
        event.getTarget().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, nausea, 0,
                false, true), true);
        event.getTarget().setWitherTicks(event.getAttacker(), wither);
    }
}
