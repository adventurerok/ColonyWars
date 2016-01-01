package com.ithinkrok.minigames.util;

import org.bukkit.event.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by paul on 01/01/16.
 */
public class EventExecutor {

    private static Map<Class<? extends Listener>, ListenerHandler> listenerHandlerMap = new HashMap<>();

    public static void executeEvent(Listener listener, Event event) throws EventException {
        ListenerHandler handler = listenerHandlerMap.get(listener.getClass());

        if (handler == null) {
            handler = new ListenerHandler(listener.getClass());
            listenerHandlerMap.put(listener.getClass(), handler);
        }

        handler.executeEvent(listener, event);
    }

    private static class ListenerHandler {
        private Class<? extends Listener> listenerClass;

        private Map<Class<? extends Event>, List<MethodExecutor>> eventMethodsMap = new HashMap<>();

        public ListenerHandler(Class<? extends Listener> listenerClass) {
            this.listenerClass = listenerClass;
        }

        public void executeEvent(Listener listener, Event event) throws EventException {
            List<MethodExecutor> eventMethods = eventMethodsMap.get(event.getClass());

            if (eventMethods == null) {
                eventMethods = new ArrayList<>();

                for (Method method : listenerClass.getMethods()) {
                    if (!method.isAccessible()) continue;
                    if (method.getParameterCount() != 1) continue;
                    if (!method.isAnnotationPresent(EventHandler.class)) continue;
                    if (!method.getParameterTypes()[0].isInstance(event)) continue;

                    eventMethods.add(new MethodExecutor(method,
                            method.getAnnotation(EventHandler.class).ignoreCancelled()));
                }

                Collections.sort(eventMethods);

                eventMethodsMap.put(event.getClass(), eventMethods);
            } else {
                for (MethodExecutor method : eventMethods) {
                    method.execute(listener, event);
                }
            }
        }
    }

    private static class MethodExecutor implements Comparable<MethodExecutor> {
        private Method method;
        private boolean ignoreCancelled;

        public MethodExecutor(Method method, boolean ignoreCancelled) {
            this.method = method;
            this.ignoreCancelled = ignoreCancelled;
        }

        public void execute(Listener listener, Event event) throws EventException {
            if (ignoreCancelled && (event instanceof Cancellable) && ((Cancellable) event).isCancelled()) return;

            try {
                method.invoke(listener, event);
            } catch (Exception e) {
                throw new EventException(e, "Failed while calling event method: " + method.getName());
            }
        }

        @Override
        public int compareTo(MethodExecutor o) {
            return method.getAnnotation(EventHandler.class).priority()
                    .compareTo(o.method.getAnnotation(EventHandler.class).priority());
        }
    }
}
