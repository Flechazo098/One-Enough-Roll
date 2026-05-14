package com.flechazo.reallyradicalrolls.flight;

import cc.sighs.oelib.config.ConfigSchema;
import cc.sighs.oelib.config.ConfigMetaCodec;
import cc.sighs.oelib.config.field.ConfigField;
import org.jetbrains.annotations.NotNull;

public record RotateState(double pitch, double yaw, double roll) {

    public static ConfigMetaCodec<RotateState> metaCodec(double pitchDefault, double yawDefault, double rollDefault) {
        return ConfigSchema.metaCodec(RotateState.class, instance -> instance.group(
                ConfigField.doubleRange("pitch", 0, 20).defaultValue(pitchDefault).tooltip()
                        .forGetter(RotateState::pitch),
                ConfigField.doubleRange("yaw", 0, 20).defaultValue(yawDefault).tooltip()
                        .forGetter(RotateState::yaw),
                ConfigField.doubleRange("roll", 0, 20).defaultValue(rollDefault).tooltip()
                        .forGetter(RotateState::roll)
        ).apply(instance, RotateState::new));
    }
    public RotateState add(double pitch, double yaw, double roll) {
        return new RotateState(this.pitch + pitch, this.yaw + yaw, this.roll + roll);
    }

    public RotateState multiply(double pitch, double yaw, double roll) {
        return new RotateState(this.pitch * pitch, this.yaw * yaw, this.roll * roll);
    }

    public RotateState multiply(RotateState state) {
        return new RotateState(this.pitch * state.pitch(), this.yaw * state.yaw(), this.roll * state.roll());
    }

    public RotateState fixNaN() {
        return new RotateState(Double.isNaN(this.pitch) ? 0 : this.pitch, Double.isNaN(this.yaw) ? 0 : this.yaw, Double.isNaN(this.roll) ? 0 : this.roll);
    }

    public RotateState addAbsolute(double x, double y, double currentRoll) {
        double cos = Math.cos(currentRoll);
        double sin = Math.sin(currentRoll);
        return new RotateState(this.pitch - y * cos - x * sin, this.yaw - y * sin + x * cos, this.roll);
    }

    public RotateState applySensitivity(RotateState sensitivity) {
        return new RotateState(
                pitch * sensitivity.pitch(),
                yaw * sensitivity.yaw(),
                roll * sensitivity.roll()
        );
    }

    @Override
    public @NotNull String toString() {
        return "pitch=%.2f, yaw=%.2f, roll=%.2f".formatted(this.pitch, this.yaw, this.roll);
    }
}
