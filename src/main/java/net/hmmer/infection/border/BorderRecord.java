package net.hmmer.infection.border;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * BorderRecord
 * @param borderSize The initial size of the border
 * @param borderShrinkSize The size the border gets to when it is finished shrinking
 * @param borderTimeTillShrink The time to wait before the border starts shrinking
 * @param borderTimeToShrink Time it takes for the border to shrink
 */

public record BorderRecord(int borderSize, int borderShrinkSize,
                           int borderTimeTillShrink, int borderTimeToShrink) {

    public static final StreamCodec<RegistryFriendlyByteBuf, BorderRecord> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BorderRecord::borderSize,
                    ByteBufCodecs.VAR_INT, BorderRecord::borderShrinkSize,
                    ByteBufCodecs.VAR_INT, BorderRecord::borderTimeTillShrink,
                    ByteBufCodecs.VAR_INT, BorderRecord::borderTimeToShrink,
                    BorderRecord::new);
}
