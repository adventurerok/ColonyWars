package com.ithinkrok.oldmccw.data;

import com.ithinkrok.oldmccw.WarsPlugin;
import org.bukkit.entity.Player;

/**
 * Created by paul on 22/12/15.
 */
public class ZombieStats {

    private boolean allowFlight;
    private float flySpeed;
    private float exp;
    private int level;

    public ZombieStats(Player loadFrom) {
        allowFlight = loadFrom.getAllowFlight();
        flySpeed = loadFrom.getFlySpeed();

        exp = loadFrom.getExp();
        level = loadFrom.getLevel();
    }

    public void applyTo(WarsPlugin plugin, Player player) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.setAllowFlight(allowFlight);
            player.setFlySpeed(flySpeed);

            player.setExp(exp);
            player.setLevel(level);
        }, 2);
    }
}
