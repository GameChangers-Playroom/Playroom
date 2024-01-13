package io.github.flameyheart.playroom.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

import java.util.*;

public class ScheduleUtils {
    private final PriorityQueue<Event> events = new PriorityQueue<>(createEventComparator());
    private final Map<UUID, Event> eventsByName = new HashMap<>();

    public void close() {
        events.clear();
    }

    public void tick(MinecraftServer server) {
        Event event;
        while ((event = this.events.peek()) != null && event.triggerTime <= server.getOverworld().getTime()) {
            this.events.remove();
            event.callback.run();
        }
    }

    private static Comparator<Event> createEventComparator() {
        return Comparator.comparingLong(event -> event.triggerTime);
    }

    public void schedule(UUID id, long triggerTime, Runnable callback) {
        if (eventsByName.containsKey(id)) {
            events.remove(eventsByName.get(id));
        }
        Event event = new Event(triggerTime, callback);
        events.add(event);
        eventsByName.put(id, event);
    }

    public void schedule(MinecraftServer server, UUID id, long delay, Runnable callback) {
        schedule(id, server.getOverworld().getTime() + delay, callback);
    }

    public void scheduleOrExtend(MinecraftServer server, UUID id, long delay, Runnable callback) {
        if (eventsByName.containsKey(id)) {
            Event event = eventsByName.get(id);
            event.triggerTime += delay;
            return;
        }
        schedule(id, server.getOverworld().getTime() + delay, callback);
    }

    public static void scheduleDelay(UUID id, MinecraftServer server, long delay, TimerCallback<MinecraftServer> callback) {
        if (server == null) return;
        Timer<MinecraftServer> timer = server.getSaveProperties().getMainWorldProperties().getScheduledEvents();
        timer.setEvent("playroom#" + id, server.getSaveProperties().getMainWorldProperties().getTime() + delay, callback);
    }

    public static void schedule(MinecraftServer server, long time, TimerCallback<MinecraftServer> callback) {
        if (server == null) return;
        Timer<MinecraftServer> timer = server.getSaveProperties().getMainWorldProperties().getScheduledEvents();
        timer.setEvent("playroom#" + UUID.randomUUID(), time, callback);
    }

    static final class Event {
        private final Runnable callback;
        private long triggerTime;

        Event(long triggerTime, Runnable callback) {
            this.triggerTime = triggerTime;
            this.callback = callback;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Event) obj;
            return this.triggerTime == that.triggerTime &&
                    Objects.equals(this.callback, that.callback);
        }

        @Override
        public int hashCode() {
            return Objects.hash(triggerTime, callback);
        }

        @Override
        public String toString() {
            return "Event[" +
                    "triggerTime=" + triggerTime + ", " +
                    "callback=" + callback + ']';
        }
    }
}
