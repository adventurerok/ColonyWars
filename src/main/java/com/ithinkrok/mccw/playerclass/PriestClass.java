package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackUserEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 15/11/15.
 * <p>
 * Handles the priest class
 */
public class PriestClass extends BuyableInventory implements PlayerClassHandler {

    private WarsPlugin plugin;

    public PriestClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll Upgrade 1",
                        "Cooldown: 150 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.healing1"),
                "healing", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll Upgrade 2",
                        "Cooldown: 60 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.healing2"), "healing",
                2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender Upgrade 1",
                        "Cooldown: 30 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.bender1"), "bender",
                1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender Upgrade 2",
                        "Cooldown: 15 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.bender2"), "bender",
                2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross Upgrade 1", "Damage: 3.0 Hearts"),
                Buildings.CATHEDRAL, config.getInt("costs.priest.cross1"), "cross", 1), new UpgradeBuyable(
                InventoryUtils.createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross Upgrade 2",
                        "Damage: 4.0 Hearts"), Buildings.CATHEDRAL, config.getInt("costs.priest.cross2"), "cross", 2));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        switch (buildingName) {
            case Buildings.CATHEDRAL:
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll",
                                "Cooldown: 240 seconds"));
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender",
                                "Cooldown: 45 seconds"));
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross", "Damage: 2.0 Hearts"));
                break;
        }
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch(event.getUpgradeName()){
            case "healing":
                int healingCooldown = 240 - 90 * event.getUpgradeLevel();
                ItemStack scroll = InventoryUtils.createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0,
                        "Healing Scroll", "Cooldown: " + healingCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), scroll);
                break;
            case "bender":
                int benderCooldown = 45 - 15 * event.getUpgradeLevel();
                ItemStack bender = InventoryUtils.createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0,
                        "Earth Bender", "Cooldown: " + benderCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), bender);
                break;
            case "cross":
                double damage = 2.0 + 1.0 * event.getUpgradeLevel();
                ItemStack cross = InventoryUtils.createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0,
                        "Cross", "Damage: " + damage + " Hearts");
                InventoryUtils.replaceItem(event.getUserInventory(), cross);
                break;

        }
    }

    @Override
    public void onUserAttackUser(UserAttackUserEvent event) {
        ItemStack item = event.getWeapon();
        if(item == null || item.getType() != Material.GOLD_LEGGINGS) return;

        double damage = 4 + 2 * event.getAttacker().getUpgradeLevel("cross");
        event.setDamage(damage);
    }
}
