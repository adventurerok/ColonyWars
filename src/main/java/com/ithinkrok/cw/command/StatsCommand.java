package com.ithinkrok.cw.command;

import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;

import java.text.DecimalFormat;

/**
 * Created by paul on 17/01/16.
 */
public class StatsCommand implements GameCommandExecutor {

    private static DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireUser(sender)) return true;

        String category = command.getStringArg(0, "total");

        StatsHolder.getUserCategoryStats(command.getGameGroup(), command.getUser().getUuid(), category, stats -> {
            if(stats == null) {
                sender.sendLocale("command.stats.none", category);
                return;
            }

            UserCategoryStats add;
            StatsHolder statsHolder = StatsHolder.getOrCreate(command.getUser());
            if(category.equals("total") || category.equals(statsHolder.getLastKit()) || category.equals(statsHolder
                    .getLastTeam())) {
                add = statsHolder.getStatsChanges();
            } else add = new UserCategoryStats();
            
            sender.sendLocale("commands.stats.category", category);

            int kills = stats.getKills() + add.getKills();
            int deaths = stats.getDeaths() + add.getDeaths();

            double kd = kills;
            if (deaths != 0) kd /= (double) deaths;

            int gameWins = stats.getGameWins() + add.getGameWins();
            int gameLosses = stats.getGameLosses() + add.getGameLosses();
            int games = stats.getGames() + add.getGames();
            int totalMoney = stats.getTotalMoney() + add.getTotalMoney();
            int score = stats.getScore() + add.getScore();


            String kdText = twoDecimalPlaces.format(kd);

            sender.sendLocaleNoPrefix("commands.stats.kills", kills);
            sender.sendLocaleNoPrefix("commands.stats.deaths", deaths);
            sender.sendLocaleNoPrefix("commands.stats.kd", kdText);
            sender.sendLocaleNoPrefix("commands.stats.wins", gameWins);
            sender.sendLocaleNoPrefix("commands.stats.losses", gameLosses);
            sender.sendLocaleNoPrefix("commands.stats.games", games);
            sender.sendLocaleNoPrefix("commands.stats.totalmoney", totalMoney);
            sender.sendLocaleNoPrefix("commands.stats.score", score);
        });
        
        return true;
    }
}
