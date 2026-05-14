package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.ModKeybindings;
import com.flechazo.reallyradicalrolls.compat.controlify.ControlifyCompat;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.config.ServerConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class ElytraThrustMixin extends Entity {

    public ElytraThrustMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyArg(
            method = "travelFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 0
            ),
            index = 0
    )
    private Vec3 rollable$wrapElytraVelocity(Vec3 original) {
        if (!((Object) this instanceof LocalPlayer)) return original;

        RollableClientConfig config = RollableClientConfig.CONFIG_UNIT.get();
        if (!config.thrust().enabled()) return original;
        if (!ServerConfig.CONFIG_UNIT.get().allowThrusting()) return original;

        Vec3 rotation = getLookAngle();
        Vec3 velocity = getDeltaMovement();

        double throttleSign = ModKeybindings.THRUST_FORWARD.isDown() ? 1
                : ModKeybindings.THRUST_BACKWARD.isDown() ? -1 : 0;
        throttleSign += ControlifyCompat.getThrustModifier();

        if (throttleSign == 0) return original;

        double maxSpeed = config.thrust().maxThrust();
        double speedIncrease = Math.max(
                maxSpeed - velocity.length() * Math.signum(rotation.dot(velocity) * throttleSign), 0)
                / maxSpeed * throttleSign;
        double acceleration = config.thrust().acceleration() * speedIncrease;

        return original.add(
                rotation.x * acceleration,
                rotation.y * acceleration,
                rotation.z * acceleration
        );
    }
}
