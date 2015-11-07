package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 03/11/15.
 * <p>
 * Utility class for inventories
 */
public class InventoryUtils {

    public static ItemStack addPrice(ItemStack item, int cost, boolean team) {
        ItemMeta im = item.getItemMeta();

        String teamText = ChatColor.GRAY + " (" + ChatColor.GOLD + (team ? "Team Money" : "Player Money") +
                ChatColor.GRAY + ")";

        List<String> lore;
        if (im.hasLore()) lore = im.getLore();
        else lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GREEN + "$" + cost + teamText);
        im.setLore(lore);

        item.setItemMeta(im);

        return item;
    }

    public static ItemStack enchantItem(ItemStack item, Object... enchantments) {
        for (int i = 0; i < enchantments.length; i += 2) {
            int level = (int) enchantments[i + 1];
            if (level == 0) continue;

            item.addUnsafeEnchantment((Enchantment) enchantments[i], level);
        }

        return item;
    }

    public static ItemStack createPotion(PotionType type, int level, boolean splash, boolean extended, int amount){
        Potion pot = new Potion(type, level);
        pot.setSplash(splash);
        if(extended) pot.setHasExtendedDuration(true);

        return pot.toItemStack(amount);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        if (name != null) im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack createItemWithEnchantments(Material mat, int amount, int damage, String name, String desc,
                                                       Object... enchantments) {
        ItemStack stack;
        if (desc != null) stack = createItemWithNameAndLore(mat, amount, damage, name, desc);
        else stack = createItemWithNameAndLore(mat, amount, damage, name);

        return enchantItem(stack, enchantments);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount, int damage, String name,
                                                      String... lore) {
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static boolean payWithTeamCash(int amount, TeamInfo teamInfo, PlayerInfo playerInfo) {
        int teamAmount = Math.min(teamInfo.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        if (playerAmount > 0 && !playerInfo.subtractPlayerCash(playerAmount)) return false;

        teamInfo.subtractTeamCash(teamAmount);

        if (playerAmount > 0) playerInfo.message("Paid " + ChatColor.RED + "$" + playerAmount +
                ChatColor.YELLOW + " using your own money as your Team did not have enough!");

        return true;
    }

    public static boolean hasTeamCash(int amount, TeamInfo teamInfo, PlayerInfo playerInfo) {
        int teamAmount = Math.min(teamInfo.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        return !(playerAmount > 0 && !playerInfo.hasPlayerCash(playerAmount));

    }

    public static void playBuySound(Player player) {
        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1.0f, 1.0f);
    }

    public static void replaceItem(Inventory inventory, ItemStack stack) {
        int first = inventory.first(stack.getType());

        if (first == -1) inventory.addItem(stack);
        else inventory.setItem(first, stack);
    }
}
