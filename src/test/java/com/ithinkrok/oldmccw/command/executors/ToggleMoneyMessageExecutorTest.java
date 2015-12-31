package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.command.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by paul on 15/12/15.
 */
public class ToggleMoneyMessageExecutorTest {

    @Mock
    User user;

    @Mock
    Command command;

    ToggleMoneyMessageExecutor executor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        executor = new ToggleMoneyMessageExecutor();
    }

    @Test
    public void commandShouldToggleUserMoneyMessage() {
        boolean success = executor.onCommand(user, command, "togglemoneymessage", new String[0]);

        Mockito.verify(user).toggleMoneyMessagesEnabled();

        assertThat(success).isTrue();
    }
}