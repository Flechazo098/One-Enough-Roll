package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.RollRenderState;
import com.flechazo.reallyradicalrolls.api.RollableEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            at = @At("TAIL"))
    private void rollable$copyRollToRenderState(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (state instanceof RollRenderState rollState && entity instanceof RollableEntity rollable) {
            rollState.rollable$setRolling(rollable.rollable$isRolling());
            rollState.rollable$setRoll(rollable.rollable$getRoll(partialTicks));
        }
    }
}
