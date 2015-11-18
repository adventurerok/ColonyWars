package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.inventory.Buyable;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.playerclass.items.ClassItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 18/11/15.
 *
 * Handles classes that use ClassItems
 */
public class ClassItemClassHandler extends BuyableInventory implements PlayerClassHandler {

    private List<ClassItem> classItemList;

    public ClassItemClassHandler(ClassItem...items){
        this(Arrays.asList(items));
    }

    public ClassItemClassHandler(List<ClassItem> items){
        super(calculateBuyables(items));
        classItemList = items;
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
        for(ClassItem item : classItemList){
            item.onBuildingBuilt(event);
        }
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        for(ClassItem item : classItemList){
            item.onUserUpgrade(event);
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        for(ClassItem item : classItemList){
            if(item.getItemMaterial() != event.getWeapon().getType()) continue;

            item.onUserAttack(event);
            break;
        }
    }
}
