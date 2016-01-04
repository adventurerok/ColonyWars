package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.task.GameTask;

/**
 * Created by paul on 04/01/16.
 */
public class Countdown {

    private final String name;
    private final String localeStub;

    private GameTask task;
    private int seconds;

    public Countdown(String name, String localeStub, int seconds) {
        this.name = name;
        this.localeStub = localeStub;
        this.seconds = seconds;
    }

    public void start(GameGroup gameGroup) {
        task = gameGroup.repeatInFuture(task -> {
            --seconds;

            doCountdownMessage(gameGroup);

            for (User user : gameGroup.getUsers()) {
                user.setXpLevel(seconds);
            }

            if (seconds > 0) return;

            CountdownFinishedEvent event = new CountdownFinishedEvent(gameGroup, Countdown.this);
            gameGroup.gameEvent(event);

            //The event can change the amount of time left in the countdown
            if (seconds > 0) return;

            task.finish();
        }, 20, 20);
    }

    public void end() {
        task.cancel();
    }

    private void doCountdownMessage(Messagable messagable) {
        if (seconds > 30) {
            if (seconds % 60 != 0) return;
            messagable.sendLocale(localeStub + ".minutes", seconds / 60);
        } else {
            switch (seconds) {
                case 30:
                case 10:
                    messagable.sendLocale(localeStub + ".seconds", seconds);
                    return;
                case 5:
                case 4:
                case 3:
                case 2:
                    messagable.sendLocale(localeStub + ".final", seconds);
                    return;
                case 0:
                    messagable.sendLocale(localeStub + ".now");
            }
        }
    }

    public boolean isFinished() {
        return task.getTaskState() == GameTask.TaskState.FINISHED ||
                task.getTaskState() == GameTask.TaskState.CANCELLED;
    }
}
