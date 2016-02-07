package com.ithinkrok.cw.map;

import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 08/01/16.
 */
public class InitialBuildingSpawner implements CustomListener {

    private final List<InitialBuilding> buildingList = new ArrayList<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        List<ConfigurationSection> buildingConfigs = ConfigUtils.getConfigList(event.getConfig(), "initial_buildings");

        buildingList.addAll(buildingConfigs.stream().map(InitialBuilding::new).collect(Collectors.toList()));
    }

    @CustomEventHandler
    public void onMapChange(MapChangedEvent event) {
        if(!event.getNewMap().getListeners().contains(this)) return;

        GameMap map = event.getNewMap();
        BuildingController controller = BuildingController.getOrCreate(event.getGameGroup());

        for(InitialBuilding building : buildingList) {
            Location loc = map.getLocation(building.location);
            TeamIdentifier identifier = event.getGameGroup().getTeamIdentifier(building.teamName);
            if(identifier == null) continue;

            controller.buildBuilding(building.buildingName, identifier, loc, building.rotation, true);
        }
    }

    private static class InitialBuilding {
        private final Vector location;
        private final String teamName;
        private final String buildingName;
        private final int rotation;

        public InitialBuilding(ConfigurationSection config) {
            location = ConfigUtils.getVector(config, "location");
            teamName = config.getString("team");
            buildingName = config.getString("building");
            rotation = config.getInt("rotation", 0);
        }
    }
}
