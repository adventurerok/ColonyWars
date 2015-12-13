package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.data.User;
import org.bukkit.entity.EntityType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

/**
 * Created by paul on 12/12/15.
 *
 *
 */
public class Disguises {

    private static DisguiseCraftAPI dcAPI = DisguiseCraft.getAPI();


    public static void disguise(User user, EntityType type) {
        //Uses a hack that will convert most EntityTypes to DisguiseTypes. DOES NOT WORK FOR ALL (e.g. TNT)
        Disguise disguise = new Disguise(dcAPI.newEntityID(), DisguiseType.fromString(type.name().replace("_", "")));

        if(dcAPI.isDisguised(user.getPlayer())) dcAPI.changePlayerDisguise(user.getPlayer(), disguise);
        else dcAPI.disguisePlayer(user.getPlayer(), disguise);
    }

    public static void unDisguise(User user) {
        if(dcAPI.isDisguised(user.getPlayer())){
            dcAPI.undisguisePlayer(user.getPlayer());
        }
    }
}
