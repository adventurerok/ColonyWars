package com.ithinkrok.minigames.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 31/12/15.
 */
public class InventoryUtils {

    public static boolean isEmpty(ItemStack stack){
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
    }
}
