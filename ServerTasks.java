package com.dotamobs.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;

/**
 * A tiny scheduler for short timed jobs on the server thread (ice that melts after a few seconds,
 * the hook pulling a creature towards you over several ticks).
 */
public final class ServerTasks {
    /** One job. {@link #tick} is called once per server tick until it returns {@code true} (= finished). */
    public interface Task {
        boolean tick(MinecraftServer server);
    }

    private static final List<Task> RUNNING = new ArrayList<>();
    private static final List<Task> NEW = new ArrayList<>();

    private ServerTasks() {}

    public static void add(Task task) {
        NEW.add(task);
    }

    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        if (!NEW.isEmpty()) {
            RUNNING.addAll(NEW);
            NEW.clear();
        }
        MinecraftServer server = event.server();
        RUNNING.removeIf(task -> task.tick(server));
    }
}
