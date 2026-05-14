package com.flechazo.reallyradicalrolls.compat.controlify;

import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.ModKeybindings;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.event.ClientEvents;
import com.flechazo.reallyradicalrolls.flight.RollContext;
import com.flechazo.reallyradicalrolls.flight.RotateState;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.BindContext;
import net.minecraft.network.chat.Component;

public class ControlifyCompat implements ControlifyEntrypoint {
    public static final BindContext FALL_FLYING = new BindContext(
            ReallyRadicalRolls.id("fall_flying"),
            mc -> ClientEvents.isFallFlying()
    );

    public static InputBindingSupplier PITCH_UP;
    public static InputBindingSupplier PITCH_DOWN;
    public static InputBindingSupplier ROLL_LEFT;
    public static InputBindingSupplier ROLL_RIGHT;
    public static InputBindingSupplier YAW_LEFT;
    public static InputBindingSupplier YAW_RIGHT;
    public static InputBindingSupplier THRUST_FORWARD;
    public static InputBindingSupplier THRUST_BACKWARD;

    private RotateState applyToRotation(RotateState rotationDelta, RollContext context) {
        var perhapsController = ControlifyApi.get().getCurrentController();
        if (perhapsController.isPresent()) {
            var controller = perhapsController.get();
            var sensitivity = RollableClientConfig.CONFIG_UNIT.get().sensitivity().controller();

            if (PITCH_UP.on(controller) == null) return rotationDelta;

            double multiplier = context.getRenderDelta() * 1200;

            double pitchAxis = PITCH_DOWN.on(controller).analogueNow() - PITCH_UP.on(controller).analogueNow();
            double yawAxis = YAW_RIGHT.on(controller).analogueNow() - YAW_LEFT.on(controller).analogueNow();
            double rollAxis = ROLL_RIGHT.on(controller).analogueNow() - ROLL_LEFT.on(controller).analogueNow();

            pitchAxis *= multiplier * sensitivity.pitch();
            yawAxis *= multiplier * sensitivity.yaw();
            rollAxis *= multiplier * sensitivity.roll();

            return rotationDelta.add(pitchAxis, yawAxis, rollAxis);
        }

        return rotationDelta;
    }

    public static double getThrustModifier() {
        var opt = ControlifyApi.get().getCurrentController();
        if (opt.isEmpty()) return 0;
        var controller = opt.get();

        float forward = THRUST_FORWARD.on(controller).analogueNow();
        float backward = THRUST_BACKWARD.on(controller).analogueNow();
        return forward - backward;
    }

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        var bindings = context.bindings();
        bindings.registerBindContext(FALL_FLYING);

        PITCH_UP = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("pitch_up"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.pitch_up"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.PITCH_UP)
        );
        PITCH_DOWN = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("pitch_down"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.pitch_down"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.PITCH_DOWN)
        );
        ROLL_LEFT = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("roll_left"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.roll_left"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.ROLL_LEFT)
        );
        ROLL_RIGHT = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("roll_right"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.roll_right"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.ROLL_RIGHT)
        );
        YAW_LEFT = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("yaw_left"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.yaw_left"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.YAW_LEFT)
        );
        YAW_RIGHT = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("yaw_right"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.yaw_right"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.YAW_RIGHT)
        );
        THRUST_FORWARD = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("thrust_forward"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.thrust_forward"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.THRUST_FORWARD)
        );
        THRUST_BACKWARD = bindings.registerBinding(builder -> builder
                .id(ReallyRadicalRolls.id("thrust_backward"))
                .category(Component.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Component.translatable("controlify.bind.do_a_barrel_roll.thrust_backward"))
                .allowedContexts(FALL_FLYING, BindContext.IN_GAME)
                .addKeyCorrelation(ModKeybindings.THRUST_BACKWARD)
        );

        ClientEvents.ELYTRA_GROUP.registerAfterModifier(ctx -> ctx
                .useModifier(this::applyToRotation));

        ControlifyEvents.LOOK_INPUT_MODIFIER.register(event -> {
            if (ClientEvents.isFallFlying()) {
                event.lookInput().zero();
            }
        });
    }

    @Override
    public void onControlifyInit(InitContext context) {
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlifyApi) {
    }
}
