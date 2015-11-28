package com.ithinkrok.mccw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by paul on 28/11/15.
 *
 * Data structure for user stats
 */
@Entity
@Table(name = "mccw_stats")
public class UserCategoryStats {

    @Id private long id;

    @Column private UUID playerUUID;

    /**
     * The category of the stats. Can be "total", a player class name, or a team name.
     */
    @Column private String category;
    @Column private int gameWins;
    @Column private int gameLosses;
    @Column private int kills;
    @Column private int deaths;
    @Column private int totalMoney;
    @Column private int score;
    @Column private int games;

    public long getId() {
        return id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getCategory() {
        return category;
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

    public void setCategory(String category) {
        this.category = category;
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
}
