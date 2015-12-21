package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.WarsCommandSender;
import org.bukkit.command.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by paul on 21/12/15.
 */
public class ReloadExecutorTest {

    @Mock
    WarsCommandSender sender;

    @Mock
    WarsPlugin plugin;

    @Mock
    Command command;

    ReloadExecutor reloadExecutor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        reloadExecutor = new ReloadExecutor();

        doReturn(plugin).when(sender).getPlugin();
    }

    @Test
    public void reloadCommandShouldCallPluginReload() {
        reloadExecutor.onCommand(sender, command, "cwreload", new String[0]);

        verify(plugin).reload();
    }

    @Test
    public void reloadCommandShouldAlertSender() {
        reloadExecutor.onCommand(sender, command, "cwreload", new String[0]);

        verify(sender, atLeastOnce()).sendLocale(anyString(), anyVararg());
    }
}