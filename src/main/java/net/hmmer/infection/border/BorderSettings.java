package net.hmmer.infection.border;

import net.hmmer.infection.util.TaskScheduler;
import net.hmmer.infection.util.TitleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class BorderSettings {
    public static final int INITIAL_BORDER_SIZE = 500;
    public static final int INITIAL_BORDER_SHRINK_SIZE = 20;
    public static final int INITIAL_BORDER_TIME_TILL = 5;
    public static final int INITIAL_BORDER_TIME_TO_SHRINK = 10;

    public static final int MIN_SIZE = 100;
    public static final int MAX_SIZE = 1000;

    public static final int MIN_SHRINK_SIZE = 10;
    public static final int MAX_SHRINK_SIZE = 100;

    public static final int MIN_TIME = 0;
    public static final int MAX_TIME = 99;


    public static void adjustTheBorder(int x, int z, MinecraftServer server, BorderRecord borderStuff,
                                       ServerLevel serverLevel) {
        // Clamp on the server too; never trust the number the client sent
        int size = Math.clamp(borderStuff.borderSize(), MIN_SIZE, MAX_SIZE);

        int shrinkSize = Math.clamp(borderStuff.borderShrinkSize(), MIN_SHRINK_SIZE, MAX_SHRINK_SIZE);

        serverLevel.getWorldBorder().setSize(size); // sets new world border size
        serverLevel.getWorldBorder().setCenter(x, z); // sets new world border location

        int timeTill = Math.clamp(borderStuff.borderTimeTillShrink(), MIN_TIME, MAX_TIME);
        int time = Math.clamp(borderStuff.borderTimeToShrink(), MIN_TIME, MAX_TIME);

        int waitTime = (timeTill * 60 * 20) + 300; // turns timeTill from minutes into ticks, adds
        // the time it takes for the game to start.

        int shrinkTime = time * 60 * 20; // converts minutes to ticks

        TaskScheduler.schedule(server, 300, () -> {
            String message;
            if (timeTill == 1) {
                message = "The border will shrink in " + timeTill + " minute";
            } else {
                message = "The border will shrink in " + timeTill + " minutes";
            }
            TitleHelper.sentOutActionBar(server, Component.literal(message));
        });

        TaskScheduler.schedule(server, waitTime, () -> {
            String message;
            if (time == 1) {
                message = "The border is shrinking to " +
                        shrinkSize + " blocks over the course of " + time + " minute";
            } else {
                message = "The border is shrinking to " +
                        shrinkSize + " blocks over the course of " + time + " minutes";
            }
            TitleHelper.sentOutActionBar(server, Component.literal(message));
            serverLevel.getWorldBorder().lerpSizeBetween(size, shrinkSize, shrinkTime, serverLevel.getGameTime());
            // shrinks the border over time shrinkTime
        });
    }
}
