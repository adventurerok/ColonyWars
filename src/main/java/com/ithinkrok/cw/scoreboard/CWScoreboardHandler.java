package com.ithinkrok.cw.scoreboard;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.user.scoreboard.ScoreboardHandler;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 06/01/16.
 */
public class CWScoreboardHandler implements ScoreboardHandler {

    private String displayName;
    private String userBalanceDisplay;
    private String teamBalanceDisplay;

    public CWScoreboardHandler(User user) {
        ConfigurationSection config = user.getSharedObject("colony_wars_scoreboard");

        String displayNameLocale = config.getString("title");
        displayName = user.getLanguageLookup().getLocale(displayNameLocale);

        String userBalanceLocale = config.getString("user_balance");
        userBalanceDisplay = user.getLanguageLookup().getLocale(userBalanceLocale);

        String teamBalanceLocale = config.getString("team_balance");
        teamBalanceDisplay = user.getLanguageLookup().getLocale(teamBalanceLocale);
    }

    @Override
    public void updateScoreboard(User user, ScoreboardDisplay scoreboard) {
        if(!scoreboard.isDisplaying()) scoreboard.resetAndDisplay();

        scoreboard.setScore(userBalanceDisplay, Money.getOrCreate(user).getMoney());
    }

    @Override
    public void setupScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setDisplayName(displayName);
        scoreboard.resetAndDisplay();
    }
}
