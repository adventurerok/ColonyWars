package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.event.*;
import com.ithinkrok.oldmccw.inventory.Buyable;
import com.ithinkrok.oldmccw.inventory.BuyableInventory;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import org.bukkit.Material;

import java.util.*;

/**
 * Created by paul on 18/11/15.
 * <p>
 * Handles classes that use ClassItems
 */
public class ClassItemClassHandler extends BuyableInventory implements PlayerClassHandler {

    private Map<Material, ClassItem> classItemHashMap = new LinkedHashMap<>();

    public ClassItemClassHandler(ClassItem... items) {
        this(Arrays.asList(items));
    }

    public ClassItemClassHandler(List<ClassItem> items) {
        addExtraClassItems(items);
    }

    private static List<Buyable> calculateBuyables(List<ClassItem> items) {
        List<Buyable> result = new ArrayList<>();

        for (ClassItem item : items) {
            item.addBuyablesToList(result);
        }

        return result;
    }

    public void addExtraClassItems(ClassItem...extra){
        addExtraClassItems(Arrays.asList(extra));
    }

    public void addExtraClassItems(List<ClassItem> extra) {
        addExtraBuyables(calculateBuyables(extra));

        for (ClassItem item : extra) {
            classItemHashMap.put(item.getItemMaterial(), item);
        }
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        for (ClassItem item : classItemHashMap.values()) {
            item.onBuildingBuilt(event);
        }
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        for (ClassItem item : classItemHashMap.values()) {
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
        for (ClassItem item : classItemHashMap.values()) {
            item.onUserUpgrade(event);
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if (event.getItem() == null) return;
        ClassItem item = classItemHashMap.get(event.getItem().getType());
        if (item == null) return;

        item.onUserAttack(event);
    }

    @Override
    public void onUserAttacked(UserAttackedEvent event) {
        //Ignore for the moment. Subclasses can override this
    }

    @Override
    public void onAbilityCooldown(UserAbilityCooldownEvent event) {
        for (ClassItem item : classItemHashMap.values()) {
            item.onAbilityCooldown(event);
        }
    }
}
