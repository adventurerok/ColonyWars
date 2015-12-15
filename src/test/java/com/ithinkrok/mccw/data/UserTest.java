package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.fest.assertions.api.Assertions.*;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Tests the User class
 */
@RunWith(JUnitParamsRunner.class)
public class UserTest {

    @Mock
    WarsPlugin plugin;

    @Mock
    Player player;

    @Mock
    StatsHolder statsHolder;

    User user;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User(plugin, player, statsHolder);

    }

    @Test
    public void setVelocityShouldChangePlayerVelocity() throws Exception {
        Vector velocity = mock(Vector.class);

        user.setVelocity(velocity);

        verify(player).setVelocity(velocity);
    }

    @Test
    @Parameters({"0.3", "0.5", "1.0"})
    public void setFlySpeedShouldChangePlayerFlySpeed(float flySpeed) throws Exception {
        user.setFlySpeed(flySpeed);

        verify(player).setFlySpeed(flySpeed);
    }

    @Test
    @Parameters({"true", "false"})
    public void setAllowFlightShouldChangePlayerAllowFlight(boolean allowFlight) {
        user.setAllowFlight(allowFlight);

        verify(player).setAllowFlight(allowFlight);
    }

    @Test
    @Parameters({"true", "false"})
    public void setFlyingShouldChangePlayerFlying(boolean flying) {
        user.setFlying(flying);

        verify(player).setFlying(flying);
    }

    @Test
    public void getLocationShouldReturnPlayerLocation() {
        Location location = mock(Location.class);

        doReturn(location).when(player).getLocation();

        Location returnedLocation = user.getLocation();

        assertThat(returnedLocation).isSameAs(location);
    }
}