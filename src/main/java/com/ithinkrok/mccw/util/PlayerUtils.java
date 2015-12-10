package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 10/12/15.
 *
 * Utilities for players
 */
public class PlayerUtils {


    public static Player getPlayerFromEntity(WarsPlugin plugin, Entity entity) {

        if (!(entity instanceof Player)) {
            if (entity instanceof Projectile) {
                Projectile arrow = (Projectile) entity;

                if (!(arrow.getShooter() instanceof Player)) return null;
                return (Player) arrow.getShooter();
            } else if (entity instanceof Tameable) {
                Tameable tameable = (Tameable) entity;
                if (tameable.getOwner() == null || !(tameable.getOwner() instanceof Player)) return null;
                return (Player) tameable.getOwner();
            } else {
                List<MetadataValue> values = entity.getMetadata("striker");
                if (values == null || values.isEmpty()) return null;

                User user = plugin.getUser((UUID) values.get(0).value());
                if (user == null) return null;
                return user.getPlayer();
            }
        } else {
            return (Player) entity;
        }
    }

    public static User getUserFromEntity(WarsPlugin plugin, Entity entity) {
        Player player = getPlayerFromEntity(plugin, entity);
        return player == null ? null : plugin.getUser(player);
    }
}
