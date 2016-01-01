package com.ithinkrok.cw;

import com.ithinkrok.cw.gamestate.LobbyListener;
import com.ithinkrok.minigames.GameState;
import com.ithinkrok.minigames.Game;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class CWGame extends Game<CWUser, CWTeam, CWGameGroup, CWGame> {

    public CWGame(Plugin plugin) {
        super(plugin, CWGameGroup.class, CWTeam.class, CWUser.class);
    }

    @Override
    public List<GameState> getGameStates() {
        List<GameState> result = new ArrayList<>();

        result.add(new GameState("lobby", new LobbyListener()));

        return result;
    }
}
