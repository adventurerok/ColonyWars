package com.ithinkrok.mccw.data;

import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;

/**
 * Created by paul on 15/11/15.
 *
 * Stores a list of FallingBlock entities created by an earth bender
 */
public class BentEarth {

    private List<FallingBlock> blocks;

    public BentEarth(List<FallingBlock> blocks, List<Player> riders) {
        this.blocks = blocks;

        HashSet<FallingBlock> taken = new HashSet<>();
        for(Player p : riders){
            double closest = 99999999D;
            FallingBlock block = null;

            for(FallingBlock b : blocks){
                double dist = b.getLocation().distanceSquared(p.getLocation());
                if(dist >= closest) continue;
                if(taken.contains(b)) continue;

                closest = dist;
                block = b;
            }

            if(block == null) continue;
            taken.add(block);
            block.setPassenger(p);
        }
    }

    public void addVelocity(Vector add){
        for(FallingBlock block : blocks){
            if(block.isDead() || block.isOnGround()) continue;

            Vector velocity = block.getVelocity();
            velocity.add(add);
            block.setVelocity(velocity);
        }
    }
}
