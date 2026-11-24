package net.hmmer.infection.border;

import net.minecraft.server.level.ServerLevel;

public class BorderSettings {
    public static final int MIN_SIZE = 100;
    public static final int MAX_SIZE = 1000;


    public static void adjustTheBorder(int x, int z, int borderSize, ServerLevel serverLevel) {
        // Clamp on the server too; never trust the number the client sent
        int size = Math.clamp(borderSize, BorderSettings.MIN_SIZE, BorderSettings.MAX_SIZE);

        serverLevel.getWorldBorder().setSize(size); // sets new world border size
        serverLevel.getWorldBorder().setCenter(x, z); // sets new world border location
    }
}
