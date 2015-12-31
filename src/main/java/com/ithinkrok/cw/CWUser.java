package com.ithinkrok.cw;

import com.ithinkrok.minigames.User;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public class CWUser extends User<CWUser, CWTeam, CWGameGroup, CWGame> {


    public CWUser(CWGame minigame, CWGameGroup gameGroup, CWTeam team, UUID uuid, LivingEntity entity) {
        super(minigame, gameGroup, team, uuid, entity);
    }
}
