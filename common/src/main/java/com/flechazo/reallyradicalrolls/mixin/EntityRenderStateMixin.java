package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.RollRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements RollRenderState {
    @Unique
    private boolean rollable$isRolling;
    @Unique
    private float rollable$roll;

    @Override
    public boolean rollable$isRolling() {
        return rollable$isRolling;
    }

    @Override
    public void rollable$setRolling(boolean rolling) {
        this.rollable$isRolling = rolling;
    }

    @Override
    public float rollable$getRoll() {
        return rollable$roll;
    }

    @Override
    public void rollable$setRoll(float roll) {
        this.rollable$roll = roll;
    }
}
