package net.hmmer.infection.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class TitleHelper {

    /**
     * sendOutTitle
     * @param server the minecraft server
     * @param title the title component to send out
     */
    public static void sendOutTitle(MinecraftServer server, Component title) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    /**
     * sendOutSubTitle
     * @param server the minecraft server
     * @param subTitle the subTitle component to send out
     */
    public static void sendOutSubTitle(MinecraftServer server, Component subTitle) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(subTitle));
        }
    }

    /**
     * sentOutActionBar
     * @param server the minecraft server
     * @param actionBar the actionBar component to send out
     */
    public static void sentOutActionBar(MinecraftServer server, Component actionBar) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetActionBarTextPacket(actionBar));
        }
    }

    /**
     * sentOutActionBar
     * @param player the minecraft player to send this message to
     * @param actionBar the actionBar component to send out
     */
    public static void sentOutActionBar(ServerPlayer player, Component actionBar) {
        player.connection.send(new ClientboundSetActionBarTextPacket(actionBar));
    }
}
