package com.ithinkrok.cw.database;

import com.ithinkrok.minigames.api.database.DatabaseAccessor;
import com.ithinkrok.minigames.api.database.DatabaseObject;

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

    private String playerUUID;

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


    public UserCategoryStats(String playerUUID, String category) {
        this.playerUUID = playerUUID;
        this.category = category;
    }

    public static UserCategoryStats get(DatabaseAccessor accessor, UUID uuid, String category, boolean orCreate)
            throws SQLException {
        try (Connection connection = accessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM mccw_stats WHERE player_uuid=? AND category=?;"
             )) {

            statement.setString(1, uuid.toString());
            statement.setString(2, category);

            try(ResultSet results = statement.executeQuery()) {
                if(results.next()) {
                    return load(results);
                } else if(orCreate) {
                    return new UserCategoryStats(uuid.toString(), category);
                } else {
                    return null;
                }
            }

        }
    }

    /**
     *
     * @param sql Appended to "SELECT * from MCCW_STATS"
     */
    public static List<UserCategoryStats> query(DatabaseAccessor accessor, String sql, Object... params) throws
            SQLException{
        try(Connection connection = accessor.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM mccw_stats " + sql + ";"
            )) {

            for(int index = 0; index < params.length; ++index) {
                statement.setObject(index + 1, params[index]);
            }

            try(ResultSet results = statement.executeQuery()) {
                List<UserCategoryStats> output = new ArrayList<>();

                while(results.next()) {
                    output.add(load(results));
                }

                return output;
            }
        }
    }

    private static UserCategoryStats load(ResultSet results) throws SQLException{
        UserCategoryStats stats = new UserCategoryStats(results.getString("player_uuid"),
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID.toString();
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
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

    public void setGameLosses(int gameLosses) {
        this.gameLosses = gameLosses;
    }

    public int getGameWins() {
        return gameWins;
    }

    public void setGameWins(int gameWins) {
        this.gameWins = gameWins;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    @Override
    public void save(DatabaseAccessor accessor) throws SQLException {
        try (Connection connection = accessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO mccw_stats " +
                             "(player_uuid, category, game_wins, game_losses, kills, deaths, total_money, score, games, " +
                             "name, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +
                             "game_wins=?, game_losses=?, kills=?, deaths=?, total_money=?, score=?, games=?, version=NOW" +
                             "();")) {


            statement.setString(1, playerUUID);
            statement.setString(2, category);

            for (int n = 0; n <= 1; ++n) {
                statement.setInt(3 + 8 * n, gameWins);
                statement.setInt(4 + 8 * n, gameLosses);
                statement.setInt(5 + 8 * n, kills);
                statement.setInt(6 + 8 * n, deaths);
                statement.setInt(7 + 8 * n, totalMoney);
                statement.setInt(8 + 8 * n, score);
                statement.setInt(9 + 8 * n, games);
                statement.setString(10 + 8 * n, name);
            }

            statement.executeUpdate();

            statement.close();
        }
    }
}
