package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.MouseHandlerAccessor;
import com.flechazo.reallyradicalrolls.api.RollableEntity;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements MouseHandlerAccessor {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private final Vector2d rollable$mouseTurnVec = new Vector2d();

    @Inject(
            method = "handleAccumulatedMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z",
                    ordinal = 0
            )
    )
    private void rollable$maintainMouseMomentum(CallbackInfo ci, @Local(ordinal = 1) double mousea) {
        if (minecraft.player != null && !minecraft.isPaused()) {
            rollable$updateMouse(minecraft.player, 0, 0, mousea);
        }
    }

    @WrapWithCondition(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private boolean rollable$changeLookDirection(LocalPlayer player, double cursorDeltaX, double cursorDeltaY, @Local(argsOnly = true) double timeDelta) {
        return !rollable$updateMouse(player, cursorDeltaX, cursorDeltaY, timeDelta);
    }

    @Unique
    public boolean rollable$updateMouse(LocalPlayer player, double cursorDeltaX, double cursorDeltaY, double mouseDelta) {
        RollableClientConfig config = RollableClientConfig.CONFIG_UNIT.get();
        if (player instanceof RollableEntity rollable && rollable.rollable$isRolling()) {
            if (config.generals().momentumBasedMouse()) {
                rollable$mouseTurnVec.add(new Vector2d(cursorDeltaX, cursorDeltaY).mul(1f / 300));
                if (rollable$mouseTurnVec.lengthSquared() > 1.0) rollable$mouseTurnVec.normalize();
                var readyTurnVec = new Vector2d(rollable$mouseTurnVec);
                double deadzone = config.generals().momentumMouseDeadzone();
                if (readyTurnVec.lengthSquared() < deadzone * deadzone) readyTurnVec.zero();
                readyTurnVec.mul(1200 * (float) mouseDelta);
                rollable.rollable$changeLook(readyTurnVec.y, readyTurnVec.x, 0, config.sensitivity().desktop(), mouseDelta);
            } else {
                rollable$mouseTurnVec.zero();
                rollable.rollable$changeLook(cursorDeltaY, cursorDeltaX, 0, config.sensitivity().desktop(), mouseDelta);
            }
            return true;
        }
        rollable$mouseTurnVec.zero();
        return false;
    }

    @Override
    public Vector2d rollable$getMouseTurnVec() {
        return rollable$mouseTurnVec;
    }
}
