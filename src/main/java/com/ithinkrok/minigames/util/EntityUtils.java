package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.user.UserResolver;
import org.bukkit.entity.*;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 03/01/16.
 */
public class EntityUtils {

    /**
     *
     * Resolves the User being represented by the entity. E.g. an arrow would be representing the User that shot the
     * arrow, or a Wolf would be representing it's owner.
     *
     * @param resolver The resolver to resolve UUIDs to Users
     * @param entity The entity to resolve the User from
     * @param <U> The type of the User class
     * @return The User that is represented by the entity, or null if there is none
     */
    public static <U extends User> U getRepresentingUser(UserResolver resolver, Entity entity) {
        if (entity instanceof Player) return getUserFromPlayer(resolver, (Player) entity);

        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;

            if (projectile.getShooter() instanceof Player) {
                return getUserFromPlayer(resolver, (Player) projectile.getShooter());
            }
        }

        if(entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if(tameable.getOwner() != null && tameable.getOwner() instanceof Player) {
                return getUserFromPlayer(resolver, (Player) tameable.getOwner());
            }
        }

        List<MetadataValue> values = entity.getMetadata("rep");
        if(values == null || values.isEmpty()) return null;

        UUID uuid = (UUID) values.get(0).value();
        return resolver.getUser(uuid);
    }

    public static <U extends User> U getActualUser(UserResolver resolver, Entity entity) {
        if (entity instanceof Player) return getUserFromPlayer(resolver, (Player) entity);

        List<MetadataValue> values = entity.getMetadata("actual");
        if(values == null || values.isEmpty()) return null;

        UUID uuid = (UUID) values.get(0).value();
        return resolver.getUser(uuid);
    }

    private static <U extends User> U getUserFromPlayer(UserResolver resolver, Player player) {
        return resolver.getUser(player.getUniqueId());
    }

}
