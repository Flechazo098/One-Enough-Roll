package com.flechazo.reallyradicalrolls.config;

import cc.sighs.oelib.config.*;
import cc.sighs.oelib.config.field.ConfigField;
import cc.sighs.oelib.config.model.ConfigStorageFormat;
import cc.sighs.oelib.config.optics.ConfigLens;
import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.config.ui.ExpressionBuilder;
import com.flechazo.reallyradicalrolls.expression.ExpressionParser;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import net.minecraft.resources.Identifier;

import java.lang.invoke.MethodHandles;

public record RollableClientConfig(
        Generals generals,
        Thrust thrust,
        Activation activation,
        Swim swim,
        Banking banking,
        Sensitivity sensitivity,
        Advanced advanced
) {
    public static final ConfigUnit<RollableClientConfig> CONFIG_UNIT;
    public static final Lenses L;

    public record Generals(
            boolean enabled,
            boolean switchRollAndYaw,
            boolean invertPitch,
            boolean momentumBasedMouse,
            double momentumMouseDeadzone,
            boolean disableWhenSubmerged,
            boolean showHorizonLine,
            int horizonLineColor,
            boolean showMomentumWidget
    ) {}

    public record Thrust(
            boolean enabled,
            double maxThrust,
            double acceleration,
            double deceleration
    ) {}

    public enum ActivationMode {
        VANILLA, TRIPLE_JUMP, HYBRID, HYBRID_TOGGLE
    }

    public record Activation(
            ActivationMode mode,
            boolean hybridToggleAfterBarrelRoll
    ) {}

    public record Swim(
            boolean enabled,
            double strafeRollStrength,
            double strafeYawStrength,
            boolean smoothingEnabled,
            boolean strafeSmoothingEnabled,
            RotateState smoothingValues
    ) {}

    public record Banking(
            boolean enabled,
            double strength,
            boolean simulateControlSurfaceEfficacy,
            boolean automaticRighting,
            double rightingStrength
    ) {}

    public record Sensitivity(
            RotateState cameraSmoothing,
            RotateState desktop,
            RotateState controller
    ) {}

    public record Advanced(
            ExpressionParser bankingXFormula,
            ExpressionParser bankingYFormula,
            ExpressionParser elevatorEfficacyFormula,
            ExpressionParser aileronEfficacyFormula,
            ExpressionParser rudderEfficacyFormula
    ) {}

    public record Lenses(
            ConfigLens<RollableClientConfig, Boolean> elytraEnabled,
            ConfigLens<RollableClientConfig, Boolean> swimEnabled,
            ConfigLens<RollableClientConfig, Boolean> thrustEnabled
    ) {}

    static {
        var def = ConfigSchema.defineClient(
                Identifier.fromNamespaceAndPath(ReallyRadicalRolls.MOD_ID, "client"),
                RollableClientConfig.class,
                meta -> meta.fileName("really_radical_rolls_client").format(ConfigStorageFormat.TOML),
                instance -> instance.group(
                        ConfigSchema.record("generals", Generals.class,
                                g -> g.group(
                                        ConfigField.bool("enabled").defaultValue(true).tooltip().forGetter(Generals::enabled),
                                        ConfigField.bool("switchRollAndYaw").defaultValue(false).tooltip().forGetter(Generals::switchRollAndYaw),
                                        ConfigField.bool("invertPitch").defaultValue(false).tooltip().forGetter(Generals::invertPitch),
                                        ConfigField.bool("momentumBasedMouse").defaultValue(false).tooltip().forGetter(Generals::momentumBasedMouse),
                                        ConfigField.doubleRange("momentumMouseDeadzone", 0, Integer.MAX_VALUE).defaultValue(0.2).tooltip().forGetter(Generals::momentumMouseDeadzone),
                                        ConfigField.bool("disableWhenSubmerged").defaultValue(true).tooltip().forGetter(Generals::disableWhenSubmerged),
                                        ConfigField.bool("showHorizonLine").defaultValue(true).tooltip().forGetter(Generals::showHorizonLine),
                                        ConfigField.intRange("horizonLineColor", 0, 0xFFFFFF).defaultValue(0x33CCFF).tooltip().text().forGetter(Generals::horizonLineColor),
                                        ConfigField.bool("showMomentumWidget").defaultValue(true).tooltip().forGetter(Generals::showMomentumWidget)
                                ).apply(g, Generals::new),
                                RollableClientConfig::generals),

                        ConfigSchema.record("thrust", Thrust.class,
                                t -> t.group(
                                        ConfigField.bool("enabled").defaultValue(false).tooltip().forGetter(Thrust::enabled),
                                        ConfigField.doubleRange("maxThrust", 0, Integer.MAX_VALUE).defaultValue(2.0).tooltip().forGetter(Thrust::maxThrust),
                                        ConfigField.doubleRange("acceleration", 0, Integer.MAX_VALUE).defaultValue(0.05).tooltip().forGetter(Thrust::acceleration),
                                        ConfigField.doubleRange("deceleration", 0, Integer.MAX_VALUE).defaultValue(0.02).tooltip().forGetter(Thrust::deceleration)
                                ).apply(t, Thrust::new),
                                RollableClientConfig::thrust),

                        ConfigSchema.record("activation", Activation.class,
                                a -> a.group(
                                        ConfigField.enumValue("mode", ActivationMode.class).defaultValue(ActivationMode.VANILLA).tooltip().forGetter(Activation::mode),
                                        ConfigField.bool("hybridToggleAfterBarrelRoll").defaultValue(true).tooltip().forGetter(Activation::hybridToggleAfterBarrelRoll)
                                ).apply(a, Activation::new),
                                RollableClientConfig::activation),

                        ConfigSchema.record("swim", Swim.class,
                                s -> s.group(
                                        ConfigField.bool("enabled").defaultValue(true).tooltip().forGetter(Swim::enabled),
                                        ConfigField.doubleRange("strafeRollStrength", 0, Integer.MAX_VALUE).defaultValue(2.5).tooltip().forGetter(Swim::strafeRollStrength),
                                        ConfigField.doubleRange("strafeYawStrength", 0, Integer.MAX_VALUE).defaultValue(1.0).tooltip().forGetter(Swim::strafeYawStrength),
                                        ConfigField.bool("smoothingEnabled").defaultValue(true).tooltip().forGetter(Swim::smoothingEnabled),
                                        ConfigField.bool("strafeSmoothingEnabled").defaultValue(true).tooltip().forGetter(Swim::strafeSmoothingEnabled),
                                        ConfigSchema.record("smoothingValues", RotateState.class,
                                                RotateState.metaCodec(0.5, 0.5, 0.5),
                                                Swim::smoothingValues)
                                ).apply(s, Swim::new),
                                RollableClientConfig::swim),

                        ConfigSchema.record("banking", Banking.class,
                                b -> b.group(
                                        ConfigField.bool("enabled").defaultValue(true).tooltip().forGetter(Banking::enabled),
                                        ConfigField.doubleRange("strength", 0, Integer.MAX_VALUE).defaultValue(20.0).tooltip().forGetter(Banking::strength),
                                        ConfigField.bool("simulateControlSurfaceEfficacy").defaultValue(false).tooltip().forGetter(Banking::simulateControlSurfaceEfficacy),
                                        ConfigField.bool("automaticRighting").defaultValue(false).tooltip().forGetter(Banking::automaticRighting),
                                        ConfigField.doubleRange("rightingStrength", 0, Integer.MAX_VALUE).defaultValue(50.0).tooltip().forGetter(Banking::rightingStrength)
                                ).apply(b, Banking::new),
                                RollableClientConfig::banking),

                        ConfigSchema.record("sensitivity", Sensitivity.class,
                                se -> se.group(
                                        ConfigSchema.record("cameraSmoothing", RotateState.class,
                                                RotateState.metaCodec(1.0, 2.5, 1.0),
                                                Sensitivity::cameraSmoothing),
                                        ConfigSchema.record("desktop", RotateState.class,
                                                RotateState.metaCodec(1.0, 0.4, 1.0),
                                                Sensitivity::desktop),
                                        ConfigSchema.record("controller", RotateState.class,
                                                RotateState.metaCodec(1.0, 0.6, 1.0),
                                                Sensitivity::controller)
                                ).apply(se, Sensitivity::new),
                                RollableClientConfig::sensitivity),

                        ConfigSchema.record("advanced", Advanced.class,
                                a -> a.group(
                                        new ExpressionBuilder("bankingXFormula")
                                                .defaultValue(new ExpressionParser("sin($roll * TO_RAD) * cos($pitch * TO_RAD) * 10 * $banking_strength"))
                                                .tooltip()
                                                .forGetter(Advanced::bankingXFormula),
                                        new ExpressionBuilder("bankingYFormula")
                                                .defaultValue(new ExpressionParser("(-1 + cos($roll * TO_RAD)) * cos($pitch * TO_RAD) * 10 * $banking_strength"))
                                                .tooltip()
                                                .forGetter(Advanced::bankingYFormula),
                                        new ExpressionBuilder("elevatorEfficacyFormula")
                                                .defaultValue(new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z"))
                                                .tooltip()
                                                .forGetter(Advanced::elevatorEfficacyFormula),
                                        new ExpressionBuilder("aileronEfficacyFormula")
                                                .defaultValue(new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z"))
                                                .tooltip()
                                                .forGetter(Advanced::aileronEfficacyFormula),
                                        new ExpressionBuilder("rudderEfficacyFormula")
                                                .defaultValue(new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z"))
                                                .tooltip()
                                                .forGetter(Advanced::rudderEfficacyFormula)
                                ).apply(a, Advanced::new),
                                RollableClientConfig::advanced)
                ).apply(instance, RollableClientConfig::new)
        );

        CONFIG_UNIT = def.unit();
        var lookup = MethodHandles.lookup();
        L = new Lenses(
                def.lens(lookup, RollableClientConfig::generals)
                        .compose(RecordLensBuilder.lens(lookup, Generals.class, "enabled")),
                def.lens(lookup, RollableClientConfig::swim)
                        .compose(RecordLensBuilder.lens(lookup, Swim.class, "enabled")),
                def.lens(lookup, RollableClientConfig::thrust)
                        .compose(RecordLensBuilder.lens(lookup, Thrust.class, "enabled"))
        );
    }

    public static void register() {
        ConfigManager.registerClient(CONFIG_UNIT);
    }

    public static boolean toggleElytraEnabled() {
        boolean current = L.elytraEnabled().view(CONFIG_UNIT.get());
        return ConfigUnitOps.setAndGet(CONFIG_UNIT, L.elytraEnabled(), !current);
    }

    public static boolean toggleSwimEnabled() {
        boolean current = L.swimEnabled().view(CONFIG_UNIT.get());
        return ConfigUnitOps.setAndGet(CONFIG_UNIT, L.swimEnabled(), !current);
    }

    public static boolean toggleThrustEnabled() {
        boolean current = L.thrustEnabled().view(CONFIG_UNIT.get());
        return ConfigUnitOps.setAndGet(CONFIG_UNIT, L.thrustEnabled(), !current);
    }
}
