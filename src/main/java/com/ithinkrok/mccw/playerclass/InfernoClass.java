package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackUserEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 14/11/15.
 * <p>
 * Handles the inferno class
 */
public class InfernoClass extends BuyableInventory implements PlayerClassHandler {


    private final WarsPlugin plugin;

    public InfernoClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_CHESTPLATE, 1, 0, "Explosion Wand Upgrade 1",
                                "Cooldown: 15 seconds"), Buildings.MAGETOWER, config.getInt("costs.inferno.wand1"), "wand", 1),
                new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_CHESTPLATE, 1, 0, "Explosion Wand Upgrade 2",
                                "Cooldown: 5 seconds"), Buildings.MAGETOWER, config.getInt("costs.inferno.wand2"),
                        "wand", 2), new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_HELMET, 1, 0, "Flame Sword Upgrade 1",
                                "Damage: 2.5 Hearts", "Fire Aspect"), Buildings.BLACKSMITH,
                        config.getInt("costs.inferno.flame1"), "flame", 1), new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_HELMET, 1, 0, "Flame Sword Upgrade 2",
                                "Damage: 4.0 Hearts", "Fire Aspect"), Buildings.BLACKSMITH,
                        config.getInt("costs.inferno.flame2"), "flame", 2),
                new ItemBuyable(new ItemStack(Material.TNT, 16), Buildings.BLACKSMITH,
                        config.getInt("costs.inferno.tnt") * 16, true));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        switch (buildingName) {
            case Buildings.MAGETOWER:
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_CHESTPLATE, 1, 0, "Explosion Wand",
                                "Cooldown: 25 seconds"));
                break;
            case Buildings.BLACKSMITH:
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_HELMET, 1, 0, "Flame Sword", "Damage: 1.0 Hearts",
                                "Fire Aspect"));
                break;
        }
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public void onInteractWorld(UserInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        User user = event.getUserClicked();
        BlockFace mod = event.getBlockFace();

        switch (item.getType()) {
            case TNT:
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) break;
                user.createPlayerExplosion(event.getClickedBlock().getLocation().clone()
                        .add(mod.getModX() + 0.5, mod.getModY() + 0.5, mod.getModZ() + 0.5), 4F, false, 80);

                ItemStack oneLess = item.clone();
                if (oneLess.getAmount() > 1) oneLess.setAmount(oneLess.getAmount() - 1);
                else oneLess = null;
                user.getPlayer().setItemInHand(oneLess);
                event.setCancelled(true);
                break;
            case IRON_CHESTPLATE:
                if (!user.startCoolDown("wand", 25 - 10 * user.getUpgradeLevel("wand"),
                        plugin.getLocale("explosion-wand-cooldown"))) break;
                Block target = user.rayTraceBlocks(200);


                if (target == null) break;
                user.createPlayerExplosion(target.getLocation().clone()
                        .add(mod.getModX() + 0.5, mod.getModY() + 0.5, mod.getModZ() + 0.5),2F, false, 0);

                break;
        }
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "wand":
                int cooldown = 25 - event.getUpgradeLevel() * 10;
                ItemStack wand = InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_CHESTPLATE, 1, 0, "Explosion Wand",
                                "Cooldown: " + cooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), wand);
                break;
            case "flame":
                float damage = 1.0f + 1.5f * event.getUpgradeLevel();
                ItemStack sword = InventoryUtils.createItemWithNameAndLore(Material.DIAMOND_HELMET, 1, 0, "Flame Sword",
                        "Damage: " + damage + " Hearts", "Fire Aspect");
                InventoryUtils.replaceItem(event.getUserInventory(), sword);
                break;
        }
    }

    @Override
    public void onUserAttackUser(UserAttackUserEvent event) {
        ItemStack item = event.getWeapon();
        if (item == null || item.getType() != Material.DIAMOND_HELMET) return;

        double damage = 2 + 3 * event.getAttacker().getUpgradeLevel("flame");
        event.setDamage(damage);

        event.getTarget().setFireTicks(event.getAttacker(), 80);
    }
}
