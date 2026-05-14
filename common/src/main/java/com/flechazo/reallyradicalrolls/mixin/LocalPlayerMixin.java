package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.flight.MagicNumbers;
import com.flechazo.reallyradicalrolls.flight.RollContext;
import com.flechazo.reallyradicalrolls.flight.RollProcessGroup;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import com.flechazo.reallyradicalrolls.flight.modifier.RotationModifiers;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends PlayerMixin {
    @Shadow public float yBob;
    @Shadow public float yBobO;

    @Unique
    private RollProcessGroup rollable$processGroup;

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void rollable$tickTail(CallbackInfo ci) {
        rollable$baseTickTail(null);
        rollable$processGroup = RollProcessGroup.getActive();
    }

    @Override
    @Unique
    protected void rollable$baseTickTail2() {
        rollable$setRolling(rollable$processGroup != null);
    }

    @Override
    public RollProcessGroup rollable$getCurrentProcessGroup() {
        return rollable$processGroup;
    }

    @Override
    public void rollable$changeLook(double pitch, double yaw, double roll, RotateState sensitivity, double mouseDelta) {
        var rotDelta = new RotateState(pitch, yaw, roll);
        var currentRoll = rollable$getRoll();
        var currentRotation = new RotateState(
                getXRot(),
                getYRot(),
                currentRoll
        );
        var context = RollContext.of(currentRotation, rotDelta, mouseDelta);

        context.useModifier(RotationModifiers.fixNaN("INPUT"));
        if (rollable$processGroup != null) rollable$processGroup.processBeforeModifier(context);
        context.useModifier(RotationModifiers.fixNaN("EARLY_CAMERA_MODIFIERS"));
        context.useModifier((rotation, ctx) -> rotation.applySensitivity(sensitivity));
        context.useModifier(RotationModifiers.fixNaN("SENSITIVITY"));
        if (rollable$processGroup != null) rollable$processGroup.processAfterModifier(context);
        context.useModifier(RotationModifiers.fixNaN("LATE_CAMERA_MODIFIERS"));

        rotDelta = context.getRotationDelta();
        rollable$changeLook((float) rotDelta.pitch(), (float) rotDelta.yaw(), (float) rotDelta.roll());
    }

    @Override
    public void rollable$changeLook(float pitch, float yaw, float roll) {
        var currentPitch = getXRot();
        var currentYaw = getYRot();
        var currentRoll = rollable$getRoll();

        // Convert pitch, yaw, and roll to a facing and left vector
        var facing = new Vector3d(getForward().toVector3f());
        var left = new Vector3d(1, 0, 0);
        left.rotateZ(-currentRoll * MagicNumbers.TORAD);
        left.rotateX(-currentPitch * MagicNumbers.TORAD);
        left.rotateY(-(currentYaw + 180) * MagicNumbers.TORAD);

        // Apply pitch
        facing.rotateAxis(-0.15 * pitch * MagicNumbers.TORAD, left.x, left.y, left.z);

        // Apply yaw
        var up = facing.cross(left, new Vector3d());
        facing.rotateAxis(0.15 * yaw * MagicNumbers.TORAD, up.x, up.y, up.z);
        left.rotateAxis(0.15 * yaw * MagicNumbers.TORAD, up.x, up.y, up.z);

        // Apply roll
        left.rotateAxis(0.15 * roll * MagicNumbers.TORAD, facing.x, facing.y, facing.z);

        // Extract new pitch, yaw, and roll
        double newPitch = -Math.asin(facing.y) * MagicNumbers.TODEG;
        double newYaw = -Math.atan2(facing.x, facing.z) * MagicNumbers.TODEG;

        var normalLeft = new Vector3d(1, 0, 0).rotateY(-(newYaw + 180) * MagicNumbers.TORAD);
        double newRoll = -Math.atan2(left.cross(normalLeft, new Vector3d()).dot(facing), left.dot(normalLeft)) * MagicNumbers.TODEG;

        // Calculate deltas
        double deltaY = newPitch - currentPitch;
        double deltaX = newYaw - currentYaw;
        double deltaRoll = newRoll - currentRoll;

        // Apply vanilla pitch and yaw
        turn(deltaX / 0.15, deltaY / 0.15);

        // Apply roll
        this.rollable$roll += (float) deltaRoll;
        this.rollable$prevRoll += (float) deltaRoll;

        // fix hand spasm when wrapping yaw value
        if (getYRot() < -90 && yBob > 90) {
            yBob -= 360;
            yBobO -= 360;
        } else if (getYRot() > 90 && yBob < -90) {
            yBob += 360;
            yBobO += 360;
        }
    }
}
