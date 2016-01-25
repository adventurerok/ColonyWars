package com.ithinkrok.cw.command;

import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.event.CommandEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 22/01/16.
 */
public class LeaderboardCommand implements Listener {

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        Command command = event.getCommand();

        if (!command.requireGameGroup(sender)) return;

        int count = command.getIntArg(0, 10);
        if (count < 1) count = 1;
        else if (count > 100) count = 100;

        String category = command.getStringArg(1, "total");

        sender.sendLocale("command.leaderboard.category", category);

        StatsHolder.getUserCategoryStatsByScore(command.getGameGroup(), category, count,
                statsByScore -> command.getGameGroup().doInFuture((task) -> {
                    for (int index = 0; index < statsByScore.size(); ++index) {
                        sender.sendLocaleNoPrefix("command.leaderboard.listing", index + 1,
                                statsByScore.get(index).getName(), statsByScore.get(index).getScore());
                    }
                }));
    }
}
