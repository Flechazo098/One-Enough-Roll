package com.flechazo.reallyradicalrolls.config.ui;

import cc.sighs.oelib.config.field.BaseFieldBuilder;
import cc.sighs.oelib.config.ui.ConfigUiHint;
import com.flechazo.reallyradicalrolls.expression.ExpressionParser;
import com.flechazo.reallyradicalrolls.platform.PlatformHelper;
import com.google.gson.JsonObject;

import java.util.ServiceLoader;

public class ExpressionBuilder extends BaseFieldBuilder<ExpressionParser, ExpressionBuilder> {
    private static final boolean IS_CLIENT;

    static {
        boolean client = false;
        ServiceLoader<PlatformHelper> loader = ServiceLoader.load(PlatformHelper.class);
        for (PlatformHelper helper : loader) {
            client = helper.isClient();
            break;
        }
        IS_CLIENT = client;
    }

    public ExpressionBuilder(String key) {
        super(key, ExpressionParser.CODEC);
        if (IS_CLIENT) {
            this.beforeMetaHook = metaBuilder ->
                    metaBuilder.uiHint(new ConfigUiHint.Custom(ExpressionEditorWidget.WIDGET_ID, new JsonObject()));
        }
    }
}
