package com.ithinkrok.oldmccw.data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by paul on 28/11/15.
 *
 * Data structure for user stats
 */
@Entity
@Table(name = "mccw_stats")
public class UserCategoryStats {

    @Id private int id;

    @Column private String playerUUID;

    /**
     * The category of the stats. Can be "total", a player class name, or a team name.
     */
    @Column private String category;
    @Column private String name;
    @Column private int gameWins;
    @Column private int gameLosses;
    @Column private int kills;
    @Column private int deaths;
    @Column private int totalMoney;
    @Column private int score;
    @Column private int games;

    @Version
    private Date version;

    public int getId() {
        return id;
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

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
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
