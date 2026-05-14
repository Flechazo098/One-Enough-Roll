package com.flechazo.reallyradicalrolls.mixin;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    @Unique
    protected boolean rollable$isRolling;
    @Unique
    protected float rollable$prevRoll;
    @Unique
    protected float rollable$roll;

    @Override
    protected void rollable$baseTickTail(CallbackInfo ci) {
        rollable$baseTickTail2();

        rollable$prevRoll = rollable$getRoll();

        if (!rollable$isRolling()) {
            rollable$setRoll(0.0f);
        }
    }

    @Unique
    protected void rollable$baseTickTail2() {
    }

    @Override
    public boolean rollable$isRolling() {
        return rollable$isRolling;
    }

    @Unique
    public void rollable$setRolling(boolean rolling) {
        rollable$isRolling = rolling;
    }

    @Override
    public float rollable$getRoll() {
        return rollable$roll;
    }

    @Override
    public float rollable$getRoll(float tickDelta) {
        if (tickDelta == 1.0f) {
            return rollable$getRoll();
        }
        return Mth.lerp(tickDelta, rollable$prevRoll, rollable$getRoll());
    }

    @Override
    public void rollable$setRoll(float roll) {
        if (!Float.isFinite(roll)) {
            Util.logAndPauseIfInIde("Invalid entity rotation: " + roll + ", discarding.");
            return;
        }
        var lastRoll = rollable$getRoll();
        this.rollable$roll = roll;

        if (roll < -90 && lastRoll > 90) {
            rollable$prevRoll -= 360;
        } else if (roll > 90 && lastRoll < -90) {
            rollable$prevRoll += 360;
        }
    }
}
