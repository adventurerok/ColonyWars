package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 15/11/15.
 * <p>
 * Handles the warrior class
 */
public class WarriorClass extends BuyableInventory implements PlayerClassHandler {

    private WarsPlugin plugin;

    public WarriorClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.IRON_SWORD, 1, 0, "Sharpness Upgrade 1", null,
                        Enchantment.DAMAGE_ALL, 1), Buildings.BLACKSMITH, config.getInt("costs.warrior.sharpness1"),
                "sharpness", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.IRON_SWORD, 1, 0, "Sharpness Upgrade 2", null,
                        Enchantment.DAMAGE_ALL, 2), Buildings.BLACKSMITH, config.getInt("costs.warrior.sharpness2"),
                "sharpness", 2), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.IRON_SWORD, 1, 0, "Knockback Upgrade 1", null,
                        Enchantment.KNOCKBACK, 1), Buildings.BLACKSMITH, config.getInt("costs.warrior.knockback1"),
                "knockback", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.IRON_SWORD, 1, 0, "Knockback Upgrade 2", null,
                        Enchantment.KNOCKBACK, 2), Buildings.BLACKSMITH, config.getInt("costs.warrior.knockback2"),
                "knockback", 2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_HELMET, 1, 0, "Wolf Wand Upgrade 1", "Cooldown: 90 seconds"),
                Buildings.BLACKSMITH, config.getInt("costs.warrior.wolf1"), "wolf", 1), new UpgradeBuyable(
                InventoryUtils.createItemWithNameAndLore(Material.GOLD_HELMET, 1, 0, "Wolf Wand Upgrade 2",
                        "Cooldown: 60 seconds"), Buildings.BLACKSMITH, config.getInt("costs.warrior.wolf2"), "wolf",
                2));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        switch (buildingName) {
            case Buildings.BLACKSMITH:
                user.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                user.getPlayer().getInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.GOLD_HELMET, 1, 0, "Wolf Wand",
                                "Cooldown: 120 seconds"));
                break;
        }
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if(event.getItem() == null || event.getItem().getType() != Material.GOLD_HELMET) return false;
        User user = event.getUserClicked();
        int cooldown = 120 - 30 * user.getUpgradeLevel("wolf");
        if(!user.startCoolDown("wolf", cooldown, plugin.getLocale("wolf-wand-cooldown"))) return true;

        Location target = event.getClickedBlock().getLocation();
        BlockFace face = event.getBlockFace();
        target = target.clone().add(face.getModX(), face.getModY(), face.getModZ());

        Wolf wolf = (Wolf) target.getWorld().spawnEntity(target, EntityType.WOLF);
        wolf.setCollarColor(user.getTeamColor().dyeColor);
        wolf.setOwner(user.getPlayer());

        return true;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "sharpness":
            case "knockback":
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, event.getUser().getUpgradeLevel("sharpness"),
                        Enchantment.KNOCKBACK, event.getUser().getUpgradeLevel("knockback"));

                InventoryUtils.replaceItem(event.getUserInventory(), sword);

                break;
            case "wolf":
                int cooldown = 120 - 30 * event.getUpgradeLevel();
                ItemStack wand = InventoryUtils.createItemWithNameAndLore(Material.GOLD_HELMET, 1, 0,
                        "Wolf Wand", "Cooldown: " + cooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), wand);
                break;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {

    }
}
