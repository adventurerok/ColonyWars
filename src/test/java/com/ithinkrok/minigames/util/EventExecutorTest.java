package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.event.MinigamesEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.bukkit.event.Listener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by paul on 20/01/16.
 */
@RunWith(DataProviderRunner.class)
public class EventExecutorTest {

    @Mock MinigamesEvent event;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void shouldExecuteAllEventsInTheCorrectOrder() {
        OrderListener listener = new OrderListener();

        EventExecutor.executeEvent(event, listener);

        assertThat(listener.doneLow && listener.doneHigh).isTrue();
    }

    private static class OrderListener implements Listener {

        private boolean doneLow = false;
        private boolean doneHigh = false;

        @MinigamesEventHandler(priority = MinigamesEventHandler.LOW)
        public void lowFirst(MinigamesEvent event) {
            assertThat(doneLow || doneHigh).isFalse();

            doneLow = true;
        }

        @MinigamesEventHandler(priority = MinigamesEventHandler.HIGH)
        public void highLast(MinigamesEvent event) {
            assertThat(doneLow && !doneHigh).isTrue();

            doneHigh = true;
        }
    }
}