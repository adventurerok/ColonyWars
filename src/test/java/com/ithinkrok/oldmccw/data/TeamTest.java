package com.ithinkrok.oldmccw.data;

import com.ithinkrok.oldmccw.WarsPlugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by paul on 16/12/15.
 */
public class TeamTest {

    private static int BIG_AMOUNT = 1000;
    private static int SMALL_AMOUNT = 100;

    @Mock
    WarsPlugin plugin;

    @Mock
    TeamColor teamColor;

    @Mock
    User user1;

    @Mock
    User user2;

    Team team;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        team = spy(new Team(plugin, teamColor));

        team.addUser(user1);

        doReturn(true).when(user1).getMoneyMessagesEnabled();
        doReturn(false).when(user2).getMoneyMessagesEnabled();
    }

    @Test
    public void addingCashShouldMessageUsersIfTheyHaveMoneyMessagesEnabled() {
        team.addTeamCash(BIG_AMOUNT, true);

        verify(user1, times(2)).sendLocale(anyString(), any());
        verify(user2, never()).sendLocale(anyString(), any());
    }

    @Test
    public void subtractingCashShouldMessageUsersIfTheyHaveMoneyMessagesEnabled() {
        team.addTeamCash(BIG_AMOUNT, false);
        team.subtractTeamCash(SMALL_AMOUNT, true);

        verify(user1, times(2)).sendLocale(anyString(), any());
        verify(user2, never()).sendLocale(anyString(), any());
    }

}