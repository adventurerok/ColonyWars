package com.ithinkrok.cw.metadata;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.database.DatabaseTask;
import com.ithinkrok.minigames.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.metadata.UserMetadata;
import com.ithinkrok.minigames.team.Team;
import com.ithinkrok.minigames.team.TeamIdentifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.UUID;

/**
 * Created by paul on 17/01/16.
 */
public class StatsHolder extends UserMetadata implements Messagable {

    private GameGroup gameGroup;
    private UserCategoryStats statsChanges = new UserCategoryStats();
    private String playerName;
    private User user;
    private UUID uniqueId;

    private int winScoreModifier;
    private int lossScoreModifier;
    private int killScoreModifier;
    private int deathScoreModifier;

    private String lastKit, lastTeam;

    public String getLastKit() {
        return lastKit;
    }

    public String getLastTeam() {
        return lastTeam;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setLastKit(String lastKit) {
        this.lastKit = lastKit;
    }

    public void setLastTeam(String lastTeam) {
        this.lastTeam = lastTeam;
    }

    public StatsHolder(User user) {
        this.user = user;

        gameGroup = user.getGameGroup();
        playerName = user.getName();
        uniqueId = user.getUuid();

        ConfigurationSection config = user.getSharedObject("stats_holder_metadata");
        if (config == null) config = new MemoryConfiguration();

        winScoreModifier = config.getInt("win_score_modifier", 50);
        lossScoreModifier = config.getInt("loss_score_modifier", -10);
        killScoreModifier = config.getInt("kill_score_modifier", 10);
        deathScoreModifier = config.getInt("death_score_modifier", -5);
    }

    private static Query<UserCategoryStats> query(EbeanServer database, UUID playerUUID, String category) {
        Query<UserCategoryStats> query = database.find(UserCategoryStats.class);

        query.where().eq("player_uuid", playerUUID.toString()).eq("category", category);

        return query;
    }

    public static void getUserCategoryStats(DatabaseTaskRunner taskRunner, UUID uuid, String category, StatsTask task) {
        taskRunner.doDatabaseTask(new StatsGet(uuid, category, task));
    }

    public static StatsHolder getOrCreate(User user) {
        StatsHolder statsHolder = user.getMetadata(StatsHolder.class);

        if(statsHolder == null) {
            if(!user.getGameGroup().getCurrentGameState().getName().equals("lobby")) {
                for (TeamIdentifier identifier : user.getGameGroup().getTeamIdentifiers()) {
                    Team team = user.getGameGroup().getTeam(identifier);
                    CWTeamStats teamStats = CWTeamStats.getOrCreate(team);

                    StatsHolder found = teamStats.getStatsHolder(user);
                    if (found == null) continue;
                    statsHolder = found;
                    break;
                }
            }

            if(statsHolder == null) statsHolder = new StatsHolder(user);
            else statsHolder.setUser(user);
            user.setMetadata(statsHolder);
        }

        return statsHolder;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public void sendLocale(String locale, Object... args) {
        if (user != null) user.sendLocale(locale, args);
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
                stats -> gameGroup.doDatabaseTask(new StatsUpdater(stats, changes))));

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
        public void run(EbeanServer database) {
            Query<UserCategoryStats> query = query(database, uuid, category);

            UserCategoryStats result = query.findUnique();
            if (result != null) task.run(result);
        }
    }

    private static class StatsGetOrCreate extends StatsGet {

        public StatsGetOrCreate(UUID uuid, String category, StatsTask task) {
            super(uuid, category, task);
        }

        @Override
        public void run(EbeanServer database) {
            Query<UserCategoryStats> query = query(database, uuid, category);

            UserCategoryStats result = query.findUnique();
            if (result != null) task.run(result);

            result = database.createEntityBean(UserCategoryStats.class);

            result.setPlayerUUID(uuid);
            result.setCategory(category);

            database.save(result);

            task.run(query.findUnique());
        }
    }

    private class StatsUpdater implements DatabaseTask {

        private UserCategoryStats target;
        private UserCategoryStats changes;

        public StatsUpdater(UserCategoryStats target, UserCategoryStats changes) {
            this.target = target;
            this.changes = changes;
        }

        @Override
        public void run(EbeanServer database) {
            target.setName(playerName);
            target.setScore(target.getScore() + changes.getScore());
            target.setKills(target.getKills() + changes.getKills());
            target.setDeaths(target.getDeaths() + changes.getDeaths());
            target.setGames(target.getGames() + changes.getGames());
            target.setGameWins(target.getGameWins() + changes.getGameWins());
            target.setGameLosses(target.getGameLosses() + changes.getGameLosses());
            target.setTotalMoney(target.getTotalMoney() + changes.getTotalMoney());

            database.save(database);
        }
    }
}
