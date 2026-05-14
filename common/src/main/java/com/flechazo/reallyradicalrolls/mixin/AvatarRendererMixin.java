package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.RollRenderState;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @ModifyArg(
            method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V",
                    ordinal = 1
            ),
            index = 0
    )
    private Quaternionfc rollable$modifyRoll(Quaternionfc original, @Local(argsOnly = true) AvatarRenderState state) {
        if (state instanceof RollRenderState rollState && rollState.rollable$isRolling()) {
            var roll = rollState.rollable$getRoll();
            return Axis.YP.rotationDegrees(roll);
        }
        return original;
    }
}
