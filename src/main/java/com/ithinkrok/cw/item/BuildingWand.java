package com.ithinkrok.cw.item;

import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * Created by paul on 10/12/16.
 */
public class BuildingWand implements CustomListener {

    private String buildingType;

    private String buildFailLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfig();

        buildingType = config.getString("building");

        buildFailLocale = config.getString("build_fail_locale", "building.invalid_loc");
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if (!event.hasBlock()) return;

        Location target = event.getClickedBlock().getLocation();
        BlockFace face = event.getBlockFace();
        target = target.clone().add(face.getModX(), face.getModY(), face.getModZ());

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        if(controller.buildBuilding(buildingType, event.getUser().getTeamIdentifier(), target, 0, false, false)){
            event.setStartCooldownAfterAction(true);
        } else {
            event.getUser().showAboveHotbarLocale(buildFailLocale);
        }
    }

}
