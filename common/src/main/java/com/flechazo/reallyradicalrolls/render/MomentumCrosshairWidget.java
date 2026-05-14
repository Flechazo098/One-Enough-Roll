package com.flechazo.reallyradicalrolls.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class MomentumCrosshairWidget {
    public static Vector2i render(GuiGraphicsExtractor context, int scaledWidth, int scaledHeight, Vector2d mouseTurnVec) {
        int centerX = scaledWidth / 2;
        int centerY = scaledHeight / 2 - 1;

        // Deep-copy to avoid mutating the caller's shared vector
        var vec = new Vector2d(mouseTurnVec).mul(50);
        var lineVec = new Vector2d(vec);
        var norm = new Vector2d(vec).negate().normalize();
        var tipBase = new Vector2d(vec).add(norm.mul(Math.min(vec.length(), 10f), new Vector2d()));

        if (vec.lengthSquared() > 10f * 10f && !vec.equals(0, 0)) {
            float angle = (float) Math.atan2(lineVec.y, lineVec.x);
            float lineLen = (float) lineVec.length();
            float tipLen = (float) tipBase.length();

            var pose = context.pose();
            pose.pushMatrix();
            pose.translate(centerX, centerY);
            pose.rotate(angle);

            int color = 0x80FFFFFF;
            float halfThick = 0.5f;

            // Main line from tipBase to tip (the indicator arm)
            if (lineLen > tipLen + 1f) {
                context.fill(RenderPipelines.GUI, (int)tipLen, (int)-halfThick, (int)lineLen, 0, color);
            }

            // Crossbar at the tip
            float crossLen = 3f;
            context.fill(RenderPipelines.GUI, (int)(lineLen - crossLen), (int)-halfThick, (int)lineLen, 0, color);

            pose.popMatrix();
        }

        return new Vector2i((int) vec.x, (int) vec.y);
    }
}
