package net.hmmer.infection.network;

import net.hmmer.infection.Infection;
import net.hmmer.infection.border.BorderRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * InfectPlayerPayload
 * @param target the player who is infected
 * @param pos location the game takes place at
 * @param borderStuff a record that stores stuff about the border
 * @param zombies If zombieMode is enabled or not
 */
public record InfectPlayerPayload(UUID target, BlockPos pos, BorderRecord borderStuff,
                                  boolean zombies) implements CustomPacketPayload {

    public static final Type<InfectPlayerPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Infection.MOD_ID, "infect_player"));

    // How the UUID is written to and read from the network
    public static final StreamCodec<RegistryFriendlyByteBuf, InfectPlayerPayload> CODEC =
        StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, InfectPlayerPayload::target,
                BlockPos.STREAM_CODEC, InfectPlayerPayload::pos,
                BorderRecord.STREAM_CODEC, InfectPlayerPayload::borderStuff,
                ByteBufCodecs.BOOL, InfectPlayerPayload::zombies,
                InfectPlayerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}