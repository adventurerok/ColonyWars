package com.ithinkrok.cw.map;

import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 08/01/16.
 */
public class InitialBuildingSpawner implements Listener {

    private List<InitialBuilding> buildingList = new ArrayList<>();

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        List<ConfigurationSection> buildingConfigs = ConfigUtils.getConfigList(event.getConfig(), "initial_buildings");

        buildingList.addAll(buildingConfigs.stream().map(InitialBuilding::new).collect(Collectors.toList()));
    }

    @EventHandler
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
        private Vector location;
        private String teamName;
        private String buildingName;
        private int rotation;

        public InitialBuilding(ConfigurationSection config) {
            location = ConfigUtils.getVector(config, "location");
            teamName = config.getString("team");
            buildingName = config.getString("building");
            rotation = config.getInt("rotation", 0);
        }
    }
}
