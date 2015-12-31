package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.CommandUtils;
import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import com.ithinkrok.oldmccw.data.User;
import com.ithinkrok.oldmccw.data.UserCategoryStats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.text.DecimalFormat;

/**
 * Created by paul on 11/12/15.
 * <p>
 * Handles the /stats command
 */
public class StatsExecutor implements WarsCommandExecutor {

    private static DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if (!sender.getPlugin().hasPersistence()) {
            sender.sendLocale("commands.stats.disabled");
            return true;
        }

        if (!CommandUtils.checkUser(sender)) return true;

        User user = (User) sender;

        String category = "total";
        if (args.length > 0) category = args[0];

        final String finalCategory = category;
        user.getStats(category, stats -> Bukkit.getScheduler().scheduleSyncDelayedTask(user.getPlugin(), () -> {
            if (stats == null) {
                user.sendLocale("commands.stats.none", finalCategory);
                return;
            }

            UserCategoryStats add;
            if (finalCategory.equals("total") || finalCategory.equals(user.getLastPlayerClass().getName()) ||
                    finalCategory.equals(user.getLastTeamColor().getName())) {
                add = user.getStatsHolder().getStatsChanges();
            } else add = new UserCategoryStats();

            user.sendLocale("commands.stats.category", finalCategory);


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

            user.sendLocaleDirect("commands.stats.kills", kills);
            user.sendLocaleDirect("commands.stats.deaths", deaths);
            user.sendLocaleDirect("commands.stats.kd", kdText);
            user.sendLocaleDirect("commands.stats.wins", gameWins);
            user.sendLocaleDirect("commands.stats.losses", gameLosses);
            user.sendLocaleDirect("commands.stats.games", games);
            user.sendLocaleDirect("commands.stats.totalmoney", totalMoney);
            user.sendLocaleDirect("commands.stats.score", score);
        }));

        return true;
    }
}
