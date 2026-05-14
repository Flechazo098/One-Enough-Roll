package com.flechazo.reallyradicalrolls.flight.modifier;

import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.flight.RollContext;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.SmoothDouble;

public class StrafeRollModifiers {
    private static final SmoothDouble STRAFE_ROLL_SMOOTHER = new SmoothDouble();
    private static final SmoothDouble STRAFE_YAW_SMOOTHER = new SmoothDouble();

    public static RotateState applyStrafeRoll(RotateState state, RollContext context) {
        RollableClientConfig config = RollableClientConfig.CONFIG_UNIT.get();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return state;

        Options options = Minecraft.getInstance().options;
        double rollDelta = 0;
        double yawDelta = 0;

        if (options.keyLeft.isDown() && !options.keyRight.isDown()) {
            rollDelta = -config.swim().strafeRollStrength();
            yawDelta = -config.swim().strafeYawStrength();
        } else if (options.keyRight.isDown() && !options.keyLeft.isDown()) {
            rollDelta = config.swim().strafeRollStrength();
            yawDelta = config.swim().strafeYawStrength();
        }

        if (config.swim().smoothingEnabled() && config.swim().strafeSmoothingEnabled()) {
            rollDelta = STRAFE_ROLL_SMOOTHER.getNewDeltaValue(rollDelta, 1 / config.swim().smoothingValues().roll() * context.getRenderDelta());
            yawDelta = STRAFE_YAW_SMOOTHER.getNewDeltaValue(yawDelta, 1 / config.swim().smoothingValues().yaw() * context.getRenderDelta());
        }

        return state.add(0, yawDelta, rollDelta);
    }
}
