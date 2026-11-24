package net.hmmer.infection.util;

import net.hmmer.infection.Infection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PreventPlayerMovementHelper {

    public static final Identifier PREVENT_PLAYER_ID =
            Identifier.fromNamespaceAndPath(Infection.MOD_ID, "prevent_player_movement");

    private static final AttributeModifier PREVENT_PLAYER_MOVEMENT =
            new AttributeModifier(PREVENT_PLAYER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public static void freeze(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);

        if (speed != null && !speed.hasModifier(PREVENT_PLAYER_ID))
            speed.addTransientModifier(PREVENT_PLAYER_MOVEMENT);
        if (jump != null && !jump.hasModifier(PREVENT_PLAYER_ID))
            jump.addTransientModifier(PREVENT_PLAYER_MOVEMENT);
    }

    public static void unfreeze(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);

        if (speed != null) speed.removeModifier(PREVENT_PLAYER_ID);
        if (jump != null) jump.removeModifier(PREVENT_PLAYER_MOVEMENT);
    }

    public static void freezeAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            freeze(player);
    }

    public static void unfreezeAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            unfreeze(player);
    }
}
