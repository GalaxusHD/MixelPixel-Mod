package net.mixelpixel.mod.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ActionWidget extends ClickableWidget {
    private final Runnable action;
    private final Identifier hoverTexture;
    private final int hoverTextureWidth;
    private final int hoverTextureHeight;
    private final boolean invisible;

    public ActionWidget(int x, int y, int width, int height, Text message, Runnable action,
                        Identifier hoverTexture, int hoverTextureWidth, int hoverTextureHeight, boolean invisible) {
        super(x, y, width, height, message);
        this.action = action;
        this.hoverTexture = hoverTexture;
        this.hoverTextureWidth = hoverTextureWidth;
        this.hoverTextureHeight = hoverTextureHeight;
        this.invisible = invisible;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (invisible) return;
        if (isHovered() && hoverTexture != null) {
            context.drawTexture(GuiAssets.PIPELINE, hoverTexture, getX(), getY(), 0, 0,
                    width, height, width, height, hoverTextureWidth, hoverTextureHeight);
            context.fill(getX(), getY(), getRight(), getBottom(), 0x33000000);
        } else {
            context.fill(getX(), getY(), getRight(), getBottom(), 0xFF000000);
            context.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2,
                    isHovered() ? 0xFF8A8A8A : 0xFF666666);
        }
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        action.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
