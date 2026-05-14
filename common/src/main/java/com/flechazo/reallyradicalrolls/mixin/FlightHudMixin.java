package com.flechazo.reallyradicalrolls.mixin;

import com.flechazo.reallyradicalrolls.api.MouseHandlerAccessor;
import com.flechazo.reallyradicalrolls.api.RollableEntity;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.render.HorizonLineWidget;
import com.flechazo.reallyradicalrolls.render.MomentumCrosshairWidget;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Gui.class)
public abstract class FlightHudMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "extractCrosshair",
            at = @At("HEAD")
    )
    private void rollable$renderAdditionalCrosshairComponents(GuiGraphicsExtractor context, DeltaTracker tickCounter,
                                                               CallbackInfo ci, @Share("crosshair_offset") LocalRef<Vector2i> crosshairOffset) {
        LocalPlayer player = this.minecraft.player;
        if (!(player instanceof RollableEntity rollable) || !rollable.rollable$isRolling())
            return;

        var tickDelta = tickCounter.getRealtimeDeltaTicks();
        RollableClientConfig config = RollableClientConfig.CONFIG_UNIT.get();

        if (config.generals().showHorizonLine()) {
            HorizonLineWidget.render(context, context.guiWidth(), context.guiHeight(),
                    rollable.rollable$getRoll(tickDelta), player.getViewXRot(tickDelta));
        }

        if (config.generals().momentumBasedMouse() && config.generals().showMomentumWidget()) {
            var handler = this.minecraft.mouseHandler;
            if (handler instanceof MouseHandlerAccessor accessor) {
                var vec = accessor.rollable$getMouseTurnVec();
                crosshairOffset.set(MomentumCrosshairWidget.render(context, context.guiWidth(), context.guiHeight(),
                        new Vector2d(vec)));
            }
        }
    }

    @ModifyArgs(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private void rollable$moveCrosshair(Args args, @Share("crosshair_offset") LocalRef<Vector2i> crosshairOffset) {
        var offset = crosshairOffset.get();
        if (offset != null) {
            args.set(2, (int) args.get(2) + offset.x);
            args.set(3, (int) args.get(3) + offset.y);
        }
    }

    @ModifyArgs(
            method = "extractCrosshair",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 1
            )
    )
    private void rollable$moveCrosshair2(Args args, @Share("crosshair_offset") LocalRef<Vector2i> crosshairOffset) {
        var offset = crosshairOffset.get();
        if (offset != null) {
            args.set(2, (int) args.get(2) + offset.x);
            args.set(3, (int) args.get(3) + offset.y);
        }
    }
}
