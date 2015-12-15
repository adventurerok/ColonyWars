package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.command.CommandUtils;
import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.util.Vector;

/**
 * Created by paul on 10/12/15.
 *
 * Handles the /fix command
 */
public class FixExecutor implements WarsCommandExecutor {
    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if(!CommandUtils.checkUser(sender)) return true;

        if (!sender.getPlugin().isInGame()) return true;

        User user = (User) sender;

        if(!user.isOnGround()) return true;

        if (!user.startCoolDown("fix", 1, sender.getPlugin().getLocale("cooldowns.fix.finished"))) return true;
        if (user.getPlayer().getVelocity().lengthSquared() > 0.3d) return true;

        Block base = user.getPlayer().getLocation().clone().add(0, 1, 0).getBlock();
        Block block;

        for (int radius = 0; radius < 5; ++radius) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    int state = 0;
                    for (int y = radius + 1; y >= -radius - 2; --y) {
                        if (Math.abs(x) < radius && Math.abs(y) + 3 < radius && Math.abs(z) < radius) continue;
                        block = base.getRelative(x, y, z);

                        boolean air = block.getType().isTransparent() || block.isLiquid();
                        if (!air && state < 2) {
                            state = 0;
                            continue;
                        } else if (air && state == 2) continue;
                        else if (state < 3) {
                            ++state;
                            continue;
                        }

                        user.teleport(block.getLocation().clone().add(0.5, 2.0, 0.5));
                        user.getPlayer().setVelocity(new Vector(0, -1, 0));
                        return true;
                    }
                }
            }
        }

        user.teleport(sender.getPlugin().getMapSpawn(user.getTeamColor()));
        user.sendLocale("commands.fix.failed");

        return true;
    }
}
