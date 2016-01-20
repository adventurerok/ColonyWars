package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;

/**
 * Created by paul on 20/01/16.
 */
public class WolfWand implements Listener {

    @MinigamesEventHandler
    public void onInteract(UserInteractEvent event) {
        if(!event.hasBlock() || !event.getUser().isPlayer()) return;

        Location target = event.getClickedBlock().getLocation();
        BlockFace face = event.getBlockFace();
        target = target.clone().add(face.getModX(), face.getModY(), face.getModZ());

        Wolf wolf = (Wolf) event.getUserGameGroup().getCurrentMap().spawnEntity(target, EntityType.WOLF);
        wolf.setCollarColor(event.getUser().getTeamIdentifier().getDyeColor());
        wolf.setOwner(event.getUser().getPlayer());
        wolf.setHealth(wolf.getMaxHealth());

        event.setStartCooldownAfterAction(true);
    }
}
