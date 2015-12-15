package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Tests the User class
 */
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
}