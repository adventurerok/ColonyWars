package com.ithinkrok.minigames.util;

import org.bukkit.event.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by paul on 01/01/16.
 */
public class EventExecutor {

    private static Map<Class<? extends Listener>, ListenerHandler> listenerHandlerMap = new HashMap<>();

    public static void executeEvent(Event event, Listener... listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    private static void executeListeners(Event event, SortedMap<MethodExecutor, Listener> map) {
        for (Map.Entry<MethodExecutor, Listener> entry : map.entrySet()) {
            try {
                entry.getKey().execute(entry.getValue(), event);
            } catch (EventException e) {
                System.out.println("Failed while calling event listener: " + entry.getValue().getClass());
                e.printStackTrace();
            }
        }
    }

    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(Event event, Listener... listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Listener listener : listeners) {
            for (MethodExecutor methodExecutor : getMethodExecutors(listener, event)) {
                map.put(methodExecutor, listener);
            }
        }

        return map;
    }

    private static Collection<MethodExecutor> getMethodExecutors(Listener listener, Event event) {
        ListenerHandler handler = listenerHandlerMap.get(listener.getClass());

        if (handler == null) {
            handler = new ListenerHandler(listener.getClass());
            listenerHandlerMap.put(listener.getClass(), handler);
        }

        return handler.getMethodExecutors(event);
    }

    @SafeVarargs
    public static void executeEvent(Event event, Collection<Listener>... listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    @SafeVarargs
    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(Event event,
                                                                            Collection<Listener>... listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Collection<Listener> listenerGroup : listeners) {
            for (Listener listener : listenerGroup) {
                for (MethodExecutor methodExecutor : getMethodExecutors(listener, event)) {
                    map.put(methodExecutor, listener);
                }
            }
        }

        return map;
    }

    public static void executeEvent(Event event, Collection<Collection<Listener>> listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(Event event,
                                                                            Collection<Collection<Listener>> listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Collection<Listener> listenerGroup : listeners) {
            for (Listener listener : listenerGroup) {
                for (MethodExecutor methodExecutor : getMethodExecutors(listener, event)) {
                    map.put(methodExecutor, listener);
                }
            }
        }

        return map;
    }

    private static class ListenerHandler {
        private Class<? extends Listener> listenerClass;

        private Map<Class<? extends Event>, List<MethodExecutor>> eventMethodsMap = new HashMap<>();

        public ListenerHandler(Class<? extends Listener> listenerClass) {
            this.listenerClass = listenerClass;
        }

        public Collection<MethodExecutor> getMethodExecutors(Event event) {
            List<MethodExecutor> eventMethods = eventMethodsMap.get(event.getClass());

            if (eventMethods == null) {
                eventMethods = new ArrayList<>();

                for (Method method : listenerClass.getMethods()) {
                    if (method.getParameterCount() != 1) continue;
                    if (!method.isAnnotationPresent(EventHandler.class)) continue;
                    if (!method.getParameterTypes()[0].isInstance(event)) continue;

                    eventMethods.add(new MethodExecutor(method,
                            method.getAnnotation(EventHandler.class).ignoreCancelled()));
                }

                Collections.sort(eventMethods);

                eventMethodsMap.put(event.getClass(), eventMethods);
            }

            return eventMethods;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodExecutor that = (MethodExecutor) o;

            if (ignoreCancelled != that.ignoreCancelled) return false;
            return method.equals(that.method);

        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + (ignoreCancelled ? 1 : 0);
            return result;
        }
    }
}
