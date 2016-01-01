package com.ithinkrok.oldmccw.data;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.minigames.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Represents a showdown arena
 */
public class ShowdownArena {

    private int radiusX, radiusZ;
    private Location center;
    private BoundingBox bounds;

    public ShowdownArena(int radiusX, int radiusZ, Location center, BoundingBox bounds) {
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.center = center;
        this.bounds = bounds;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public int getRadiusZ() {
        return radiusZ;
    }

    public Location getCenter() {
        return center;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public boolean checkUserMove(User user, Location target) {
        if (isInBounds(target)) return false;

        double xv = 0;
        if (target.getX() > center.getX() + radiusX - 2) xv = -1;
        else if (target.getX() < center.getX() - radiusZ + 2) xv = 1;

        double zv = 0;
        if (target.getZ() > center.getZ() + radiusZ - 2) zv = -1;
        else if (target.getZ() < center.getZ() - radiusZ + 2) zv = 1;

        Vector velocity = new Vector();

        velocity.setX(velocity.getX() + xv * 0.25);
        velocity.setY(0.1);
        velocity.setZ(velocity.getZ() + zv * 0.25);

        if(!user.isInsideVehicle()) user.setVelocity(velocity);
        else user.getVehicle().setVelocity(velocity);

        return true;
    }

    public boolean isInBounds(Location loc) {
        double xd = Math.abs(loc.getX() - center.getX());
        double zd = Math.abs(loc.getZ() - center.getZ());

        return !(xd > radiusX || zd > radiusZ);
    }

    public void startShrinkTask(WarsPlugin plugin){
        int startTime = 20 * 60 * 3;

        plugin.getGameInstance().scheduleTask(() -> {
            plugin.messageAllLocale("showdown.shrinking");
        }, startTime);

        plugin.getGameInstance().scheduleRepeatingTask(() -> {
            shrinkArena(plugin);
        }, startTime, 20 * 20);
    }

    public void shrinkArena(WarsPlugin plugin) {
        if(radiusX == 5 && radiusZ == 5) return;

        if(radiusX > 5) radiusX -= 1;
        if(radiusZ > 5) radiusZ -= 1;

        for(User user : plugin.getUsers()){
            if(!user.isInGame()) continue;

            checkUserMove(user, user.getLocation());
        }

//        ItemStack potion = InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 1);
//
//        if(radiusX == 5 && radiusZ == 5) {
//            plugin.messageAllLocale("showdown.potions-gone");
//        }
//
//        for(User user : plugin.getUsers()) {
//            if(!user.isInGame()) continue;
//
//            PlayerInventory inv = user.getPlayerInventory();
//
//            for(int index = 0; index < inv.getSize(); ++index) {
//                if(inv.getItem(index) == null || !inv.getItem(index).isSimilar(potion)) continue;
//
//                inv.setItem(index, null);
//                if(radiusX + radiusZ > 10) {
//                    user.sendLocale("showdown.potion-leech");
//                    break;
//                }
//            }
//        }
    }
}
