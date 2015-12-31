package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.enumeration.PlayerClass;
import com.ithinkrok.oldmccw.event.ItemPurchaseEvent;
import com.ithinkrok.oldmccw.event.UserBeginGameEvent;
import com.ithinkrok.oldmccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.oldmccw.event.UserUpgradeEvent;
import com.ithinkrok.oldmccw.inventory.ItemBuyable;
import com.ithinkrok.oldmccw.inventory.UpgradeBuyable;
import com.ithinkrok.oldmccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import com.ithinkrok.oldmccw.playerclass.items.LinearCalculator;
import com.ithinkrok.oldmccw.strings.Buildings;
import com.ithinkrok.oldmccw.util.item.InventoryUtils;
import com.ithinkrok.oldmccw.util.item.TeamCompass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 10/11/15.
 * <p>
 * Handles the archer class
 */
public class ArcherClass extends ClassItemClassHandler {

    public ArcherClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass.getName(), Material.BOW).withUpgradeBuildings(Buildings.LUMBERMILL)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.ARROW_DAMAGE, "bow", new ArrayCalculator(0, 1, 3)))
                        .withUpgradables(new ClassItem.Upgradable("bow", "upgrades.bow.name", 2)),
                new ClassItem(plugin, playerClass.getName(), Material.WOOD_SWORD)
                        .withUpgradeBuildings(Buildings.LUMBERMILL).withUnlockOnBuildingBuild(true)
                        .withEnchantmentEffects(new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sword",
                                        new LinearCalculator(0, 1)),
                                new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "sword",
                                        new LinearCalculator(0, 0.5d)))
                        .withUpgradables(new ClassItem.Upgradable("sword", "upgrades.wood-sword.name", 2)),
                TeamCompass.createTeamCompass(plugin));

        addExtraBuyables(new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.ARROW, 64, 0, plugin.getLocale("upgrades.arrows.name", 1)),
                        Buildings.LUMBERMILL, plugin.getWarsConfig().getClassItemCost(playerClass, "arrows1"), "arrows", 1),
                new ItemBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.ARROW, 1, 0, plugin.getLocale("upgrades.arrows.name", 2),
                                plugin.getLocale("upgrades.arrows2.desc")), new ItemStack(Material.ARROW, 64),
                        Buildings.LUMBERMILL, plugin.getWarsConfig().getClassItemCost(playerClass, "arrows2"), false,
                        true) {
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
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        event.getUser()
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
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
