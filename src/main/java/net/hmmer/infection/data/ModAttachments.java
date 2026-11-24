package net.hmmer.infection.data;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hmmer.infection.Infection;
import net.minecraft.resources.Identifier;

public class ModAttachments {
    public static final AttachmentType<Boolean> INFECTED = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Infection.MOD_ID, "infected"),
            builder -> builder.persistent(Codec.BOOL)
                    .copyOnDeath());

    public static void registerAttachments() {
        Infection.LOGGER.info("Registering ModAttachments for" + Infection.MOD_ID);
    }
}
