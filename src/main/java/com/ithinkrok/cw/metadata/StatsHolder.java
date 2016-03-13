package com.ithinkrok.cw.metadata;

import com.avaje.ebean.Query;
import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.database.DatabaseAccessor;
import com.ithinkrok.minigames.api.database.DatabaseTask;
import com.ithinkrok.minigames.api.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.Messagable;

import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 17/01/16.
 */
public class StatsHolder extends UserMetadata implements Messagable {

    private final GameGroup gameGroup;
    private UserCategoryStats statsChanges = new UserCategoryStats();
    private final String playerName;
    private User user;
    private final UUID uniqueId;

    private final int winScoreModifier;
    private final int lossScoreModifier;
    private final int killScoreModifier;
    private final int deathScoreModifier;

    private String lastKit, lastTeam;

    public StatsHolder(User user) {
        this.user = user;

        gameGroup = user.getGameGroup();
        playerName = user.getName();
        uniqueId = user.getUuid();

        Config config = user.getSharedObjectOrEmpty("stats_holder_metadata");

        winScoreModifier = config.getInt("win_score_modifier", 50);
        lossScoreModifier = config.getInt("loss_score_modifier", -10);
        killScoreModifier = config.getInt("kill_score_modifier", 10);
        deathScoreModifier = config.getInt("death_score_modifier", -5);
    }

    private static Query<UserCategoryStats> query(DatabaseAccessor accessor, UUID playerUUID, String category) {
        Query<UserCategoryStats> query = accessor.find(UserCategoryStats.class);

        query.where().eq("player_uuid", playerUUID.toString()).eq("category", category);

        return query;
    }

    public static void getUserCategoryStats(DatabaseTaskRunner taskRunner, UUID uuid, String category, StatsTask task) {
        taskRunner.doDatabaseTask(new StatsGet(uuid, category, task));
    }

    public static void getUserCategoryStatsByScore(DatabaseTaskRunner taskRunner, String category, int max,
                                                   ScoresTask scoresTask) {
        taskRunner.doDatabaseTask(new StateListByScore(category, max, scoresTask));
    }

    public static StatsHolder getOrCreate(User user) {
        StatsHolder statsHolder = user.getMetadata(StatsHolder.class);

        if (statsHolder == null) {
            for (TeamIdentifier identifier : user.getGameGroup().getTeamIdentifiers()) {
                Team team = user.getGameGroup().getTeam(identifier);
                TeamStatsHolderGroup teamStats = TeamStatsHolderGroup.getOrCreate(team);

                StatsHolder found = teamStats.getStatsHolder(user);
                if (found == null) continue;
                statsHolder = found;
                break;
            }

            if (statsHolder == null) statsHolder = new StatsHolder(user);
            else statsHolder.setUser(user);
            user.setMetadata(statsHolder);
        }

        return statsHolder;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLastKit() {
        return lastKit;
    }

    public void setLastKit(String lastKit) {
        this.lastKit = lastKit;
    }

    public String getLastTeam() {
        return lastTeam;
    }

    public void setLastTeam(String lastTeam) {
        this.lastTeam = lastTeam;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void addTotalMoney(int amount) {
        statsChanges.setTotalMoney(statsChanges.getTotalMoney() + amount);
    }

    public UserCategoryStats getStatsChanges() {
        return statsChanges;
    }

    public void addGameWin() {
        statsChanges.setGameWins(statsChanges.getGameWins() + 1);

        addScore(winScoreModifier);
    }

    private void addScore(int amount) {
        if (amount == 0) return;

        statsChanges.setScore(statsChanges.getScore() + amount);

        if (amount > 0) sendLocale("score.gain", amount);
        else sendLocale("score.loss", -amount);
    }

    public void addGameLoss() {
        statsChanges.setGameLosses(statsChanges.getGameLosses() + 1);

        addScore(lossScoreModifier);
    }

    public void addGame() {

        statsChanges.setGames(statsChanges.getGames() + 1);
    }

    public void addKill() {

        statsChanges.setKills(statsChanges.getKills() + 1);

        addScore(killScoreModifier);
    }

    public void addDeath() {

        statsChanges.setDeaths(statsChanges.getDeaths() + 1);

        addScore(deathScoreModifier);
    }

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return false;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }

    @Override
    public void sendMessage(String message) {
        if (user != null) user.sendMessage(message);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        if (user != null) user.sendMessageNoPrefix(message);
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        if (user != null) user.sendLocale(locale, args);
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        if (user != null) user.sendLocaleNoPrefix(locale, args);
    }

    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup;
    }

