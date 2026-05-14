package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.config.KineticDamage;
import com.flechazo.reallyradicalrolls.config.ServerConfig;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class KineticDamageMixin {
    @ModifyVariable(
            method = "handleFallFlyingCollisions",
            at = @At("STORE"),
            require = 0,
            name = "dmg")
    private float rollable$modifyKineticDamage(float original) {
        KineticDamage damageType = ServerConfig.CONFIG_UNIT.get().kineticDamage();

        return switch (damageType) {
            case VANILLA -> original;
            case HIGH_SPEED -> original - 2.0f;
            case NONE -> 0.0f;
            case INSTANT_KILL -> Float.MAX_VALUE;
        };
    }
}
