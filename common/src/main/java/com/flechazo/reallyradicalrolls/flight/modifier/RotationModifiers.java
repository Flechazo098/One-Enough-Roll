package com.flechazo.reallyradicalrolls.flight.modifier;

import com.flechazo.reallyradicalrolls.ModKeybindings;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.flight.MagicNumbers;
import com.flechazo.reallyradicalrolls.flight.RollContext;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SmoothDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RotationModifiers {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotationModifiers.class);
    public static final double ROLL_REORIENT_CUTOFF = Math.sqrt(10.0 / 3.0);

    private static final ThreadLocal<HashMap<String, Double>> VAR_MAP =
            ThreadLocal.withInitial(HashMap::new);

    public static RollContext.ConfiguresRotation buttonControls(double power) {
        return (rotationInstant, context) -> {
            var delta = power * context.getRenderDelta();
            var pitch = 0.0;
            var yaw = 0.0;
            var roll = 0.0;

            if (ModKeybindings.PITCH_UP.isDown()) pitch -= delta;
            if (ModKeybindings.PITCH_DOWN.isDown()) pitch += delta;
            if (ModKeybindings.YAW_LEFT.isDown()) yaw -= delta;
            if (ModKeybindings.YAW_RIGHT.isDown()) yaw += delta;
            if (ModKeybindings.ROLL_LEFT.isDown()) roll -= delta;
            if (ModKeybindings.ROLL_RIGHT.isDown()) roll += delta;

            return rotationInstant.add(pitch, yaw, roll);
        };
    }

    public static RollContext.ConfiguresRotation smoothing(SmoothDouble pitchSmoother, SmoothDouble yawSmoother,
                                                            SmoothDouble rollSmoother, RotateState smoothness) {
        return (rotationInstant, context) -> new RotateState(
                smoothness.pitch() == 0 ? rotationInstant.pitch()
                        : pitchSmoother.getNewDeltaValue(rotationInstant.pitch(), 1 / smoothness.pitch() * context.getRenderDelta()),
                smoothness.yaw() == 0 ? rotationInstant.yaw()
                        : yawSmoother.getNewDeltaValue(rotationInstant.yaw(), 1 / smoothness.yaw() * context.getRenderDelta()),
                smoothness.roll() == 0 ? rotationInstant.roll()
                        : rollSmoother.getNewDeltaValue(rotationInstant.roll(), 1 / smoothness.roll() * context.getRenderDelta())
        );
    }

    public static RotateState banking(RotateState rotationInstant, RollContext context) {
        var config = RollableClientConfig.CONFIG_UNIT.get();
        var delta = context.getRenderDelta();
        var currentRotation = context.getCurrentRotation();
        var currentRoll = currentRotation.roll() * MagicNumbers.TORAD;

        var xExpression = config.advanced().bankingXFormula().getCompiledOrDefaulting(0);
        var yExpression = config.advanced().bankingYFormula().getCompiledOrDefaulting(0);

        var vars = getVars(context);
        vars.put("banking_strength", config.banking().strength());

        var dX = xExpression.eval(vars);
        var dY = yExpression.eval(vars);

        if (Double.isNaN(dX)) dX = 0;
        if (Double.isNaN(dY)) dY = 0;

        return rotationInstant.addAbsolute(dX * delta, dY * delta, currentRoll);
    }

    public static RotateState reorient(RotateState rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();
        var currentRoll = context.getCurrentRotation().roll() * MagicNumbers.TORAD;
        var strength = 10 * RollableClientConfig.CONFIG_UNIT.get().banking().rightingStrength();

        var cutoff = ROLL_REORIENT_CUTOFF;
        double rollDelta = 0;
        if (-cutoff < currentRoll && currentRoll < cutoff) {
            rollDelta = -Math.pow(currentRoll, 3) / 3.0 + currentRoll;
        }

        return rotationInstant.add(0, 0, -rollDelta * strength * delta);
    }

    public static RollContext.ConfiguresRotation fixNaN(String name) {
        return (rotationInstant, context) -> {
            if (Double.isNaN(rotationInstant.pitch())) {
                rotationInstant = new RotateState(0, rotationInstant.yaw(), rotationInstant.roll());
                LOGGER.warn("NaN found in pitch for {}, setting to 0 as fallback", name);
            }
            if (Double.isNaN(rotationInstant.yaw())) {
                rotationInstant = new RotateState(rotationInstant.pitch(), 0, rotationInstant.roll());
                LOGGER.warn("NaN found in yaw for {}, setting to 0 as fallback", name);
            }
            if (Double.isNaN(rotationInstant.roll())) {
                rotationInstant = new RotateState(rotationInstant.pitch(), rotationInstant.yaw(), 0);
                LOGGER.warn("NaN found in roll for {}, setting to 0 as fallback", name);
            }
            return rotationInstant;
        };
    }

    public static RotateState applyControlSurfaceEfficacy(RotateState rotationInstant, RollContext context) {
        var config = RollableClientConfig.CONFIG_UNIT.get();
        var elevatorExpression = config.advanced().elevatorEfficacyFormula().getCompiledOrDefaulting(1);
        var aileronExpression = config.advanced().aileronEfficacyFormula().getCompiledOrDefaulting(1);
        var rudderExpression = config.advanced().rudderEfficacyFormula().getCompiledOrDefaulting(1);

        var vars = getVars(context);
        return rotationInstant.multiply(elevatorExpression.eval(vars), rudderExpression.eval(vars), aileronExpression.eval(vars));
    }

    public static RotateState configureRotation(RotateState rotationInstant, @SuppressWarnings("unused") RollContext context) {
        var config = RollableClientConfig.CONFIG_UNIT.get();
        var pitch = rotationInstant.pitch();
        var yaw = rotationInstant.yaw();
        var roll = rotationInstant.roll();

        if (!config.generals().switchRollAndYaw()) {
            var temp = yaw;
            yaw = roll;
            roll = temp;
        }
        if (config.generals().invertPitch()) {
            pitch = -pitch;
        }

        return new RotateState(pitch, yaw, roll);
    }

    private static Map<String, Double> getVars(RollContext context) {
        var player = Minecraft.getInstance().player;
        assert player != null;

        var currentRotation = context.getCurrentRotation();
        var velocity = player.getDeltaMovement();
        var rotationVector = player.getLookAngle();
        var map = VAR_MAP.get();
        map.clear();
        map.put("pitch", currentRotation.pitch());
        map.put("yaw", currentRotation.yaw());
        map.put("roll", currentRotation.roll());
        map.put("velocity_length", velocity.length());
        map.put("velocity_x", velocity.x());
        map.put("velocity_y", velocity.y());
        map.put("velocity_z", velocity.z());
        map.put("look_x", rotationVector.x());
        map.put("look_y", rotationVector.y());
        map.put("look_z", rotationVector.z());
        return map;
    }
}
