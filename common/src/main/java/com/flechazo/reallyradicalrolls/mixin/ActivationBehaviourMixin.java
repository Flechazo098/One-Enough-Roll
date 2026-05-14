package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig.ActivationMode;
import com.flechazo.reallyradicalrolls.util.MixinHooks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ActivationBehaviourMixin extends Avatar {

    protected ActivationBehaviourMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "tryToStartFallFlying()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rollable$interceptFallFlyingStart(CallbackInfoReturnable<Boolean> cir) {
        if (this.onGround() || this.isInWater() || this.hasEffect(MobEffects.LEVITATION)) {
            return;
        }

        ActivationMode mode = RollableClientConfig.CONFIG_UNIT.get().activation().mode();

        if (((Player) (Object) this) instanceof LocalPlayer
                && (mode == ActivationMode.TRIPLE_JUMP
                || mode == ActivationMode.HYBRID
                || mode == ActivationMode.HYBRID_TOGGLE)) {

            boolean shouldCancel = mode == ActivationMode.TRIPLE_JUMP;

            if (!MixinHooks.wasJumping) {
                MixinHooks.wasJumping = true;
                if (!MixinHooks.secondJump) {
                    MixinHooks.secondJump = true;
                    if (shouldCancel) cir.setReturnValue(false);
                } else {
                    MixinHooks.thirdJump = mode != ActivationMode.HYBRID_TOGGLE || !MixinHooks.thirdJump;
                }
            } else {
                if (shouldCancel) cir.setReturnValue(false);
            }
        }
    }
}
