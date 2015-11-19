package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.inventory.Buyable;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.mccw.playerclass.items.Calculator;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 18/11/15.
 *
 * Handles classes that use ClassItems
 */
public class ClassItemClassHandler extends BuyableInventory implements PlayerClassHandler {

    private HashMap<Material, ClassItem> classItemHashMap;

    public ClassItemClassHandler(ClassItem...items){
        this(Arrays.asList(items));
    }

    public ClassItemClassHandler(List<ClassItem> items){
        super(calculateBuyables(items));

        for(ClassItem item : items){
            classItemHashMap.put(item.getItemMaterial(), item);
        }
    }

    private static List<Buyable> calculateBuyables(List<ClassItem> items){
        List<Buyable> result = new ArrayList<>();

        for(ClassItem item : items){
            item.addBuyablesToList(result);
        }

        return result;
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        for(ClassItem item : classItemHashMap.values()){
            item.onBuildingBuilt(event);
        }
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        for(ClassItem item : classItemHashMap.values()){
            item.onUserBeginGame(event);
        }
    }

    @Override
    public boolean onInteract(UserInteractEvent event) {
        if (event.getItem() == null) return false;
        ClassItem item = classItemHashMap.get(event.getItem().getType());
        return item != null && item.onInteract(event);

    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        for(ClassItem item : classItemHashMap.values()){
            item.onUserUpgrade(event);
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if(event.getItem() == null) return;
        ClassItem item = classItemHashMap.get(event.getItem().getType());
        if(item == null) return;

        item.onUserAttack(event);
    }

    protected static Calculator configArrayCalculator(FileConfiguration config, String base, int maxLevel){
        double[] returnValues = new double[maxLevel + 1];

        for(int level = 0; level <= maxLevel; ++level){
            returnValues[level] = config.getDouble(base + level);
        }

        return new ArrayCalculator(returnValues);
    }
}
