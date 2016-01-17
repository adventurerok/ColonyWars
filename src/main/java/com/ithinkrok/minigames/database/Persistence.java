package com.ithinkrok.minigames.database;

import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by paul on 17/01/16.
 */
public class Persistence extends Thread {

    private final Plugin plugin;

    private boolean stop = false;

    private ConcurrentLinkedQueue<DatabaseTask> threadTasks = new ConcurrentLinkedQueue<>();

    public Persistence(Plugin plugin) {
        this.plugin = plugin;

        start();
    }

    @Override
    public void run() {
        while(!stop) {
            DatabaseTask task;

            while((task = threadTasks.poll()) != null) {
                task.run(plugin.getDatabase());
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void onPluginDisabled() {
        stop = true;

        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doTask(DatabaseTask task) {
        threadTasks.add(task);
    }
}
