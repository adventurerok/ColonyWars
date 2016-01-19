package com.ithinkrok.oldmccw.util;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.data.TeamColor;
import com.ithinkrok.oldmccw.data.User;
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
public class EntityUtils {


    public static User getUserFromEntity(WarsPlugin plugin, Entity entity) {

        if (!(entity instanceof Player)) {
            if (entity instanceof Projectile) {
                Projectile arrow = (Projectile) entity;

                if (!(arrow.getShooter() instanceof Player)) return null;
                return plugin.getUser((Player) arrow.getShooter());
            } else if (entity instanceof Tameable) {
                Tameable tameable = (Tameable) entity;
                if (tameable.getOwner() == null || !(tameable.getOwner() instanceof Player)) return null;
                return plugin.getUser((Player) tameable.getOwner());
            } else {
                List<MetadataValue> values = entity.getMetadata("striker");
                if (values == null || values.isEmpty()) return null;

                User user = plugin.getUser((UUID) values.get(0).value());
                if (user == null) return null;
                return user;
            }
        } else {
            return plugin.getUser((Player) entity);
        }
    }

    public static TeamColor getTeamFromEntity(WarsPlugin plugin, Entity entity) {
        User user = getUserFromEntity(plugin, entity);
        if(user != null) return user.getTeamColor();

        if(!entity.hasMetadata("team")) return null;

        return (TeamColor) entity.getMetadata("team").get(0).value();
    }
}
