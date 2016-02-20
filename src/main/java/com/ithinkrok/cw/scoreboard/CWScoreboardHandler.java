package com.ithinkrok.cw.scoreboard;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.util.config.Config;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 06/01/16.
 */
public class CWScoreboardHandler implements ScoreboardHandler {

    private final String displayName;
    private final String userBalanceDisplay;
    private final String teamBalanceDisplay;
    private final String buildingNowCountDisplay;
    private final String revivalRateDisplay;

    private final List<String> oldBuildingNows = new ArrayList<>();

    private int oldMoney = 0;

    public CWScoreboardHandler(User user) {
        Config config = user.getSharedObject("colony_wars_scoreboard");

        String displayNameLocale = config.getString("title");
        displayName = user.getLanguageLookup().getLocale(displayNameLocale);

        String userBalanceLocale = config.getString("user_balance");
        userBalanceDisplay = user.getLanguageLookup().getLocale(userBalanceLocale);

        String teamBalanceLocale = config.getString("team_balance");
        teamBalanceDisplay = user.getLanguageLookup().getLocale(teamBalanceLocale);

        String buildingNowCountLocale = config.getString("building_now_count");
        buildingNowCountDisplay = user.getLanguageLookup().getLocale(buildingNowCountLocale);

        String revivalRateLocale = config.getString("revival_rate");
        revivalRateDisplay = user.getLanguageLookup().getLocale(revivalRateLocale);
    }

    @Override
    public void updateScoreboard(User user, ScoreboardDisplay scoreboard) {
        if(!scoreboard.isDisplaying()) scoreboard.resetAndDisplay();
        if(user.getTeam() == null) return;

        Team team = user.getTeam();
        CWTeamStats buildingStats = CWTeamStats.getOrCreate(team);

        int userMoney = Money.getOrCreate(user).getMoney();
        if(userMoney > oldMoney) {

            //A dirty hack that relies on updateScoreboard() being called every time the user's money amount changes
            StatsHolder statsHolder = StatsHolder.getOrCreate(user);
            statsHolder.addTotalMoney(userMoney - oldMoney);
        }
        oldMoney = userMoney;

        scoreboard.setScore(userBalanceDisplay, userMoney);
        scoreboard.setScore(teamBalanceDisplay, Money.getOrCreate(team).getMoney());
        scoreboard.setScore(buildingNowCountDisplay, buildingStats.getTotalBuildingNowCount());

        Map<String, Integer> buildingNow = buildingStats.getBuildingNowCounts();

        for(String old : oldBuildingNows) {
            if(buildingNow.containsKey(old)) continue;
            scoreboard.removeScore(ChatColor.GREEN + old + ":");
        }

        oldBuildingNows.clear();

        for(Map.Entry<String, Integer> entry : buildingNow.entrySet()) {
            scoreboard.setScore(ChatColor.GREEN + entry.getKey() + ":", entry.getValue());
            oldBuildingNows.add(entry.getKey());
        }

        scoreboard.setScore(revivalRateDisplay, buildingStats.getRespawnChance());
    }

    @Override
    public void setupScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setDisplayName(displayName);
        scoreboard.resetAndDisplay();
    }
}
