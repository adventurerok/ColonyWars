package com.ithinkrok.mccw.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by paul on 03/11/15.
 */
public class InventoryUtils {

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack createShopItem(Material mat, int amount,  int damage, String name, String desc, int cost,
                                           boolean team){
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        String teamText = team ? " (Team Cash)" : " (Player Cash)";

        return setItemNameAndLore(stack, name, new String[]{desc, "Cost: " + cost + teamText});
    }
}
