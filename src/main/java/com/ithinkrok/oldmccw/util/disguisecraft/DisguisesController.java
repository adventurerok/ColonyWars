package com.ithinkrok.oldmccw.util.disguisecraft;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.entity.EntityType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

/**
 * Created by paul on 13/12/15.
 *
 * Handles disguises
 */
public class DisguisesController {

    private DisguiseCraftAPI dcAPI = DisguiseCraft.getAPI();

    public void disguise(User user, EntityType type) {
        if(!user.isPlayer()) return;

        //Uses a hack that will convert most EntityTypes to DisguiseTypes. DOES NOT WORK FOR ALL (e.g. TNT)
        Disguise disguise = new Disguise(dcAPI.newEntityID(), DisguiseType.fromString(type.name().replace("_", "")));

        if(dcAPI.isDisguised(user.getPlayer())) dcAPI.changePlayerDisguise(user.getPlayer(), disguise);
        else dcAPI.disguisePlayer(user.getPlayer(), disguise);
    }

    public void unDisguise(User user) {
        if(!user.isPlayer()) return;

        if(dcAPI.isDisguised(user.getPlayer())){
            dcAPI.undisguisePlayer(user.getPlayer());
        }
    }
}
