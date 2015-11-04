package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 04/11/15.
 */
public class FarmInventory implements InventoryHandler {

    private int rawPotatoCost = 4;
    private int cookieCost = 2;
    private int rawBeefCost = 6;
    private int bakedPotatoCost = 5;
    private int cookedBeefCost = 10;
    private int goldenAppleCost = 200;

    @Override
    public void onInventoryClick(ItemStack item, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        if (inv.firstEmpty() == -1) {
            playerInfo.getPlayer().sendMessage("Please ensure you have one free slot in your inventory.");
            return;
        }

        int cost = 0;
        ItemStack add = null;

        switch (item.getType()) {
            case POTATO:
                cost = rawPotatoCost * item.getAmount();
                add = new ItemStack(Material.POTATO, item.getAmount());
                break;
            case COOKIE:
                cost = cookieCost * item.getAmount();
                add = new ItemStack(Material.COOKIE, item.getAmount());
                break;
            case RAW_BEEF:
                cost = rawBeefCost * item.getAmount();
                add = new ItemStack(Material.RAW_BEEF, item.getAmount());
                break;
            case BAKED_POTATO:
                cost = bakedPotatoCost * item.getAmount();
                add = new ItemStack(Material.BAKED_POTATO, item.getAmount());
                break;
            case COOKED_BEEF:
                cost = cookedBeefCost * item.getAmount();
                add = new ItemStack(Material.COOKED_BEEF, item.getAmount());
                break;
            case GOLDEN_APPLE:
                cost = goldenAppleCost * item.getAmount();
                add = new ItemStack(Material.GOLDEN_APPLE, item.getAmount());
                break;
        }

        if(!playerInfo.subtractPlayerCash(cost)){
            playerInfo.getPlayer().sendMessage("You don't have that amount of money!");
            return;
        }

        inv.addItem(add);
        InventoryUtils.playBuySound(playerInfo.getPlayer());

    }

    @Override
    public List<ItemStack> getInventoryContents(PlayerInfo playerInfo, TeamInfo teamInfo) {
        ArrayList<ItemStack> result = new ArrayList<>();
        result.add(InventoryUtils
                .createShopItem(Material.POTATO, 5, 0, null, "Lovely potatoes!", rawPotatoCost * 5, false));
        result.add(
                InventoryUtils.createShopItem(Material.COOKIE, 10, 0, null, "Nice cookies!", cookieCost * 10, false));
        result.add(InventoryUtils
                .createShopItem(Material.RAW_BEEF, 5, 0, null, "Raw meat is good for you.", rawBeefCost * 5, false));
        result.add(InventoryUtils
                .createShopItem(Material.BAKED_POTATO, 10, 0, null, "Properly cooked potatoes!", bakedPotatoCost * 10,
                        false));
        result.add(InventoryUtils
                .createShopItem(Material.COOKED_BEEF, 10, 0, null, "Fast food!", cookedBeefCost * 10, false));
        result.add(InventoryUtils
                .createShopItem(Material.GOLDEN_APPLE, 1, 0, null, "May have side effects!", goldenAppleCost, false));
        return result;
    }
}
