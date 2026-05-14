package com.flechazo.reallyradicalrolls;

import cc.sighs.oelib.config.ui.ConfigWidgetRegistry;
import com.flechazo.reallyradicalrolls.compat.debug.RollDebugEntry;
import com.flechazo.reallyradicalrolls.config.RollableClientConfig;
import com.flechazo.reallyradicalrolls.config.ui.ExpressionEditorFactory;
import com.flechazo.reallyradicalrolls.config.ui.ExpressionEditorWidget;
import com.flechazo.reallyradicalrolls.event.ClientEvents;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public class ReallyRadicalRollsClient {
    public static void init() {
        ConfigWidgetRegistry.register(ExpressionEditorWidget.WIDGET_ID, new ExpressionEditorFactory());
        RollableClientConfig.register();
        ModKeybindings.registerAll();
        ClientEvents.registerEvents();
        DebugScreenEntries.register(RollDebugEntry.ID, new RollDebugEntry());
    }
}
