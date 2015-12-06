package com.ithinkrok.mccw.util;

import com.avaje.ebean.Query;
import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.UserCategoryStats;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by paul on 28/11/15.
 *
 * Handles saving player stats to database
 */
public class Persistence extends Thread{

    private final WarsPlugin plugin;
    private final Object writeLock = new Object();

    private boolean stop = false;
    private ConcurrentLinkedQueue<Runnable> threadTasks = new ConcurrentLinkedQueue<>();


    public Persistence(WarsPlugin plugin) {
        this.plugin = plugin;
        checkDDL();

        start();
    }

    public void onPluginDisabled() {
        stop = true;

        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(!stop) {
            Runnable runnable;

            while((runnable = threadTasks.poll()) != null) {
                runnable.run();
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                plugin.getLogger().warning("Persistence thread interrupted");
                e.printStackTrace();
                return;
            }
        }
    }

    public interface PersistenceTask {
        void run(UserCategoryStats stats);
    }

    public interface ScoresTask {
        void run(List<UserCategoryStats> statsByScore);
    }

    private void checkDDL(){
        try{
            plugin.getDatabase().find(UserCategoryStats.class).findRowCount();
        } catch(PersistenceException e){
            plugin.installDDL();
        }
    }

    public void getUserCategoryStats(UUID playerUUID, String category, PersistenceTask task){
        threadTasks.add(() -> task.run(_getUserCategoryStats(playerUUID, category)));
    }

    private UserCategoryStats _getUserCategoryStats(UUID playerUUID, String category){
        return query(playerUUID, category).findUnique();
    }

    public void getUserCategoryStatsByScore(String category, int max, ScoresTask task){
        threadTasks.add(() -> task.run(_getUserCategoryStatsByScore(category, max)));
    }

    private List<UserCategoryStats> _getUserCategoryStatsByScore(String category, int max) {
        Query<UserCategoryStats> query = plugin.getDatabase().find(UserCategoryStats.class);

        query.where().eq("category", category);

        query.orderBy("score desc");

        query.setMaxRows(max);

        return query.findList();
    }

    public void getOrCreateUserCategoryStats(UUID playerUUID, String category, PersistenceTask task) {
        threadTasks.add(() -> task.run(_getOrCreateUserCategoryStats(playerUUID, category)));
    }

    private UserCategoryStats _getOrCreateUserCategoryStats(UUID playerUUID, String category){
        Query<UserCategoryStats> query = query(playerUUID, category);

        synchronized (writeLock) {
            UserCategoryStats result = query.findUnique();
            if (result != null) return result;

            result = plugin.getDatabase().createEntityBean(UserCategoryStats.class);

            result.setPlayerUUID(playerUUID);
            result.setCategory(category);

            plugin.getDatabase().save(result);

            //TODO prevent two being created at once

            return query.findUnique();
        }
    }

    public void saveUserCategoryStats(UserCategoryStats stats) {
        if(Thread.currentThread() == this) _saveUserCategoryStats(stats);
        else threadTasks.add(() -> _saveUserCategoryStats(stats));
    }

    private void _saveUserCategoryStats(UserCategoryStats stats){
        try {
            plugin.getDatabase().save(stats);
        } catch(OptimisticLockException e){
            plugin.getLogger().warning("Failed to save stats");
            e.printStackTrace();
        }
    }

    private Query<UserCategoryStats> query(UUID playerUUID, String category){
        Query<UserCategoryStats> query = plugin.getDatabase().find(UserCategoryStats.class);

        query.where().eq("player_uuid", playerUUID.toString()).eq("category", category);

        return query;
    }
}
