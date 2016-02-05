package com.ithinkrok.cw.item;

import com.ithinkrok.cw.metadata.BentEarth;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.util.MinigamesConfigs;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.minigames.base.util.EntityUtils;
import com.ithinkrok.minigames.base.util.SoundEffect;
import com.ithinkrok.minigames.base.util.math.Calculator;
import com.ithinkrok.minigames.base.util.math.ExpressionCalculator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by paul on 20/01/16.
 */
public class EarthBender implements Listener {

    private Calculator maxMoves, moveVelocity;
    private SoundEffect knockback, spawn;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();

        maxMoves = new ExpressionCalculator(config.getString("max_moves"));
        moveVelocity = new ExpressionCalculator(config.getString("move_velocity", "1"));

        spawn = MinigamesConfigs.getSoundEffect(config, "spawn_sound");
        knockback = MinigamesConfigs.getSoundEffect(config, "knockback_sound");
    }

    @MinigamesEventHandler
    public void onInteract(UserInteractEvent event) {
        if (event.getInteractType() == UserInteractEvent.InteractType.RIGHT_CLICK) rightClick(event);
        else if (event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) leftClick(event);
    }

    private void rightClick(UserInteractEvent event) {
        Block target = event.getUser().rayTraceBlocks(200);
        if (target == null) return;
        event.getUser().setUpgradeLevel("bending", 0);

        target.getWorld().playSound(target.getLocation(), spawn.getSound(), spawn.getVolume(), spawn.getPitch());

        int maxDist = 3 * 3;

        Collection<Entity> nearby = target.getWorld().getNearbyEntities(target.getLocation(), 3, 3, 3);
        List<LivingEntity> riders = new ArrayList<>();

        for (Entity near : nearby) {
            if (!(near instanceof LivingEntity)) continue;


            User other = EntityUtils.getRepresentingUser(event.getUser(), near);
            if (other != null) {
                if (!other.isInGame()) continue;
                else if (!Objects.equals(event.getUser().getTeamIdentifier(), other.getTeamIdentifier())) {
                    if (other.getEntity() == near) other.setLastAttacker(event.getUser());
                    ((LivingEntity) near).damage(10, event.getUser().getEntity());
                }
            }

            riders.add((LivingEntity) near);
        }

        List<FallingBlock> fallingBlockList = new ArrayList<>();

        for (int y = -3; y <= 3; ++y) {
            int ys = y * y;
            for (int x = -3; x <= 3; ++x) {
                int xs = x * x;
                for (int z = -3; z <= 3; ++z) {
                    int zs = z * z;
                    if (xs + ys + zs > maxDist) continue;
                    Block block = target.getRelative(x, y, z);

                    if (block.getType() == Material.AIR || block.getType() == Material.OBSIDIAN ||
                            block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) continue;
                    if (block.isLiquid()) {
                        block.setType(Material.AIR);
                        continue;
                    }
                    BlockState oldState = block.getState();
                    block.setType(Material.AIR);

                    FallingBlock falling = block.getWorld()
                            .spawnFallingBlock(block.getLocation(), oldState.getType(), oldState.getRawData());

                    falling.setVelocity(new Vector(0, 1.5, 0));
                    fallingBlockList.add(falling);
                }
            }
        }

        BentEarth bentEarth = new BentEarth(fallingBlockList, riders);
        event.getUser().setMetadata(bentEarth);

        event.setStartCooldownAfterAction(true);
    }

    private void leftClick(UserInteractEvent event) {
        BentEarth bentEarth = event.getUser().getMetadata(BentEarth.class);
        if (bentEarth == null) return;

        if (event.getUser().getUpgradeLevel("bending") >=
                (int) maxMoves.calculate(event.getUser().getUpgradeLevels())) {
            return;
        }

        Vector add = event.getUser().getLocation().getDirection();
        bentEarth.addVelocity(add.multiply(moveVelocity.calculate(event.getUser().getUpgradeLevels())));
        bentEarth.playKnockSound(event.getUser(), knockback);
        event.getUser().setUpgradeLevel("bending", event.getUser().getUpgradeLevel("bending") + 1);
    }
}
