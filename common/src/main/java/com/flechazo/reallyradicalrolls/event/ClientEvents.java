package com.flechazo.reallyradicalrolls.event;

import cc.sighs.oelib.event.EventBus;
import cc.sighs.oelib.event.Subscribe;
import cc.sighs.oelib.event.events.ClientTickEvent;
import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.ModKeybindings;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig.ActivationMode;
import com.flechazo.reallyradicalrolls.config.ServerConfig;
import com.flechazo.reallyradicalrolls.util.MixinHooks;
import com.flechazo.reallyradicalrolls.flight.RollProcessGroup;
import com.flechazo.reallyradicalrolls.flight.modifier.RotationModifiers;
import com.flechazo.reallyradicalrolls.flight.modifier.StrafeRollModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.SmoothDouble;

public class ClientEvents {
    public static final RollProcessGroup ELYTRA_GROUP = RollProcessGroup.get(ReallyRadicalRolls.id("elytra"));
    private static final RollProcessGroup SWIMMING_GROUP = RollProcessGroup.get(ReallyRadicalRolls.id("swimming"));
    private static final SmoothDouble PITCH_SMOOTHER = new SmoothDouble();
    private static final SmoothDouble YAW_SMOOTHER = new SmoothDouble();
    private static final SmoothDouble ROLL_SMOOTHER = new SmoothDouble();
    private static final SmoothDouble SWIM_PITCH = new SmoothDouble();
    private static final SmoothDouble SWIM_YAW = new SmoothDouble();
    private static final SmoothDouble SWIM_ROLL = new SmoothDouble();

    public static void registerEvents() {
        // Enable predicates
        ELYTRA_GROUP.trueIf(ClientEvents::isFallFlying);
        SWIMMING_GROUP.trueIf(ClientEvents::isSwimming);

        // EARLY_CAMERA_MODIFIERS (applied before sensitivity)
        ELYTRA_GROUP.registerBeforeModifier(context -> context
                .useModifier(RotationModifiers.buttonControls(1800)));
        ELYTRA_GROUP.registerBeforeModifier(context -> context
                .useModifier(RotationModifiers::configureRotation));

        // LATE_CAMERA_MODIFIERS (applied after sensitivity)
        ELYTRA_GROUP.registerAfterModifier(context -> context
                .useModifier(RotationModifiers::applyControlSurfaceEfficacy, () ->
                        RollableClientConfig.CONFIG_UNIT.get().banking().simulateControlSurfaceEfficacy())
                .useModifier(RotationModifiers.smoothing(
                        PITCH_SMOOTHER, YAW_SMOOTHER, ROLL_SMOOTHER,
                        RollableClientConfig.CONFIG_UNIT.get().sensitivity().cameraSmoothing()
                ))
                .useModifier(RotationModifiers::banking, () ->
                        RollableClientConfig.CONFIG_UNIT.get().banking().enabled())
                .useModifier(RotationModifiers::reorient, () ->
                        RollableClientConfig.CONFIG_UNIT.get().banking().automaticRighting()));

        // Swimming modifiers
        SWIMMING_GROUP.registerBeforeModifier(context -> context
                .useModifier(StrafeRollModifiers::applyStrafeRoll));
        SWIMMING_GROUP.registerBeforeModifier(context -> context
                .useModifier(RotationModifiers::configureRotation));
        SWIMMING_GROUP.registerAfterModifier(context -> context
                .useModifier(RotationModifiers.smoothing(
                        SWIM_PITCH, SWIM_YAW, SWIM_ROLL,
                        RollableClientConfig.CONFIG_UNIT.get().swim().smoothingValues())));

        // EventBus handlers for active group determination
        EventBus.register(new ElytraActiveHandler());
        EventBus.register(new SwimActiveHandler());
    }

    @Subscribe
    public static void clientTick(ClientTickEvent event) {
        var mc = Minecraft.getInstance();
        if (!isFallFlying()) clearElytra();
        if (!isSwimming()) clearSwim();
        ModKeybindings.clientTick(mc);
    }

    public static boolean isFallFlying() {
        var serverConfig = ServerConfig.CONFIG_UNIT.get();
        if (!serverConfig.forceEnabled()) {
            var clientConfig = RollableClientConfig.CONFIG_UNIT.get();
            if (!clientConfig.generals().enabled()) return false;

            ActivationMode mode = clientConfig.activation().mode();
            if ((mode == ActivationMode.HYBRID || mode == ActivationMode.HYBRID_TOGGLE)
                    && !MixinHooks.thirdJump) {
                return false;
            }
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        var clientConfig = RollableClientConfig.CONFIG_UNIT.get();
        if (clientConfig.generals().disableWhenSubmerged() && player.isUnderWater()) return false;
        return player.isFallFlying();
    }

    public static boolean isSwimming() {
        LocalPlayer player = Minecraft.getInstance().player;
        return RollableClientConfig.CONFIG_UNIT.get().swim().enabled() && player != null
                && player.isSwimming() && player.isUnderWater();
    }

    private static void clearElytra() {
        PITCH_SMOOTHER.reset();
        YAW_SMOOTHER.reset();
        ROLL_SMOOTHER.reset();
    }

    private static void clearSwim() {
        SWIM_PITCH.reset();
        SWIM_YAW.reset();
        SWIM_ROLL.reset();
    }

    // ---- OELib EventBus handlers ----

    static class ElytraActiveHandler {
        @Subscribe
        public void onActiveGroupCheck(RollActiveGroupEvent event) {
            if (isFallFlying()) event.setActiveGroup(ELYTRA_GROUP);
        }
    }

    static class SwimActiveHandler {
        @Subscribe
        public void onActiveGroupCheck(RollActiveGroupEvent event) {
            if (isSwimming()) event.setActiveGroup(SWIMMING_GROUP);
        }
    }
}
