package com.flechazo.reallyradicalrolls.config.ui;

import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.expression.ExpressionParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class ExpressionEditorWidget extends AbstractWidget {
    public static final Identifier WIDGET_ID = ReallyRadicalRolls.id("expression_editor");
    private static final int ROW_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 52;
    private static final int H_PADDING = 4;
    private static final int GAP = 4;

    private final Font font;
    private final Screen screen;
    private final Runnable onChanged;
    private final Component title;
    private String expressionString;
    private ExpressionParser expression;
    private boolean hasError;
    private String errorMessage;

    public ExpressionEditorWidget(
            Screen screen,
            Font font,
            int x,
            int y,
            int width,
            Component title,
            ExpressionParser initial,
            Runnable onChanged
    ) {
        super(x, y, width, ROW_HEIGHT, Component.empty());
        this.screen = screen;
        this.font = font;
        this.title = title;
        this.onChanged = onChanged;
        this.expression = initial;
        this.expressionString = initial.getString();
        this.updateStatus(initial);
    }

    private void setValueInternal(String text) {
        this.expressionString = text == null ? "" : text;
        this.expression = new ExpressionParser(this.expressionString);
        this.hasError = this.expression.hasError();
        this.errorMessage = this.hasError && this.expression.getError() != null
                ? this.expression.getError().getMessage()
                : null;
    }

    private void updateStatus(ExpressionParser parser) {
        this.expression = parser == null ? new ExpressionParser("") : parser;
        this.expressionString = this.expression.getString();
        this.hasError = this.expression.hasError();
        this.errorMessage = this.hasError && this.expression.getError() != null
                ? this.expression.getError().getMessage()
                : null;
    }

    public ExpressionParser getExpression() {
        return this.expression;
    }

    public String getExpressionString() {
        return this.expressionString;
    }

    public void setExpressionString(String expressionString) {
        this.setValueInternal(expressionString);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int bgColor = this.isFocused() ? 0x40000000 : 0x26000000;
        g.fill(RenderPipelines.GUI, this.getX(), this.getY(),
                this.getX() + this.width, this.getY() + this.height, bgColor);

        int iconY = this.getY() + (ROW_HEIGHT - this.font.lineHeight) / 2;
        int previewX = this.getX() + H_PADDING + 10;
        int previewY = iconY;
        int previewMaxWidth = Math.max(20, getButtonX() - GAP - previewX);
        String previewText = this.expressionString == null || this.expressionString.isBlank()
                ? "<empty>"
                : this.font.plainSubstrByWidth(this.expressionString, previewMaxWidth);

        if (hasError && errorMessage != null) {
            g.text(this.font,
                    Component.literal("!").withStyle(Style.EMPTY.withColor(0xFF5555)),
                    this.getX() + H_PADDING, iconY, 0xFFFFFFFF);
        } else {
            g.text(this.font,
                    Component.literal("v").withStyle(Style.EMPTY.withColor(0x55FF55)),
                    this.getX() + H_PADDING, iconY, 0xFFFFFFFF);
        }

        int previewColor = hasError ? 0xFFFF8080 : 0xFFD0D0D0;
        g.text(this.font, previewText, previewX, previewY, previewColor, false);

        int buttonX = getButtonX();
        int buttonY = this.getY() + 1;
        int buttonW = this.getX() + this.width - H_PADDING - buttonX;
        int buttonH = ROW_HEIGHT - 2;
        int buttonColor = this.isButtonHovered(mouseX, mouseY) ? 0xFF5A5A5A : 0xFF3E3E3E;
        g.fill(RenderPipelines.GUI, buttonX, buttonY, buttonX + buttonW, buttonY + buttonH, buttonColor);
        g.outline(buttonX, buttonY, buttonW, buttonH, 0x90FFFFFF);
        g.centeredText(
                this.font,
                Component.translatable("config.%s.expression.open_editor".formatted(ReallyRadicalRolls.MOD_ID)),
                buttonX + buttonW / 2,
                this.getY() + (ROW_HEIGHT - this.font.lineHeight) / 2,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) {
            return;
        }
        this.openBigEditor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.isMouseOver(event.x(), event.y())) {
            this.openBigEditor();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.width
                && mouseY >= this.getY() && mouseY <= this.getY() + this.height;
    }

    private void openBigEditor() {
        Minecraft.getInstance().setScreen(new ExpressionEditorScreen(
                this.screen,
                this.title,
                this.expressionString,
                value -> {
                    this.setValueInternal(value);
                    if (this.onChanged != null) {
                        this.onChanged.run();
                    }
                }
        ));
    }

    private int getButtonX() {
        return this.getX() + this.width - H_PADDING - BUTTON_WIDTH;
    }

    private boolean isButtonHovered(double mouseX, double mouseY) {
        int x0 = getButtonX();
        int x1 = this.getX() + this.width - H_PADDING;
        return mouseY >= this.getY() && mouseY <= this.getY() + this.height
                && mouseX >= x0 && mouseX <= x1;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
