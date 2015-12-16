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
import org.mockito.verification.VerificationMode;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


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
    public Player player;

    @Mock
    public StatsHolder statsHolder;

    public User user;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = spy(new User(plugin, player, statsHolder));

        doNothing().when(user).updateScoreboard();
    }

    @Test
    @Parameters({"true", "false"})
    public void toggleMoneyMessageShouldToggleMoneyMessage(boolean enabledByDefault) throws Exception {
        user.setMoneyMessagesEnabled(enabledByDefault);

        user.toggleMoneyMessagesEnabled();

        assertThat(user.getMoneyMessagesEnabled()).isNotEqualTo(enabledByDefault);

        user.toggleMoneyMessagesEnabled();

        assertThat(user.getMoneyMessagesEnabled()).isEqualTo(enabledByDefault);
    }

    @Test
    @Parameters({"true", "false"})
    public void toggleMoneyMessageShouldSendLocale(boolean enabledByDefault) {
        user.setMoneyMessagesEnabled(enabledByDefault);

        user.toggleMoneyMessagesEnabled();

        verify(user, times(1)).sendLocale(anyString(), anyVararg());
    }

    @Test
    public void moneyMessagesEnabledShouldBeTrueByDefault() {
        assertThat(user.getMoneyMessagesEnabled()).isTrue();
    }

    @Test
    @Parameters({"true, true, true", "true, false, false", "false, true, false", "false, false, false"})
    public void subtractPlayerCashShouldMessageOnlyIfMessageTrue(boolean message, boolean moneyMessagesEnabled,
                                                                 boolean expectedResult) {
        int SMALL_AMOUNT = 100;
        int BIG_AMOUNT = 1000;

        user.setMoneyMessagesEnabled(moneyMessagesEnabled);
        user.addPlayerCash(BIG_AMOUNT, false);

        user.subtractPlayerCash(SMALL_AMOUNT, message);

        VerificationMode verificationMode = expectedResult ? atLeastOnce() : never();

        verify(user, verificationMode).sendLocale(anyString(), any());
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