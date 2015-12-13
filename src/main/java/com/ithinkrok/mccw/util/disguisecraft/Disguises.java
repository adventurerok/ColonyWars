package com.ithinkrok.mccw.util.disguisecraft;

import com.ithinkrok.mccw.data.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 12/12/15.
 *
 *
 */
public class Disguises {

    private static DisguisesController disguisesController;

    static {
        if(Bukkit.getPluginManager().getPlugin("DisguiseCraft") != null) {
            disguisesController = new DisguisesController();
        }
    }


    public static void disguise(User user, EntityType type) {
        if(disguisesController != null) disguisesController.disguise(user, type);
    }

    public static void unDisguise(User user) {
        if(disguisesController != null) disguisesController.unDisguise(user);
    }
}
