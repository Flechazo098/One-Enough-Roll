package com.flechazo.reallyradicalrolls.neoforge.mixin;

import com.flechazo.reallyradicalrolls.api.RollableCamera;
import com.flechazo.reallyradicalrolls.api.RollableEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraNeoForgeMixin implements RollableCamera {
    @Shadow
    private Entity entity;
    @Unique
    private boolean rollable$isRolling;
    @Unique
    private float rollable$lastRollBack;
    @Unique
    private float rollable$rollBack;
    @Unique
    private float rollable$roll;
    @Unique
    private final ThreadLocal<Float> rollable$tempRoll = new ThreadLocal<>();

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Camera;eyeHeight:F", ordinal = 0, opcode = Opcodes.GETFIELD))
    private void rollable$interpolateRollBack(CallbackInfo ci) {
        if (this.entity instanceof RollableEntity rollable && !rollable.rollable$isRolling()) {
            this.rollable$lastRollBack = this.rollable$rollBack;
            this.rollable$rollBack -= this.rollable$rollBack * 0.5f;
        }
    }

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void rollable$captureTickDelta(float partialTicks, CallbackInfo ci, @Share("tickDelta") LocalFloatRef tickDeltaRef) {
        tickDeltaRef.set(partialTicks);
        this.rollable$isRolling = this.entity instanceof RollableEntity rollable && rollable.rollable$isRolling();
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void rollable$updateRollBack(float partialTicks, CallbackInfo ci) {
        if (this.rollable$isRolling) {
            this.rollable$rollBack = this.rollable$roll;
            this.rollable$lastRollBack = this.rollable$roll;
        }
    }

    // NeoForge 的 rotationYXZ 已经取反了，所以这里正常写就行了
    @WrapWithCondition(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", ordinal = 0))
    private boolean rollable$addRoll1(Camera instance, float yRot, float xRot, float roll, @Share("tickDelta") LocalFloatRef tickDelta) {
        if (this.entity instanceof RollableEntity rollable)
            if (this.rollable$isRolling)
                this.rollable$tempRoll.set(rollable.rollable$getRoll(tickDelta.get()));
            else
                this.rollable$tempRoll.set(Mth.lerp(tickDelta.get(), this.rollable$lastRollBack, this.rollable$rollBack));
        return true;
    }

    @WrapWithCondition(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", ordinal = 1))
    private boolean rollable$addRoll2(Camera instance, float yRot, float xRot, float roll) {
        this.rollable$tempRoll.set(this.rollable$roll);
        return true;
    }

    @WrapWithCondition(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1))
    private boolean rollable$addRoll3(Camera instance, float yRot, float xRot) {
        this.rollable$tempRoll.set(0.0f);
        return true;
    }

    // NeoForge 加了个三参数版本的，要修改这个
    @ModifyArg(method = "setRotation(FFF)V", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", remap = false), index = 2, require = 0)
    private float rollable$setRollNeoForge(float original) {
        Float roll = this.rollable$tempRoll.get();
        if (roll != null) {
            this.rollable$roll = roll;
            return original - (float) Math.toRadians(this.rollable$roll);
        }
        return original;
    }

    @Override
    public float rollable$getRoll() {
        return this.rollable$roll;
    }
}
