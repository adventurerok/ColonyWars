package com.ithinkrok.cw.database;

import com.ithinkrok.minigames.api.database.DatabaseAccessor;
import com.ithinkrok.minigames.api.database.DatabaseObject;
import com.ithinkrok.util.UUIDUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 28/11/15.
 * <p>
 * Data structure for user stats
 */
public class UserCategoryStats implements DatabaseObject {

    private UUID playerUUID;

    /**
     * The category of the stats. Can be "total", a player class name, or a team name.
     */
    private String category;
    private String name;
    private int gameWins;
    private int gameLosses;
    private int kills;
    private int deaths;
    private int totalMoney;
    private int score;
    private int games;


    public UserCategoryStats(UUID playerUUID, String category) {
        this.playerUUID = playerUUID;
        this.category = category;
    }


    public static UserCategoryStats get(DatabaseAccessor accessor, UUID uuid, String category, boolean orCreate)
            throws SQLException {
        try (Connection connection = accessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT HEX(m1.uuid) AS uuid, category, game_wins, game_losses, kills, " +
                     "deaths, total_money, score, games, name " +
                     "FROM mccw_stats AS m1 " +
                     "LEFT JOIN mg_name_cache AS m2 ON m1.uuid = m2.uuid " +
                     "WHERE m1.uuid=UNHEX(REPLACE(?,'-','')) AND category=?;"
             )) {

            statement.setString(1, uuid.toString());
            statement.setString(2, category);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return load(results);
                } else if (orCreate) {
                    return new UserCategoryStats(uuid, category);
                } else {
                    return null;
                }
            }

        }
    }


    private static UserCategoryStats load(ResultSet results) throws SQLException {
        UserCategoryStats stats = new UserCategoryStats(
                UUIDUtils.fromStringWithoutDashes(results.getString("uuid")),
                results.getString("category"));

        stats.setGameWins(results.getInt("game_wins"));
        stats.setGameLosses(results.getInt("game_losses"));
        stats.setKills(results.getInt("kills"));
        stats.setDeaths(results.getInt("deaths"));
        stats.setTotalMoney(results.getInt("total_money"));
        stats.setScore(results.getInt("score"));
        stats.setGames(results.getInt("games"));
        stats.setName(results.getString("name"));

        return stats;
    }


    public void setGameWins(int gameWins) {
        this.gameWins = gameWins;
    }


    public void setGameLosses(int gameLosses) {
        this.gameLosses = gameLosses;
    }


    public void setKills(int kills) {
        this.kills = kills;
    }


    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }


    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }


    public void setScore(int score) {
        this.score = score;
    }


    public void setGames(int games) {
        this.games = games;
    }


    @Deprecated
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @param sql Appended to "SELECT * from MCCW_STATS"
     */
    public static List<UserCategoryStats> query(DatabaseAccessor accessor, String sql, Object... params) throws
            SQLException {
        try (Connection connection = accessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT HEX(m1.uuid) AS uuid, category, game_wins, game_losses, kills, " +
                     "deaths, total_money, score, games, name " +
                     "FROM mccw_stats AS m1 " +
                     "LEFT JOIN mg_name_cache AS m2 ON m1.uuid = m2.uuid " + sql + ";"
             )) {

            for (int index = 0; index < params.length; ++index) {
                statement.setObject(index + 1, params[index]);
            }

            try (ResultSet results = statement.executeQuery()) {
                List<UserCategoryStats> output = new ArrayList<>();

                while (results.next()) {
                    output.add(load(results));
                }

                return output;
            }
        }
    }


    public String getName() {
        return name;
    }


    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public int getGameLosses() {
        return gameLosses;
    }


    public int getGameWins() {
        return gameWins;
    }


    public int getKills() {
        return kills;
    }


    public int getDeaths() {
        return deaths;
    }


    public int getTotalMoney() {
        return totalMoney;
    }


    public int getScore() {
        return score;
    }


    public int getGames() {
        return games;
    }


    @Override
    public void save(DatabaseAccessor accessor) throws SQLException {
        try (Connection connection = accessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO mccw_stats " +
                     "(uuid, category, game_wins, game_losses, kills, deaths, total_money, score, games" +
                     ") VALUES (UNHEX(REPLACE(?,'-','')), ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "game_wins=?, game_losses=?, kills=?, deaths=?, total_money=?, score=?, games=?")) {


            statement.setString(1, playerUUID.toString());
            statement.setString(2, category);

            for (int n = 0; n <= 1; ++n) {
                statement.setInt(3 + 7 * n, gameWins);
                statement.setInt(4 + 7 * n, gameLosses);
                statement.setInt(5 + 7 * n, kills);
                statement.setInt(6 + 7 * n, deaths);
                statement.setInt(7 + 7 * n, totalMoney);
                statement.setInt(8 + 7 * n, score);
                statement.setInt(9 + 7 * n, games);
            }

            statement.executeUpdate();
        }
    }
}