    public void saveStats() {
        UserCategoryStats changes = statsChanges;
        statsChanges = new UserCategoryStats();

        gameGroup.doDatabaseTask(new StatsGetOrCreate(uniqueId, "total",
                stats -> {
                    int newScore = stats.getScore() + changes.getScore();
                    gameGroup.getDatabase().setUserScore(uniqueId, playerName, gameGroup.getType(), newScore);

                    gameGroup.doDatabaseTask(new StatsUpdater(stats, changes));
                }));

        if (lastKit != null) {
            gameGroup.doDatabaseTask(new StatsGetOrCreate(uniqueId, lastKit,
                    stats -> gameGroup.doDatabaseTask(new StatsUpdater(stats, changes))));
        }

        if (lastTeam != null) {
            gameGroup.doDatabaseTask(new StatsGetOrCreate(uniqueId, lastTeam,
                    stats -> gameGroup.doDatabaseTask(new StatsUpdater(stats, changes))));
        }
    }

    public interface StatsTask {
        void run(UserCategoryStats stats);
    }

    public interface ScoresTask {
        void run(List<UserCategoryStats> statsByScore);
    }

    private static class StateListByScore implements DatabaseTask {

        final String category;
        final int max;
        final ScoresTask task;

        public StateListByScore(String category, int max, ScoresTask task) {
            this.category = category;
            this.max = max;
            this.task = task;
        }

        @Override
        public void run(DatabaseAccessor accessor) {
            Query<UserCategoryStats> query =
                    accessor.find(UserCategoryStats.class);

            query.where().eq("category", category);
            query.orderBy("score desc");

            query.setMaxRows(max);

            task.run(query.findList());
        }
    }

    private static class StatsGet implements DatabaseTask {

        UUID uuid;
        String category;
        StatsTask task;

        public StatsGet(UUID uuid, String category, StatsTask task) {
            this.uuid = uuid;
            this.category = category;
            this.task = task;
        }

        @Override
        public void run(DatabaseAccessor accessor) {
            Query<UserCategoryStats> query = query(accessor, uuid, category);

            UserCategoryStats result = query.findUnique();
            task.run(result);
        }
    }

    private static class StatsGetOrCreate extends StatsGet {

        public StatsGetOrCreate(UUID uuid, String category, StatsTask task) {
            super(uuid, category, task);
        }

        @Override
        public void run(DatabaseAccessor accessor) {
            Query<UserCategoryStats> query = query(accessor, uuid, category);

            UserCategoryStats result = query.findUnique();
            if (result != null) {
                task.run(result);
                return;
            }

            result = accessor.createEntityBean(UserCategoryStats.class);

            result.setPlayerUUID(uuid);
            result.setCategory(category);

            accessor.save(result);

            task.run(query.findUnique());
        }
    }

    private class StatsUpdater implements DatabaseTask {

        private final UserCategoryStats target;
        private final UserCategoryStats changes;

        public StatsUpdater(UserCategoryStats target, UserCategoryStats changes) {
            this.target = target;
            this.changes = changes;
        }

        @Override
        public void run(DatabaseAccessor accessor) {
            target.setName(playerName);
            target.setScore(target.getScore() + changes.getScore());
            target.setKills(target.getKills() + changes.getKills());
            target.setDeaths(target.getDeaths() + changes.getDeaths());
            target.setGames(target.getGames() + changes.getGames());
            target.setGameWins(target.getGameWins() + changes.getGameWins());
            target.setGameLosses(target.getGameLosses() + changes.getGameLosses());
            target.setTotalMoney(target.getTotalMoney() + changes.getTotalMoney());

            accessor.save(target);
        }
    }
}
