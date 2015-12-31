package com.ithinkrok.oldmccw.data;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.minigames.TeamColor;
import com.ithinkrok.oldmccw.util.io.MapConfig;
import com.ithinkrok.oldmccw.util.io.WarsConfig;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * Created by paul on 19/12/15.
 */
public class WarsMap {

    private String name;
    private String mapFolder;
    private World.Environment environment;
    private boolean supportsTeamCount;

    public WarsMap(WarsPlugin plugin, String name) {
        this.name = name;

        MapConfig mapConfig = new MapConfig(plugin, name);

        WarsConfig warsConfig = new WarsConfig(() -> mapConfig);

        mapFolder = warsConfig.getMapFolder(name);
        environment = warsConfig.getMapEnvironment(name);

        for(TeamColor teamColor : TeamColor.values()) {
            Vector baseLocation = warsConfig.getBaseLocation(name, teamColor);
            if(baseLocation.getY() == 0) return;
        }

        supportsTeamCount = true;
    }

    public String getName() {
        return name;
    }

    public String getMapFolder() {
        return mapFolder;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public boolean supportsTeamCount() {
        return supportsTeamCount;
    }
}
