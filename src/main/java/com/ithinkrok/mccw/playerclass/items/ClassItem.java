package com.ithinkrok.mccw.playerclass.items;

import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.inventory.Buyable;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 18/11/15.
 * <p>
 * An item available for a class
 */
public class ClassItem {

    private PlayerClass playerClass;
    private String[] upgradeBuildings;
    private boolean unlockOnBuildingBuild;
    private InteractAction rightClickAction;
    private InteractAction leftClickAction;
    private WeaponModifier weaponModifier;
    private String itemDisplayName;
    private Material itemMaterial;
    private String rightClickCooldownUpgrade;
    private Calculator rightClickCooldown;
    private Upgradable[] upgradables;
    private EnchantmentEffect[] enchantmentEffects;

    public ClassItem(PlayerClass playerClass, Material itemMaterial, String itemDisplayName) {
        this.playerClass = playerClass;
        this.itemMaterial = itemMaterial;
        this.itemDisplayName = itemDisplayName;
    }

    public ClassItem withUpgradeBuildings(String... upgradeBuildings) {
        this.upgradeBuildings = upgradeBuildings;
        return this;
    }

    public ClassItem withUnlockOnBuildingBuild(boolean unlockOnBuildingBuild) {
        this.unlockOnBuildingBuild = unlockOnBuildingBuild;
        return this;
    }

    public ClassItem withRightClickAction(InteractAction rightClickAction) {
        this.rightClickAction = rightClickAction;
        return this;
    }

    public ClassItem withLeftClickAction(InteractAction leftClickAction) {
        this.leftClickAction = leftClickAction;
        return this;
    }

    public ClassItem withWeaponModifier(WeaponModifier weaponModifier) {
        this.weaponModifier = weaponModifier;
        return this;
    }

    public ClassItem withRightClickCooldown(String upgradeName, Calculator rightClickCooldown) {
        this.rightClickCooldownUpgrade = upgradeName;
        this.rightClickCooldown = rightClickCooldown;
        return this;
    }

    public ClassItem withUpgradables(Upgradable... upgradables) {
        this.upgradables = upgradables;
        return this;
    }

    public ClassItem withEnchantmentEffects(EnchantmentEffect... enchantmentEffects) {
        this.enchantmentEffects = enchantmentEffects;
        return this;
    }

    public void addBuyablesToList(List<Buyable> buyables) {
        for (Upgradable upgradable : upgradables) {
            for (int level = 1; level <= upgradable.maxLevel; ++level) {
                Map<String, Integer> upgradeLevels = new HashMap<>();
                upgradeLevels.put(upgradable.upgradeName, level);

                ItemStack display = createItemFromUpgradeLevels(upgradeLevels);
                display.getItemMeta().setDisplayName(String.format(upgradable.upgradeDisplayName, level));

                UpgradeBuyable buyable =
                        new UpgradeBuyable(display, upgradeBuildings[0], (int) upgradable.upgradeCost.calculate(level),
                                upgradable.upgradeName, level);

                for(int buildingIndex = 1; buildingIndex < upgradeBuildings.length; ++buildingIndex){
                    buyable.withAdditionalBuildings(upgradeBuildings[buildingIndex]);
                }

                buyables.add(buyable);
            }
        }
    }

    private ItemStack createItemFromUpgradeLevels(Map<String, Integer> upgradeLevels) {
        List<String> lore = new ArrayList<>();

        if (weaponModifier != null) {
            weaponModifier.addLoreItems(lore, upgradeLevels);
        }

        if (rightClickCooldown != null) {
            lore.add("Cooldown: " +
                    rightClickCooldown.calculate(getUpgradeLevel(upgradeLevels, rightClickCooldownUpgrade)) +
                    " seconds");
        }

        String[] loreArray = new String[lore.size()];
        lore.toArray(loreArray);

        ItemStack item = InventoryUtils.createItemWithNameAndLore(itemMaterial, 1, 0, itemDisplayName, loreArray);

        for (EnchantmentEffect enchantmentEffect : enchantmentEffects) {
            int level = getUpgradeLevel(upgradeLevels, enchantmentEffect.upgradeName);
            int enchantmentLevel = (int) enchantmentEffect.levelCalculator.calculate(level);
            if (enchantmentLevel <= 0) continue;

            item.addEnchantment(enchantmentEffect.enchantment, enchantmentLevel);
        }

        return item;
    }

    private static int getUpgradeLevel(Map<String, Integer> upgradeLevels, String upgrade) {
        Integer level = upgradeLevels.get(upgrade);
        if (level == null) return 0;
        else return level;
    }

    public ItemStack createItemForUser(User user) {
        return createItemFromUpgradeLevels(user.getUpgradeLevels());
    }

    public interface InteractAction {
        boolean onInteractWorld(UserInteractEvent event);
    }

    public static class WeaponModifier {
        private String upgradeName;

        /**
         * Calculates damage in hearts
         */
        private Calculator damageCalculator;

        /**
         * Calculates wither effect duration in seconds
         */
        private Calculator witherCalculator;
        private Calculator fireCalculator;
        private Calculator nauseaCalculator;

        public WeaponModifier(String upgradeName) {
            this.upgradeName = upgradeName;
        }

        public WeaponModifier withDamageCalculator(Calculator damageCalculator) {
            this.damageCalculator = damageCalculator;
            return this;
        }

        public WeaponModifier withWitherCalculator(Calculator witherCalculator) {
            this.witherCalculator = witherCalculator;
            return this;
        }

        public WeaponModifier withFireCalculator(Calculator fireCalculator) {
            this.fireCalculator = fireCalculator;
            return this;
        }

        public WeaponModifier withNauseaCalculator(Calculator nauseaCalculator) {
            this.nauseaCalculator = nauseaCalculator;
            return this;
        }

        public void addLoreItems(List<String> lore, Map<String, Integer> upgradeLevels) {
            int upgradeLevel = getUpgradeLevel(upgradeLevels, upgradeName);

            if (damageCalculator != null) lore.add("Damage: " + damageCalculator.calculate(upgradeLevel) + " Hearts");
            if (fireCalculator != null)
                lore.add("Fire Duration: " + fireCalculator.calculate(upgradeLevel) + " seconds");
            if (nauseaCalculator != null)
                lore.add("Nausea Duration: " + nauseaCalculator.calculate(upgradeLevel) + " seconds");
            if (witherCalculator != null)
                lore.add("Wither Duration: " + witherCalculator.calculate(upgradeLevel) + " seconds");
        }

    }

    public static class Upgradable {
        private String upgradeName;
        private String upgradeDisplayName;
        private int maxLevel;
        private Calculator upgradeCost;

        public Upgradable(String upgradeName, String upgradeDisplayName, int maxLevel, Calculator upgradeCost) {
            this.upgradeName = upgradeName;
            this.upgradeDisplayName = upgradeDisplayName;
            this.maxLevel = maxLevel;
            this.upgradeCost = upgradeCost;
        }
    }

    public static class EnchantmentEffect {
        private Enchantment enchantment;
        private String upgradeName;
        private Calculator levelCalculator;

        public EnchantmentEffect(Enchantment enchantment, String upgradeName, Calculator levelCalculator) {
            this.enchantment = enchantment;
            this.upgradeName = upgradeName;
            this.levelCalculator = levelCalculator;
        }
    }
}
