package com.ithinkrok.mccw.util;

import com.avaje.ebean.Query;
import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.UserCategoryStats;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.UUID;

/**
 * Created by paul on 28/11/15.
 *
 * Handles saving player stats to database
 */
public class Persistence {

    private final WarsPlugin plugin;
    private final Object writeLock = new Object();


    public Persistence(WarsPlugin plugin) {
        this.plugin = plugin;
        checkDDL();
    }

    private void checkDDL(){
        try{
            plugin.getDatabase().find(UserCategoryStats.class).findRowCount();
        } catch(PersistenceException e){
            plugin.installDDL();
        }
    }

    public UserCategoryStats getUserCategoryStats(UUID playerUUID, String category){
        return query(playerUUID, category).findUnique();
    }

    public UserCategoryStats getOrCreateUserCategoryStats(UUID playerUUID, String category){
        Query<UserCategoryStats> query = query(playerUUID, category);

        synchronized (writeLock) {
            UserCategoryStats result = query.findUnique();
            if (result != null) return result;

            result = plugin.getDatabase().createEntityBean(UserCategoryStats.class);

            result.setPlayerUUID(playerUUID);
            result.setCategory(category);

            plugin.getDatabase().save(result);

            //TODO prevent two being created at once

            return result;
        }
    }

    public void saveUserCategoryStats(UserCategoryStats stats){
        try {
            plugin.getDatabase().save(stats);
        } catch(OptimisticLockException e){
            plugin.getLogger().warning("Failed to save stats");
            e.printStackTrace();
        }
    }

    private Query<UserCategoryStats> query(UUID playerUUID, String category){
        Query<UserCategoryStats> query = plugin.getDatabase().find(UserCategoryStats.class);

        query.where().eq("playerUUID", playerUUID.toString()).eq("category", category);

        query.setMaxRows(1);

        return query;
    }
}
