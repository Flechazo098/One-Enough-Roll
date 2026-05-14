package com.flechazo.reallyradicalrolls.render;

import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.flight.MagicNumbers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class HorizonLineWidget {
    private static double smoothRoll;
    private static double smoothPitch;
    private static long lastFrameTime;
    private static boolean initialized;

    public static void render(GuiGraphicsExtractor context, int scaledWidth, int scaledHeight, double roll, double pitch) {
        int color = RollableClientConfig.CONFIG_UNIT.get().generals().horizonLineColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        long now = System.nanoTime();
        if (!initialized) {
            smoothRoll = roll;
            smoothPitch = pitch;
            lastFrameTime = now;
            initialized = true;
        }

        double dt = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;
        double lerpFactor = 1.0 - Math.exp(-12.0 * dt);
        smoothRoll = smoothRoll + (roll - smoothRoll) * lerpFactor;
        smoothPitch = smoothPitch + (pitch - smoothPitch) * lerpFactor;

        float cx = scaledWidth / 2f;
        float cy = scaledHeight / 2f;
        float rollRad = (float)(-smoothRoll * MagicNumbers.TORAD);
        float pitchOff = (float)(smoothPitch * scaledHeight * 0.007);

        var pose = context.pose();
        pose.pushMatrix();
        pose.translate(cx + (float)(Math.cos(rollRad + Math.PI/2) * pitchOff),
                       cy + (float)(Math.sin(rollRad + Math.PI/2) * pitchOff));
        pose.rotate(rollRad);

        // Draw two line segments with a gap in the middle
        // Semi-transparent, brighter at the tips
        int alpha = 0xC0;
        int c = (alpha << 24) | (r << 16) | (g << 8) | b;
        float halfThickness = 1.0f;

        // Left segment (from gap to outer)
        context.fill(RenderPipelines.GUI, -50, (int)-halfThickness, -10, (int)halfThickness, c);
        // Right segment (from gap to outer)
        context.fill(RenderPipelines.GUI, 10, (int)-halfThickness, 50, (int)halfThickness, c);

        pose.popMatrix();
    }
}
