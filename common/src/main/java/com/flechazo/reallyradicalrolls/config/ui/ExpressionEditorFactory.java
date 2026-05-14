package com.flechazo.reallyradicalrolls.config.ui;

import cc.sighs.oelib.config.ui.ConfigWidgetRegistry;
import com.flechazo.reallyradicalrolls.expression.ExpressionParser;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class ExpressionEditorFactory implements ConfigWidgetRegistry.CustomWidgetFactory {
    @Override
    public ConfigWidgetRegistry.CustomWidgetHandle create(ConfigWidgetRegistry.CustomWidgetContext context) {
        String initialStr = context.currentValue() != null && context.currentValue().isJsonPrimitive()
                ? context.currentValue().getAsString()
                : "";

        ExpressionEditorWidget[] holder = new ExpressionEditorWidget[1];
        holder[0] = new ExpressionEditorWidget(
                context.screen(),
                Minecraft.getInstance().font,
                context.x(),
                context.y(),
                context.width(),
                context.meta().translationKey().map(Component::translatable).orElse(Component.literal(context.meta().key())),
                new ExpressionParser(initialStr),
                () -> {
                    context.working().addProperty(context.meta().key(), holder[0].getExpressionString());
                    context.onValueChanged().run();
                }
        );

        ExpressionEditorWidget w = holder[0];
        return new ConfigWidgetRegistry.CustomWidgetHandle() {
            @Override
            public AbstractWidget widget() {
                return w;
            }

            @Override
            public void reset(JsonElement value) {
                String resetValue = value != null && value.isJsonPrimitive() ? value.getAsString() : "";
                w.setExpressionString(resetValue);
            }
        };
    }
}
