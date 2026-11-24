package net.hmmer.infection.network;

import net.hmmer.infection.Infection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenMenuPayload() implements CustomPacketPayload {

    public static final OpenMenuPayload INSTANCE = new OpenMenuPayload();

    public static final Type<OpenMenuPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Infection.MOD_ID, "open_menu_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuPayload> CODEC =
            StreamCodec.unit(INSTANCE);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
