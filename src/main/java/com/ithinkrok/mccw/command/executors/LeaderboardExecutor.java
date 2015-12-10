package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.util.UUID;

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

        String category = "total";
        if (args.length > 0) category = args[0];

        sender.sendLocale("commands.leaderboard.category", category);

        sender.getPlugin().getPersistence().getUserCategoryStatsByScore(category, 10, statsByScore -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(sender.getPlugin(), () -> {
                for (int index = 0; index < statsByScore.size(); ++index) {
                    sender.sendLocaleDirect("commands.leaderboard.listing", index + 1,
                            Bukkit.getOfflinePlayer(UUID.fromString(statsByScore.get(index).getPlayerUUID())).getName(),
                            statsByScore.get(index).getScore());
                }
            });
        });

        return true;
    }
}
