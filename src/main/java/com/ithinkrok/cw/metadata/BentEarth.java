package com.ithinkrok.cw.metadata;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.base.metadata.UserMetadata;
import com.ithinkrok.minigames.base.util.SoundEffect;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;

/**
 * Created by paul on 20/01/16.
 */
public class BentEarth extends UserMetadata {

    private List<FallingBlock> blocks;

    public BentEarth(List<FallingBlock> blocks, List<LivingEntity> riders) {
        this.blocks = blocks;

        HashSet<FallingBlock> taken = new HashSet<>();
        for (LivingEntity living : riders) {
            double closest = 99999999D;
            FallingBlock block = null;

            for (FallingBlock b : blocks) {
                double dist = b.getLocation().distanceSquared(living.getLocation());
                if (dist >= closest) continue;
                if (taken.contains(b)) continue;

                closest = dist;
                block = b;
            }

            if (block == null) continue;
            taken.add(block);
            block.setPassenger(living);
        }
    }

    public void addVelocity(Vector add) {
        for (FallingBlock block : blocks) {
            if (block.isDead() || block.isOnGround()) continue;

            Vector velocity = block.getVelocity();
            velocity.add(add);
            block.setVelocity(velocity);
        }
    }

    public void playKnockSound(User priest, SoundEffect knockback) {
        priest.playSound(priest.getLocation(), knockback);

        for (FallingBlock block : blocks) {
            if (block.isDead() || block.isOnGround()) continue;

            block.getWorld()
                    .playSound(block.getLocation(), knockback.getSound(), knockback.getVolume(), knockback.getPitch());
            return;
        }
    }

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return false;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }
}
