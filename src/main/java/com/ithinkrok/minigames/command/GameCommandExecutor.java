package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.lang.Messagable;

/**
 * Created by paul on 12/01/16.
 */
public interface GameCommandExecutor {

    boolean onCommand(Messagable sender, GameCommand command);
}
