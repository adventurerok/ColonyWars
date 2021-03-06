package com.ithinkrok.cw.command;

import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 22/01/16.
 */
public class LeaderboardCommand implements CustomListener {

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

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
