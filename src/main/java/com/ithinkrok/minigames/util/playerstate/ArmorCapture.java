package com.ithinkrok.minigames.util.playerstate;

import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */

class ArmorCapture implements EntityEquipment {

    private ItemStack helmet, chestplate, leggings, boots, holding;

    @Override
    public ItemStack getItemInHand() {
        return holding;
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        if(InventoryUtils.isEmpty(stack)) stack = null;
        holding = stack;
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        if(InventoryUtils.isEmpty(helmet)) helmet = null;

        this.helmet = helmet;
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        if(InventoryUtils.isEmpty(chestplate)) chestplate = null;

        this.chestplate = chestplate;
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        if(InventoryUtils.isEmpty(leggings)) leggings = null;

        this.leggings = leggings;
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(ItemStack boots) {
        if(InventoryUtils.isEmpty(boots)) boots = null;

        this.boots = boots;
    }

    @Override
    public ItemStack[] getArmorContents() {
        List<ItemStack> armorList = new ArrayList<>();

        if(helmet != null) armorList.add(helmet);
        if(chestplate != null) armorList.add(chestplate);
        if(leggings != null) armorList.add(leggings);
        if(boots != null) armorList.add(boots);

        ItemStack[] result = new ItemStack[armorList.size()];
        armorList.toArray(result);

        return result;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {

    }

    @Override
    public void clear() {

    }

    @Override
    public float getItemInHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInHandDropChance(float chance) {

    }

    @Override
    public float getHelmetDropChance() {
        return 0;
    }

    @Override
    public void setHelmetDropChance(float chance) {

    }

    @Override
    public float getChestplateDropChance() {
        return 0;
    }

    @Override
    public void setChestplateDropChance(float chance) {

    }

    @Override
    public float getLeggingsDropChance() {
        return 0;
    }

    @Override
    public void setLeggingsDropChance(float chance) {

    }

    @Override
    public float getBootsDropChance() {
        return 0;
    }

    @Override
    public void setBootsDropChance(float chance) {

    }

    @Override
    public Entity getHolder() {
        return null;
    }
}
