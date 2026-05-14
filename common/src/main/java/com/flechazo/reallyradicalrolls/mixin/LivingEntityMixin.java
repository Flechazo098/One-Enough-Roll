package com.flechazo.reallyradicalrolls.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
    @Inject(
            method = "baseTick",
            at = @At("TAIL")
    )
    protected void rollable$baseTickTail(CallbackInfo ci) {
    }
}
