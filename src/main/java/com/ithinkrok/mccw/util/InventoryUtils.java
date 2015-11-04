package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by paul on 03/11/15.
 */
public class InventoryUtils {

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack createShopItem(Material mat, int amount,  int damage, String name, String desc, int cost,
                                           boolean team){
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        String teamText = team ? " (Team Money)" : " (Player Money)";

        return setItemNameAndLore(stack, name, desc, "Cost: " + cost + teamText);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount,  int damage, String name,
                                                      String...lore){
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static boolean payWithTeamCash(int amount, TeamInfo teamInfo, PlayerInfo playerInfo){
        int teamAmount = Math.min(teamInfo.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        if(playerAmount > 0 && !playerInfo.subtractPlayerCash(playerAmount)) return false;

        teamInfo.subtractTeamCash(teamAmount);

        if(playerAmount > 0) playerInfo.getPlayer().sendMessage("Payed " + playerAmount + " using your own money.");

        return true;
    }

    public static boolean hasTeamCash(int amount, TeamInfo teamInfo, PlayerInfo playerInfo){
        int teamAmount = Math.min(teamInfo.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        return !(playerAmount > 0 && !playerInfo.hasPlayerCash(playerAmount));

    }
}
