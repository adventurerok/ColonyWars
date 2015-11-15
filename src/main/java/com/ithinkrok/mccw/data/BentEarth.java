package com.ithinkrok.mccw.data;

import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by paul on 15/11/15.
 *
 * Stores a list of FallingBlock entities created by an earth bender
 */
public class BentEarth {

    private List<FallingBlock> blocks;

    public BentEarth(List<FallingBlock> blocks) {
        this.blocks = blocks;
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
