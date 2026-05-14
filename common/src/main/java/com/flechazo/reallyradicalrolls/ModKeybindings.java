package com.flechazo.reallyradicalrolls;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import cc.sighs.oelib.registry.extra.KeyMappingRegister;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.config.ServerConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ModKeybindings {
    private static final KeyMapping.Category CATEGORY_MAIN = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(ReallyRadicalRolls.MOD_ID, ReallyRadicalRolls.MOD_ID));

    private static String format(String key) {
        return "key.%s.%s".formatted(ReallyRadicalRolls.MOD_ID, key);
    }

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(format("open_config"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping TOGGLE_ENABLE_ELYTRA = new KeyMapping(format("toggle_enable_elytra"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY_MAIN);
    public static final KeyMapping TOGGLE_ENABLE_SWIMMING = new KeyMapping(format("toggle_enable_swimming"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, CATEGORY_MAIN);
    public static final KeyMapping TOGGLE_THRUST = new KeyMapping(format("toggle_thrust"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping PITCH_UP = new KeyMapping(format("pitch_up"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping PITCH_DOWN = new KeyMapping(format("pitch_down"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping YAW_LEFT = new KeyMapping(format("yaw_left"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping YAW_RIGHT = new KeyMapping(format("yaw_right"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);
    public static final KeyMapping ROLL_LEFT = new KeyMapping(format("roll_left"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_A, CATEGORY_MAIN);
    public static final KeyMapping ROLL_RIGHT = new KeyMapping(format("roll_right"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_D, CATEGORY_MAIN);
    public static final KeyMapping THRUST_FORWARD = new KeyMapping(format("thrust_forward"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_W, CATEGORY_MAIN);
    public static final KeyMapping THRUST_BACKWARD = new KeyMapping(format("thrust_backward"), InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY_MAIN);

    public static final List<KeyMapping> ALL = List.of(
            OPEN_CONFIG,
            TOGGLE_ENABLE_ELYTRA,
            TOGGLE_ENABLE_SWIMMING,
            TOGGLE_THRUST,
            PITCH_UP,
            PITCH_DOWN,
            YAW_LEFT,
            YAW_RIGHT,
            ROLL_LEFT,
            ROLL_RIGHT,
            THRUST_FORWARD,
            THRUST_BACKWARD
    );

    public static void registerAll() {
        for (KeyMapping mapping : ALL) {
            KeyMappingRegister.register(mapping);
        }
    }

    public static void clientTick(Minecraft client) {
        while (OPEN_CONFIG.consumeClick())
            client.setScreen(new ConfigScreen(client.screen, ReallyRadicalRolls.MOD_ID));
        while (TOGGLE_ENABLE_ELYTRA.consumeClick()) {
            boolean b = RollableClientConfig.toggleElytraEnabled();
            if (client.player != null)
                client.player.sendSystemMessage(Component.translatable("key.%s.%s".formatted(ReallyRadicalRolls.MOD_ID, b ? "toggle_enabled.enable" : "toggle_enabled.disable")));
        }

        while (TOGGLE_ENABLE_SWIMMING.consumeClick()) {
            boolean enable = RollableClientConfig.toggleSwimEnabled();
            if (client.player != null)
                client.player.sendSystemMessage(Component.translatable("key.rolling_down_in_the_deep." + (enable ? "toggle_enabled.enable" : "toggle_enabled.disable")));
        }
        while (TOGGLE_THRUST.consumeClick()) {
            if (!ServerConfig.CONFIG_UNIT.get().allowThrusting()) {
                if (client.player != null)
                    client.player.sendSystemMessage(Component.translatable("key.%s.toggle_thrust.disallowed".formatted(ReallyRadicalRolls.MOD_ID)));
            } else {
                boolean b = RollableClientConfig.toggleThrustEnabled();
                if (client.player != null)
                    client.player.sendSystemMessage(Component.translatable("key.%s.%s".formatted(ReallyRadicalRolls.MOD_ID, b ? "toggle_thrust.enable" : "toggle_thrust.disable")));
            }
        }
    }
}
