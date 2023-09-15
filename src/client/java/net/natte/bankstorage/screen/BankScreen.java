package net.natte.bankstorage.screen;

import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankType;
import net.natte.bankstorage.inventory.BankSlot;

public class BankScreen extends HandledScreen<BankScreenHandler> {

    public static final ScreenHandlerType<BankScreenHandler> BANK_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(null,
            null);

    private BankType type;
    private Identifier texture;

    public static Provider<BankScreenHandler, BankScreen> fromType(BankType type) {
        return (screenHandler, playerInventory, text) -> {
            return new BankScreen(screenHandler, playerInventory, text, type);
        };
    }

    public BankScreen(BankScreenHandler screenHandler, PlayerInventory playerInventory, Text text, BankType type) {
        super(screenHandler, playerInventory, text);
        this.type = type;
        this.texture = this.type.getGuiTexture();
        this.backgroundWidth = this.type.guiTextureWidth;
        this.backgroundHeight = this.type.guiTextureHeight;
        // titleY += 34 - (this.type.rows - 1) * 9;
        playerInventoryTitleY += (this.type.rows - 1) * 18 - 34;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        if (this.focusedSlot instanceof BankSlot bankSlot) {
            // System.out.println("ok");
            // context.getMatrices().push();
            // context.getMatrices().translate(this.x, this.y, 0);
            // bankSlot.render(context);
            // context.getMatrices().pop();
        }
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight);
        // re
    }

    // @Shadow
    @Override
    // @
    public void drawSlot(DrawContext context, Slot slot) {
        if (slot instanceof BankSlot bankSlot) {
            // System.out.println("yes");
            // bankSlot.render(context);
        } else {
            // System.out.println("no");
        }
        super.drawSlot(context, slot);

    }
    // com.mojang.datafixers.util.Pair<Identifier, Identifier> pair;
    // int i = slot.x;
    // int j = slot.y;
    // ItemStack itemStack = slot.getStack();
    // boolean bl = false;
    // /* */
    // Slot touchDragSlotStart = null;
    // ItemStack touchDragStack = ItemStack.EMPTY;
    // boolean touchIsRightClickDrag = false;

    // int heldButtonType = 0;
    // /* */
    // boolean bl2 = slot == touchDragSlotStart && !touchDragStack.isEmpty() &&
    // !touchIsRightClickDrag;
    // ItemStack itemStack2 = this.handler.getCursorStack();
    // String string = null;
    // if (slot == touchDragSlotStart && !touchDragStack.isEmpty() &&
    // touchIsRightClickDrag && !itemStack.isEmpty()) {
    // itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
    // } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) &&
    // !itemStack2.isEmpty()) {
    // if (this.cursorDragSlots.size() == 1) {
    // return;
    // }
    // if (ScreenHandler.canInsertItemIntoSlot((Slot)slot, (ItemStack)itemStack2,
    // (boolean)true) && this.handler.canInsertIntoSlot(slot)) {
    // bl = true;
    // int k = Math.min(itemStack2.getMaxCount() * this.type.slotStorageMultiplier,
    // slot.getMaxItemCount(itemStack2));
    // int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
    // int m = ScreenHandler.calculateStackSize(this.cursorDragSlots,
    // (int)heldButtonType, (ItemStack)itemStack2) + l;
    // if (m > k) {
    // m = k;
    // string = Formatting.YELLOW.toString() + k;
    // }
    // itemStack = itemStack2.copyWithCount(m);
    // } else {
    // this.cursorDragSlots.remove(slot);
    // // this.calculateOffset();
    // }
    // }
    // context.getMatrices().push();
    // context.getMatrices().translate(0.0f, 0.0f, 100.0f);
    // if (itemStack.isEmpty() && slot.isEnabled() && (pair =
    // slot.getBackgroundSprite()) != null) {
    // Sprite sprite =
    // this.client.getSpriteAtlas((Identifier)pair.getFirst()).apply((Identifier)pair.getSecond());
    // context.drawSprite(i, j, 0, 16, 16, sprite);
    // bl2 = true;
    // }
    // if (!bl2) {
    // if (bl) {
    // context.fill(i, j, i + 16, j + 16, -2130706433);
    // }
    // context.drawItem(itemStack, i, j, slot.x + slot.y * this.backgroundWidth);
    // context.drawItemInSlot(this.textRenderer, itemStack, i, j, "900");
    // }
    // context.getMatrices().pop();
    // }

}
