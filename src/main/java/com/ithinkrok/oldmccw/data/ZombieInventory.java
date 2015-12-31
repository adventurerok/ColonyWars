package com.ithinkrok.oldmccw.data;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Created by paul on 22/12/15.
 */
public class ZombieInventory implements PlayerInventory {

    private Zombie zombie;
    private ItemStack[] contents;

    public ZombieInventory(Zombie zombie, ItemStack[] contents) {
        this.zombie = zombie;
        this.contents = contents;
    }

    @Override
    public ItemStack[] getArmorContents() {
        return zombie.getEquipment().getArmorContents();
    }

    @Override
    public ItemStack getHelmet() {
        return zombie.getEquipment().getHelmet();
    }

    @Override
    public ItemStack getChestplate() {
        return zombie.getEquipment().getChestplate();
    }

    @Override
    public ItemStack getLeggings() {
        return zombie.getEquipment().getLeggings();
    }

    @Override
    public ItemStack getBoots() {
        return zombie.getEquipment().getBoots();
    }

    @Override
    public int getSize() {
        return contents.length;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Override
    public String getName() {
        return "zombie";
    }

    @Override
    public ItemStack getItem(int index) {
        return contents[index];
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if(item != null && (item.getType() == Material.AIR || item.getAmount() == 0)) item = null;
        contents[index] = item;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> result = new HashMap<>();

        for(int index = 0; index < items.length; ++index) {
            ItemStack copy = items[index].clone();
            for(int i = 0; i < contents.length; ++i) {
                if(copy == null) break;

                ItemStack at = contents[i];

                if(at == null) contents[i] = copy;
                else if(at.isSimilar(copy)) {
                    int change = Math.min(copy.getAmount(), Math.max(at.getMaxStackSize() - at.getAmount(), 0));

                    at.setAmount(at.getAmount() + change);
                    copy.setAmount(copy.getAmount() - change);
                    if(copy.getAmount() == 0) copy = null;
                }
            }

            if(copy != null) result.put(index, copy);
        }

        return result;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack[] getContents() {
        return contents;
    }

    @Override
    public void setContents(ItemStack[] items) throws IllegalArgumentException {
        this.contents = items;
    }

    @Override
    public boolean contains(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        return first(material) != -1;
    }

    @Override
    public boolean contains(ItemStack item) {
        return first(item) != -1;
    }

    @Override
    public boolean contains(int materialId, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(Material material) throws IllegalArgumentException {
        for(int i = 0; i < contents.length; ++i) {
            if((material == null || material == Material.AIR) && contents[i] == null) return i;
            else if(contents[i] != null && contents[i].getType() == material) return i;
        }

        return -1;
    }

    @Override
    public int first(ItemStack item) {
        for(int i = 0; i < contents.length; ++i) {
            if(contents[i] != null && contents[i].equals(item)) return i;
        }

        return -1;
    }

    @Override
    public int firstEmpty() {
        return first(Material.AIR);
    }

    @Override
    public void remove(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {
        int first;

        while((first = first(material)) != -1) {
            setItem(first, null);
        }
    }

    @Override
    public void remove(ItemStack item) {
        int first;

        while((first = first(item)) != -1) {
            setItem(first, null);
        }
    }

    @Override
    public void clear(int index) {
        setItem(index, null);
    }

    @Override
    public void clear() {
        contents = new ItemStack[contents.length];
    }

    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public InventoryType getType() {
        return InventoryType.PLAYER;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        zombie.getEquipment().setArmorContents(items);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        zombie.getEquipment().setHelmet(helmet);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        zombie.getEquipment().setChestplate(chestplate);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        zombie.getEquipment().setLeggings(leggings);
    }

    @Override
    public void setBoots(ItemStack boots) {
        zombie.getEquipment().setBoots(boots);
    }

    @Override
    public ItemStack getItemInHand() {
        return zombie.getEquipment().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        zombie.getEquipment().setItemInHand(stack);
    }

    @Override
    public int getHeldItemSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeldItemSlot(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int clear(int id, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HumanEntity getHolder() {
        return null;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ItemStack> iterator(int index) {
        throw new UnsupportedOperationException();
    }
}
