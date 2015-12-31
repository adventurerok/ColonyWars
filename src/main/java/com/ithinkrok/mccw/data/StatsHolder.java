package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.minigames.TeamColor;
import com.ithinkrok.mccw.util.Persistence;

import java.util.UUID;

/**
 * Created by paul on 11/12/15.
 * <p>
 * Holds stats changes for a user
 */
public class StatsHolder implements WarsCommandSender {

    private WarsPlugin plugin;
    private UserCategoryStats statsChanges = new UserCategoryStats();
    private String playerName;
    private User user;
    private UUID uniqueId;
    private PlayerClass lastPlayerClass;
    private TeamColor lastTeamColor;

    public StatsHolder(User user) {
        this.user = user;

        plugin = user.getPlugin();
        playerName = user.getName();
        uniqueId = user.getUniqueId();
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void sendMessageDirect(String message) {
        if (user != null) user.sendMessageDirect(message);
    }

    @Override
    public void sendLocaleDirect(String locale, Object... args) {
        if (user != null) user.sendLocaleDirect(locale, args);
    }

    @Override
    public void sendMessage(String message) {
        if (user != null) user.sendMessage(message);
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public WarsPlugin getPlugin() {
        return plugin;
    }

    public void addTotalMoney(int amount) {
        if (!plugin.hasPersistence()) return;

        statsChanges.setTotalMoney(statsChanges.getTotalMoney() + amount);
    }

    public UserCategoryStats getStatsChanges() {
        return statsChanges;
    }

    public TeamColor getLastTeamColor() {
        return lastTeamColor;
    }

    public void setLastTeamColor(TeamColor lastTeamColor) {
        this.lastTeamColor = lastTeamColor;
    }

    public PlayerClass getLastPlayerClass() {
        return lastPlayerClass;
    }

    public void setLastPlayerClass(PlayerClass lastPlayerClass) {
        this.lastPlayerClass = lastPlayerClass;
    }

    public void saveStats() {
        if (!plugin.hasPersistence()) return;

        StatsUpdater statsUpdater = new StatsUpdater(statsChanges);
        statsChanges = new UserCategoryStats();

        plugin.getOrCreateUserCategoryStats(uniqueId, "total", statsUpdater);
        if (lastPlayerClass != null)
            plugin.getOrCreateUserCategoryStats(uniqueId, lastPlayerClass.getName(), statsUpdater);
        if (lastTeamColor != null) plugin.getOrCreateUserCategoryStats(uniqueId, lastTeamColor.getName(), statsUpdater);

    }

    public void addGameWin() {
        statsChanges.setGameWins(statsChanges.getGameWins() + 1);

        addScore(plugin.getWarsConfig().getWinScoreModifier());
    }

    private void addScore(int amount) {
        if (amount == 0) return;

        statsChanges.setScore(statsChanges.getScore() + amount);

        if (amount > 0) sendLocale("score.gain", amount);
        else sendLocale("score.loss", -amount);
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        if (user != null) user.sendLocale(locale, args);
    }

    public void addGameLoss() {
        statsChanges.setGameLosses(statsChanges.getGameLosses() + 1);

        addScore(plugin.getWarsConfig().getLossScoreModifier());
    }

    public void addGame() {
        if (!plugin.hasPersistence()) return;

        statsChanges.setGames(statsChanges.getGames() + 1);
    }

    public void addKill() {
        if (!plugin.hasPersistence()) return;

        statsChanges.setKills(statsChanges.getKills() + 1);

        addScore(plugin.getWarsConfig().getKillScoreModifier());
    }

    public void addDeath() {
        if (!plugin.hasPersistence()) return;

        statsChanges.setDeaths(statsChanges.getDeaths() + 1);

        addScore(plugin.getWarsConfig().getDeathScoreModifier());
    }

    private class StatsUpdater implements Persistence.PersistenceTask {
        private UserCategoryStats changes;

        public StatsUpdater(UserCategoryStats changes) {
            this.changes = changes;
        }

        @Override
        public void run(UserCategoryStats target) {
            target.setName(playerName);
            target.setScore(target.getScore() + changes.getScore());
            target.setKills(target.getKills() + changes.getKills());
            target.setDeaths(target.getDeaths() + changes.getDeaths());
            target.setGames(target.getGames() + changes.getGames());
            target.setGameWins(target.getGameWins() + changes.getGameWins());
            target.setGameLosses(target.getGameLosses() + changes.getGameLosses());
            target.setTotalMoney(target.getTotalMoney() + changes.getTotalMoney());

            plugin.saveUserCategoryStats(target);
        }
    }
}
