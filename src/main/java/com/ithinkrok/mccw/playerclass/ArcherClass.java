package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.event.ItemPurchaseEvent;
import com.ithinkrok.mccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import com.ithinkrok.mccw.util.item.TeamCompass;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 10/11/15.
 * <p>
 * Handles the archer class
 */
public class ArcherClass extends ClassItemClassHandler {

    public ArcherClass(WarsPlugin plugin, FileConfiguration config) {
        super(new ClassItem(plugin.getLangFile(), Material.BOW).withUpgradeBuildings(Buildings.LUMBERMILL)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.ARROW_KNOCKBACK, "bow", new LinearCalculator(0, 1)),
                        new ClassItem.EnchantmentEffect(Enchantment.ARROW_DAMAGE, "bow", new ArrayCalculator(0, 1, 3)))
                        .withUpgradables(new ClassItem.Upgradable("bow", "upgrades.bow.name", 2,
                                configArrayCalculator(config, "costs.archer.bow", 2))),
                new ClassItem(plugin.getLangFile(), Material.WOOD_SWORD).withUpgradeBuildings(Buildings.LUMBERMILL)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sword", new LinearCalculator(0, 1)),
                        new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "sword", new LinearCalculator(0, 1)))
                        .withUpgradables(new ClassItem.Upgradable("sword", "upgrades.wood-sword.name", 2,
                                configArrayCalculator(config, "costs.archer.sword", 2))),
                TeamCompass.createTeamCompass(plugin, config));

        addExtraBuyables(new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.ARROW, 64, 0, plugin.getLocale("upgrades.arrows.name", 1)),
                Buildings.LUMBERMILL, config.getInt("costs.archer.arrows1"), "arrows", 1), new ItemBuyable(
                InventoryUtils
                        .createItemWithNameAndLore(Material.ARROW, 1, 0, plugin.getLocale("upgrades.arrows.name", 2),
                                plugin.getLocale("upgrades.arrows2.desc")), new ItemStack(Material.ARROW, 64),
                Buildings.LUMBERMILL, config.getInt("costs.archer.arrows2"), false, true) {
            @Override
            public void onPurchase(ItemPurchaseEvent event) {
                for (int i = 0; i < 3; ++i) super.onPurchase(event);
            }

            @Override
            public boolean canBuy(ItemPurchaseEvent event) {
                return super.canBuy(event) && event.getUser().getUpgradeLevel("arrows") > 0;
            }
        });
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        super.onBuildingBuilt(event);
        if (!Buildings.LUMBERMILL.equals(event.getBuilding().getBuildingName())) return;

        PlayerInventory inv = event.getUserInventory();
        inv.addItem(new ItemStack(Material.ARROW, 32));
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        super.onPlayerUpgrade(event);
        switch (event.getUpgradeName()) {
            case "arrows":
                event.getUserInventory().addItem(new ItemStack(Material.ARROW, 64));
                break;
        }
    }

}
