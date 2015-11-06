package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 04/11/15.
 */
public class FarmInventory extends BuyableInventory {

    public FarmInventory(FileConfiguration config) {
        super(getBuyables(config));
    }

    private static List<Buyable> getBuyables(FileConfiguration config) {
        List<Buyable> result = new ArrayList<>();

        int rawPotatoAmount = config.getInt("amounts.farm.rawPotato");
        int cookieAmount = config.getInt("amounts.farm.cookie");
        int rawBeefAmount = config.getInt("amounts.farm.rawBeef");
        int bakedPotatoAmount = config.getInt("amounts.farm.bakedPotato");
        int cookedBeefAmount = config.getInt("amounts.farm.cookedBeef");
        int goldenAppleAmount = config.getInt("amounts.farm.goldenApple");

        result.add(new ItemBuyable(new ItemStack(Material.POTATO_ITEM, rawPotatoAmount), Buildings.FARM,
                config.getInt("costs.farm.rawPotato") * rawPotatoAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.COOKIE, cookieAmount), Buildings.FARM,
                config.getInt("costs.farm.cookie") * cookieAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.RAW_BEEF, rawBeefAmount), Buildings.FARM,
                config.getInt("costs.farm.rawBeef") * rawBeefAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.BAKED_POTATO, bakedPotatoAmount), Buildings.FARM,
                config.getInt("costs.farm.bakedPotato") * bakedPotatoAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.COOKED_BEEF, cookedBeefAmount), Buildings.FARM,
                config.getInt("costs.farm.cookedBeef") * cookedBeefAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.GOLDEN_APPLE, goldenAppleAmount), Buildings.FARM,
                config.getInt("costs.farm.goldenApple") * goldenAppleAmount, true));

        return result;
    }
}
