package net.hmmer.infection.mixin.client;


import net.hmmer.infection.util.PreventPlayerMovementHelper;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(method= "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void infection$noFreezeZoom(CallbackInfoReturnable<Float> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer)(Object)this;
        AttributeInstance speed = self.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null && speed.hasModifier(PreventPlayerMovementHelper.PREVENT_PLAYER_ID)) {
            cir.setReturnValue(1.0f); // 1.0 is the normal fov
        }
    }
}
