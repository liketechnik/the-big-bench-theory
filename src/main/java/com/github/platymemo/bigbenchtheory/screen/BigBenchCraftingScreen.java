package com.github.platymemo.bigbenchtheory.screen;

import com.github.platymemo.bigbenchtheory.BigBenchTheory;
import com.github.platymemo.bigbenchtheory.screen.handlers.AbstractBigBenchCraftingScreenHandler;
import com.github.platymemo.bigbenchtheory.util.ScreenPlacementHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BigBenchCraftingScreen extends HandledScreen<AbstractBigBenchCraftingScreenHandler> implements RecipeBookProvider {
    private static final Identifier[] TEXTURES = {
            new Identifier(BigBenchTheory.MOD_ID, "textures/gui/container/tiny_bench.png"),
            new Identifier(BigBenchTheory.MOD_ID, "textures/gui/container/big_bench.png"),
            new Identifier(BigBenchTheory.MOD_ID, "textures/gui/container/bigger_bench.png"),
            new Identifier(BigBenchTheory.MOD_ID, "textures/gui/container/biggest_bench.png")};
    private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
    private ScreenPlacementHelper placementHelper;
    private final RecipeBookWidget recipeBook = new RecipeBookWidget();
    private boolean narrow;

    public BigBenchCraftingScreen(AbstractBigBenchCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.placementHelper = handler.placementHelper;
    }

    protected void init() {
        this.backgroundWidth = this.placementHelper.getWidth();
        this.backgroundHeight = this.placementHelper.getHeight();
        super.init();
        this.narrow = this.width < this.backgroundWidth + 204;
        this.recipeBook.initialize(placementHelper.getRecipeBookWidth(width), this.height, this.client, this.narrow, this.handler);
        this.x = this.recipeBook.findLeftEdge(this.narrow, this.width, this.backgroundWidth);
        this.children.add(this.recipeBook);
        this.setInitialFocus(this.recipeBook);
        this.addButton(new TexturedButtonWidget(placementHelper.getRecipeBookX(x), placementHelper.getRecipeBookY(this.height), 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (buttonWidget) -> {
            this.recipeBook.reset(this.narrow);
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.narrow, this.width, this.backgroundWidth);
            ((TexturedButtonWidget)buttonWidget).setPos(placementHelper.getRecipeBookX(x), placementHelper.getRecipeBookY(this.height));
        }));
        this.titleX = this.placementHelper.getTitleX();
        this.titleY = this.placementHelper.getTitleY();
        this.playerInventoryTitleX = this.placementHelper.getPlayerInventoryTitleX();
        this.playerInventoryTitleY = this.placementHelper.getPlayerInventoryTitleY();
    }

    public void tick() {
        super.tick();
        this.recipeBook.update();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if (this.recipeBook.isOpen() && this.narrow) {
            this.drawBackground(matrices, delta, mouseX, mouseY);
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
        } else {
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURES[this.placementHelper.getOrder()]);
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.placementHelper.getHeight() >= 255)
            return;
        super.drawForeground(matrices, mouseX, mouseY);
    }

    protected boolean isPointWithinBounds(int xPosition, int yPosition, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(xPosition, yPosition, width, height, pointX, pointY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBook);
            return true;
        } else {
            return this.narrow && this.recipeBook.isOpen() || super.mouseClicked(mouseX, mouseY, button);
        }
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
    }

    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
        this.recipeBook.slotClicked(slot);
    }

    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    public void removed() {
        this.recipeBook.close();
        super.removed();
    }

    public RecipeBookWidget getRecipeBookWidget() {
        return this.recipeBook;
    }
}
