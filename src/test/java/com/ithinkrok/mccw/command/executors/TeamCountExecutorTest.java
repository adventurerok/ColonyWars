package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.WarsCommandSender;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.bukkit.command.Command;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Created by paul on 19/12/15.
 */
@RunWith(JUnitParamsRunner.class)
public class TeamCountExecutorTest {

    @Mock
    WarsCommandSender sender;

    @Mock
    WarsPlugin plugin;

    @Mock
    Command command;

    TeamCountExecutor teamCountExecutor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        teamCountExecutor = new TeamCountExecutor();

        doReturn(plugin).when(sender).getPlugin();
    }

    @Test
    @Parameters({"2", "4", "7", "12", "16"})
    public void teamCountShouldCallPluginChangeTeamCountMethod(int teamCount) {
        String[] args = new String[] {Integer.toString(teamCount)};

        teamCountExecutor.onCommand(sender, command, "ANY_LABEL", args);

        verify(plugin).changeTeamCount(teamCount);
    }

    @Test
    @Parameters({"1", "17", "3254", "-1", "0"})
    public void teamCountShouldNotAllowInvalidCounts(int teamCount) {
        String[] args = new String[] {Integer.toString(teamCount)};

        teamCountExecutor.onCommand(sender, command, "ANY_LABEL", args);

        verify(plugin, never()).changeTeamCount(teamCount);
    }

    @Test
    @Parameters({"-1", "0", "2", "12", "16", "17", "-13", "324"})
    public void teamCountShouldInformTheSender(int teamCount) {
        String[] args = new String[] {Integer.toString(teamCount)};

        teamCountExecutor.onCommand(sender, command, "ANY_LABEL", args);

        verify(sender, atLeastOnce()).sendLocale(anyString(), anyVararg());
    }
}