package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by paul on 03/11/15.
 */
public class InventoryUtils {

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        if(name != null) im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack createShopItem(Material mat, int amount,  int damage, String name, String desc, int cost,
                                           boolean team){
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        String teamText = team ? " (Team Money)" : " (Player Money)";

        if(desc != null) return setItemNameAndLore(stack, name, desc, "Cost: " + cost + teamText);
        else return setItemNameAndLore(stack, name, "Cost: " + cost + teamText);
    }

    public static ItemStack createShopItemWithEnchantments(Material mat, int amount, int damage, String name, String desc, int cost,
                                                           boolean team, Object...enchantments){
        ItemStack stack = createShopItem(mat, amount, damage, name, desc, cost, team);

        return enchantItem(stack, enchantments);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount,  int damage, String name,
                                                      String...lore){
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static ItemStack enchantItem(ItemStack item, Object...enchantments){
        for(int i = 0; i < enchantments.length; i += 2){
            item.addUnsafeEnchantment((Enchantment)enchantments[i], (Integer)enchantments[i + 1]);
        }

        return item;
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

    public static void playBuySound(Player player){
        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1.0f, 1.0f);
    }
}
