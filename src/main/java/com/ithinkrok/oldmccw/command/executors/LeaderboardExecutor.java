package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

/**
 * Created by paul on 10/12/15.
 * <p>
 * Handles the /leaderboard command
 */
public class LeaderboardExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if (!sender.getPlugin().hasPersistence()) {
            sender.sendLocale("commands.stats.disabled");
            return true;
        }



        int count = 10;
        if(args.length > 0) {
            try{
                count = Math.min(Math.max(Integer.parseInt(args[0]), 3), 100);
            } catch (NumberFormatException e) {
                sender.sendLocale("commands.leaderboard.bad-count");
                return true;
            }
        }

        String category = "total";
        if (args.length > 1) category = args[1];

        sender.sendLocale("commands.leaderboard.category", category);

        sender.getPlugin().getPersistence().getUserCategoryStatsByScore(category, count, statsByScore -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(sender.getPlugin(), () -> {
                for (int index = 0; index < statsByScore.size(); ++index) {
                    sender.sendLocaleDirect("commands.leaderboard.listing", index + 1,
                            statsByScore.get(index).getName(),
                            statsByScore.get(index).getScore());
                }
            });
        });

        return true;
    }
}
