package com.ithinkrok.cw.item;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * Created by paul on 10/12/16.
 */
public class BuildingWand implements CustomListener {

    private String buildingType;

    private String buildFailLocale;

    private Calculator removeTime;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfig();

        buildingType = config.getString("building");

        buildFailLocale = config.getString("build_fail_locale", "building.invalid_loc");

        removeTime = new ExpressionCalculator(config.getString("remove_seconds", "-1"));
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if (!event.hasBlock()) return;

        Location target = event.getClickedBlock().getLocation();
        BlockFace face = event.getBlockFace();
        target = target.clone().add(face.getModX(), face.getModY(), face.getModZ());

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        Building building =
                controller.buildBuilding(buildingType, event.getUser().getTeamIdentifier(), target, 0, false, false);
        if (building != null) {
            event.setStartCooldownAfterAction(true);

            int removeTicks = (int) (removeTime.calculate(event.getUser().getUpgradeLevels()) * 20);
            if(removeTicks > 0) {
                GameTask task = event.getUserGameGroup().doInFuture(t -> {
                   building.remove();
                }, removeTicks);

                event.getUserGameGroup().bindTaskToCurrentGameState(task);
                event.getUserGameGroup().bindTaskToCurrentMap(task);
            }
        } else {
            event.getUser().showAboveHotbarLocale(buildFailLocale);
        }
    }

}
