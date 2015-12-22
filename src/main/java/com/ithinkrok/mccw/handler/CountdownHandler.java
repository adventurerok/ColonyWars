package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.CountdownType;
import com.ithinkrok.mccw.enumeration.GameState;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Handles countdowns
 */
public class CountdownHandler {

    private int countDownTask;
    private int countDown;
    private CountdownType countdownType;

    private WarsPlugin plugin;

    public CountdownType getCountdownType() {
        return countdownType;
    }

    public CountdownHandler(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isCountingDown(){
        return countDownTask != 0;
    }

    public int getCountDownTime() {
        return countDown;
    }

    public void setCountDownTime(int countDownTime){
        this.countDown = countDownTime;
    }

    public void startCountdown(int countdownFrom, CountdownType countdownType, Runnable finished, Runnable during) {
        if (this.countDownTask != 0) {
            plugin.getServer().getScheduler().cancelTask(countDownTask);
            this.countDownTask = 0;
        }

        if(plugin.getUsers().isEmpty()) return;

        this.countDown = countdownFrom;
        this.countdownType = countdownType;

        countDownTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            --countDown;

            for (User p : plugin.getUsers()) {
                p.setLevel(countDown);
            }

            if (during != null) during.run();

            String baseMessage = "countdowns." + countdownType.name + ".warning";

            if (countDown == 120) plugin.messageAll(plugin.getLocale(baseMessage + ".minutes", "2"));
            else if (countDown == 60) plugin.messageAll(plugin.getLocale(baseMessage + ".minutes", "1"));
            else if (countDown == 30)
                plugin.messageAll(plugin.getLocale(baseMessage + ".seconds", "30"));
            else if (countDown == 10)
                //Log the 10 second warning only
                plugin.messageAll(plugin.getLocale(baseMessage + ".seconds", "10"));
            else if (countDown == 0) {
                plugin.getServer().getScheduler().cancelTask(countDownTask);
                countDownTask = 0;
                finished.run();
                this.countdownType = null;
            } else if (countDown < 6) {
                plugin.messageAll(plugin.getLocale(baseMessage + ".final", Integer.toString(countDown)));
            }


        }, 20, 20);
    }

    public void startEndCountdown() {
        Random random = plugin.getRandom();
        plugin.messageAll(ChatColor.GREEN + "Teleporting back to the lobby in 15 seconds!");

        startCountdown(15, CountdownType.LOBBY, () -> plugin.changeGameState(GameState.LOBBY), () -> {
            if (countDown < 10) return;
            if(plugin.getGameInstance().getWinningTeam() == null) return;

            User randomUser = plugin.getTeam(plugin.getGameInstance().getWinningTeam()).getRandomUser();
            if (randomUser == null) return;
            Location loc = randomUser.getLocation();
            Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

            Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

            firework.setVelocity(new Vector(0, 0.5f, 0));
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(
                    FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color).withFade(fade)
                            .build());
            firework.setFireworkMeta(meta);
        });
    }

    public void startShowdownCountdown() {
        plugin.messageAll(ChatColor.GREEN + "Showdown starting in 30 seconds!");

        startCountdown(30, CountdownType.SHOWDOWN, () -> plugin.changeGameState(GameState.SHOWDOWN), null);
    }

    public void startLobbyCountdown() {
        plugin.messageAll(plugin.getLocale("countdowns.game.warning.minutes", "3"));

        startCountdown(180, CountdownType.GAME, () -> {
            if (plugin.getPlayerCount() > 3) {
                plugin.changeGameState(GameState.GAME);
            } else {
                plugin.messageAll(plugin.getLocale("lobby.info.not-enough-players"));
                startLobbyCountdown();
            }
        }, null);
    }

    public void stopCountdown() {
        if (countDownTask == 0) return;

        plugin.getServer().getScheduler().cancelTask(countDownTask);
        countDownTask = 0;
        countdownType = null;

        for (User p : plugin.getUsers()) {
            p.setLevel(0);
        }
    }
}
