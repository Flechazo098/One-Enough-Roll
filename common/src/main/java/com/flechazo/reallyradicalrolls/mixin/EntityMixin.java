package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.RollableEntity;
import com.flechazo.reallyradicalrolls.flight.RollProcessGroup;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements RollableEntity {
    @Shadow public abstract float getXRot();
    @Shadow public abstract float getYRot();
    @Shadow public abstract void turn(double cursorDeltaX, double cursorDeltaY);
    @Shadow public abstract Vec3 getForward();

    @Override
    public void rollable$changeLook(double pitch, double yaw, double roll, RotateState sensitivity, double mouseDelta) {
    }

    @Override
    public void rollable$changeLook(float pitch, float yaw, float roll) {
    }

    @Override
    public boolean rollable$isRolling() {
        return false;
    }

    @Override
    public float rollable$getRoll() {
        return 0;
    }

    @Override
    public float rollable$getRoll(float tickDelta) {
        return 0;
    }

    @Override
    public void rollable$setRoll(float roll) {
    }

    @Override
    public RollProcessGroup rollable$getCurrentProcessGroup() {
        return null;
    }
}
