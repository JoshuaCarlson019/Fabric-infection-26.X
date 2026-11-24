package net.hmmer.infection.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class TaskScheduler {

    private record Task(long runAtTick, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();

    public static void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Task(server.getTickCount() + delayTicks, action));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            long now = server.getTickCount();

            List<Task> due = new ArrayList<>();
            TASKS.removeIf(task -> {
                if (task.runAtTick <= now) {
                    due.add(task);
                    return true;
                }
                return false;
            });

            for (Task task : due) {
                task.action.run();
            }
        });
        // tasks don't carry to another world
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TASKS.clear());
    }

    // Clears all tasks
    public static void clear() {
        TASKS.clear();
    }
}
