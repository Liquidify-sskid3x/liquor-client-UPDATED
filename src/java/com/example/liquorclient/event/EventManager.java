package com.example.liquorclient.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private static final Map<Class<? extends Event>, List<Handler>> HANDLERS = new HashMap<>();

    public static void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(paramType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    Subscribe subscribe = method.getAnnotation(Subscribe.class);
                    
                    HANDLERS.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                            .add(new Handler(subscriber, method, subscribe.priority()));
                    
                    HANDLERS.get(eventClass).sort(Comparator.comparingInt(Handler::priority).reversed());
                }
            }
        }
    }

    public static void unsubscribe(Object subscriber) {
        for (List<Handler> handlers : HANDLERS.values()) {
            handlers.removeIf(handler -> handler.subscriber() == subscriber);
        }
    }

    public static <T extends Event> T post(T event) {
        List<Handler> handlers = HANDLERS.get(event.getClass());
        if (handlers != null) {
            for (Handler handler : handlers) {
                try {
                    handler.method().setAccessible(true);
                    handler.method().invoke(handler.subscriber(), event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return event;
    }

    private record Handler(Object subscriber, Method method, int priority) {}
}
