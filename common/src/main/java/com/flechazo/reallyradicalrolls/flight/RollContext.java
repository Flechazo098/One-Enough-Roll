package com.flechazo.reallyradicalrolls.flight;

import java.util.function.BooleanSupplier;

public class RollContext {
    private final RotateState currentRotation;
    private RotateState rotationDelta;
    private final double renderDelta;

    public static RollContext of(RotateState currentRotation, RotateState rotationDelta, double delta) {
        return new RollContext(currentRotation, rotationDelta, delta);
    }

    public RollContext(RotateState currentRotation, RotateState rotationDelta, double renderDelta) {
        this.currentRotation = currentRotation.fixNaN();
        this.rotationDelta = rotationDelta.fixNaN();
        this.renderDelta = renderDelta;
    }

    public RollContext useModifier(ConfiguresRotation modifier, BooleanSupplier condition) {
        if (condition.getAsBoolean()) this.rotationDelta = modifier.apply(this.rotationDelta, this).fixNaN();
        return this;
    }

    public RollContext useModifier(ConfiguresRotation modifier) {
        return this.useModifier(modifier, () -> true);
    }

    public RotateState getCurrentRotation() {
        return this.currentRotation;
    }

    public RotateState getRotationDelta() {
        return this.rotationDelta;
    }

    public double getRenderDelta() {
        return this.renderDelta;
    }

    @FunctionalInterface
    public interface ConfiguresRotation {
        RotateState apply(RotateState rotationInstant, RollContext context);
    }
}
