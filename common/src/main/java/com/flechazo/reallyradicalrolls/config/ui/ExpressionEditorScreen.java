package com.flechazo.reallyradicalrolls.config.ui;

import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.expression.ExpressionParser;
import com.flechazo.reallyradicalrolls.expression.SyntaxHighlighter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExpressionEditorScreen extends Screen {
    private static final int OUTER_MARGIN = 16;
    private static final int HEADER_HEIGHT = 28;
    private static final int EDITOR_HEIGHT = 20;

    private final Screen parent;
    private final Component fieldTitle;
    private final Consumer<String> onSave;
    private final String initialValue;

    private EditBox expressionBox;
    private Button saveButton;
    private Button cancelButton;
    private boolean hasError;
    private String errorMessage;

    public ExpressionEditorScreen(Screen parent, Component fieldTitle, String initialValue, Consumer<String> onSave) {
        super(Component.translatable("config.%s.expression.editor.title".formatted(ReallyRadicalRolls.MOD_ID)));
        this.parent = parent;
        this.fieldTitle = fieldTitle;
        this.initialValue = initialValue == null ? "" : initialValue;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int panelX = OUTER_MARGIN;
        int panelW = this.width - OUTER_MARGIN * 2;
        int inputY = HEADER_HEIGHT + 18;

        this.expressionBox = new EditBox(this.font, panelX + 8, inputY, panelW - 16, EDITOR_HEIGHT, Component.empty());
        this.expressionBox.setMaxLength(256);
        this.expressionBox.setValue(this.initialValue);
        this.expressionBox.setResponder(this::updateValidation);
        this.expressionBox.addFormatter((text, offset) -> SyntaxHighlighter.highlightText(text).getVisualOrderText());
        this.addRenderableWidget(this.expressionBox);
        this.setInitialFocus(this.expressionBox);

        int buttonY = this.height - 28;
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.onClose())
                .bounds(this.width / 2 - 124, buttonY, 120, 20)
                .build());
        this.saveButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.saveAndClose())
                .bounds(this.width / 2 + 4, buttonY, 120, 20)
                .build());

        this.updateValidation(this.initialValue);
    }

    private void updateValidation(String value) {
        ExpressionParser parser = new ExpressionParser(value == null ? "" : value);
        this.hasError = parser.hasError();
        this.errorMessage = this.hasError && parser.getError() != null
                ? parser.getError().getMessage()
                : null;
    }

    private void saveAndClose() {
        this.onSave.accept(this.expressionBox.getValue());
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            this.saveAndClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(gui);

        int panelX = OUTER_MARGIN;
        int panelW = this.width - OUTER_MARGIN * 2;
        int panelTop = HEADER_HEIGHT;
        int panelBottom = this.height - 36;

        gui.fill(panelX, panelTop, panelX + panelW, panelBottom, 0x3A000000);
        gui.outline(panelX, panelTop, panelW, panelBottom - panelTop, 0x90FFFFFF);

        gui.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        gui.text(this.font, this.fieldTitle.copy().withStyle(Style.EMPTY.withColor(0xFFD7D7D7)), panelX + 8, panelTop + 6, 0xFFFFFFFF);

        super.extractRenderState(gui, mouseX, mouseY, partialTick);

        int statusY = this.expressionBox.getY() + EDITOR_HEIGHT + 6;
        if (this.hasError && this.errorMessage != null) {
            gui.text(
                    this.font,
                    Component.translatable("config.%s.expression.error".formatted(ReallyRadicalRolls.MOD_ID), this.errorMessage)
                            .withStyle(Style.EMPTY.withColor(0xFF5555)),
                    panelX + 8,
                    statusY,
                    0xFFFFFFFF
            );
        } else {
            gui.text(
                    this.font,
                    Component.translatable("config.%s.expression.valid".formatted(ReallyRadicalRolls.MOD_ID))
                            .withStyle(Style.EMPTY.withColor(0x55FF55)),
                    panelX + 8,
                    statusY,
                    0xFFFFFFFF
            );
        }

        int previewY = statusY + this.font.lineHeight + 4;
        gui.text(
                this.font,
                Component.translatable("config.%s.expression.preview_note".formatted(ReallyRadicalRolls.MOD_ID))
                        .withStyle(Style.EMPTY.withColor(0xFFB0B0B0)),
                panelX + 8,
                previewY,
                0xFFFFFFFF
        );
        previewY += this.font.lineHeight + 2;

        String raw = this.expressionBox.getValue();
        if (!raw.isEmpty()) {
            SyntaxHighlighter.renderHighlighted(gui, this.font, raw, panelX + 8, previewY, 0xFFFFFFFF);
        }

        int docsTop = previewY + this.font.lineHeight + 10;
        int docsBottom = panelBottom - 8;
        drawDocumentation(gui, panelX + 8, docsTop, panelW - 16, Math.max(0, docsBottom - docsTop));
    }

    private void drawDocumentation(GuiGraphicsExtractor gui, int x, int y, int width, int maxHeight) {
        if (maxHeight <= 0) {
            return;
        }

        gui.fill(x - 4, y - 4, x + width + 4, y + maxHeight, 0x1EFFFFFF);
        gui.outline(x - 4, y - 4, width + 8, maxHeight + 4, 0x40FFFFFF);

        List<Component> docs = new ArrayList<>();
        String prefix = "config.%s.expression".formatted(ReallyRadicalRolls.MOD_ID);
        docs.add(Component.translatable(prefix + ".documentation"));
        docs.add(Component.translatable(prefix + ".usage"));
        docs.add(Component.literal(""));
        docs.add(Component.translatable(prefix + ".vars").withStyle(Style.EMPTY.withColor(0xFFAA00)));
        docs.add(Component.translatable(prefix + ".vars.detail"));
        docs.add(Component.literal(""));
        docs.add(Component.translatable(prefix + ".fns").withStyle(Style.EMPTY.withColor(0xFFAA00)));
        docs.add(Component.translatable(prefix + ".fns.detail"));
        docs.add(Component.literal(""));
        docs.add(Component.translatable(prefix + ".consts").withStyle(Style.EMPTY.withColor(0xFFAA00)));
        docs.add(Component.translatable(prefix + ".consts.detail"));
        docs.add(Component.literal(""));
        docs.add(Component.translatable(prefix + ".ops").withStyle(Style.EMPTY.withColor(0xFFAA00)));
        docs.add(Component.translatable(prefix + ".ops.detail"));

        int cursorY = y;
        int lineHeight = this.font.lineHeight;
        for (Component part : docs) {
            List<FormattedCharSequence> lines = this.font.split(part, width);
            for (FormattedCharSequence line : lines) {
                if (cursorY + lineHeight > y + maxHeight) {
                    return;
                }
                gui.text(this.font, line, x, cursorY, 0xFFFFFFFF, false);
                cursorY += lineHeight;
            }
            if (cursorY + 1 > y + maxHeight) {
                return;
            }
            cursorY += 1;
        }
    }
}
