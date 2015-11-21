package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 07/11/15.
 * <p>
 * Handles the Cloaker class.
 */
public class CloakerClass extends BuyableInventory implements PlayerClassHandler {


    //Will be updated *eventually*

    private final WarsPlugin plugin;

    public CloakerClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 35 seconds",
                        "Invisibility: 15 seconds"), Buildings.MAGETOWER, config.getInt("costs.cloaker.cloak1"),
                "cloak", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 45 seconds",
                        "Invisibility: 25 seconds"), Buildings.MAGETOWER, config.getInt("costs.cloaker.cloak2"),
                "cloak", 2));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        switch (event.getBuilding().getBuildingName()) {
            case Buildings.MAGETOWER:
                event.getUserInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 25 seconds",
                                "Invisibility: 10 seconds"));
                break;
        }
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        event.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
        event.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false), false);
    }

    @Override
    public boolean onInteract(UserInteractEvent event) {
        if (!event.isRightClick()) return false;
        if (event.getItem() == null || event.getItem().getType() != Material.IRON_LEGGINGS) return false;

        User user = event.getUser();

        if (user.isCoolingDown("cloaking")) {
            user.message(ChatColor.RED + "You are already cloaked!");
            return true;
        }

        if (user.isCoolingDown("cloak")) {
            user.message(ChatColor.RED + "Please wait for the cloak to cool down!");
            return true;
        }

        int cloak = 10;
        int cooldown = 25;

        switch (user.getUpgradeLevel("cloak")) {
            case 1:
                cloak = 15;
                cooldown = 35;
                break;
            case 2:
                cloak = 25;
                cooldown = 45;
                break;
        }

        user.startCoolDown("cloaking", cloak, null);

        user.cloak();

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, cloak * 20, 1, false, true), true);

        final int finalCooldown = cooldown;

        final int swirlTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                () -> user.getPlayer().getLocation().getWorld()
                        .playEffect(user.getPlayer().getLocation(), Effect.POTION_SWIRL, 0), 20, 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (user.isInGame()) user.decloak();
            Bukkit.getScheduler().cancelTask(swirlTask);
            user.message(ChatColor.RED + "Your cloak has run out!");
            user.startCoolDown("cloak", finalCooldown, "Your cloak has cooled down!");
            event.getPlayer()
                    .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false),
                            true);
        }, cloak * 20);

        return true;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "cloak":
                int cooldown = 35;
                int invisibility = 15;
                if (event.getUpgradeLevel() == 2) {
                    cooldown = 45;
                    invisibility = 25;
                }

                ItemStack cloak = InventoryUtils.createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak",
                        "Cooldown: " + cooldown + " seconds", "Invisibility: " + invisibility + " seconds");

                InventoryUtils.replaceItem(event.getUserInventory(), cloak);
                break;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {

    }

    @Override
    public void onAbilityCooldown(UserAbilityCooldownEvent event) {

    }
}
