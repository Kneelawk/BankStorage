package net.natte.bankstorage.screen;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.rendering.ItemCountUtils;
import net.natte.bankstorage.util.Util;

public class BankScreen extends HandledScreen<BankScreenHandler> {

    private static final Identifier WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private BankType type;
    private Identifier texture;

    public static Provider<BankScreenHandler, BankScreen> fromType(BankType type) {
        return (screenHandler, playerInventory, text) -> {
            return new BankScreen(screenHandler, playerInventory, text, type);
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        // middle click sorting
        if (button == 2 && (this.getSlotAt(mouseX, mouseY) instanceof BankSlot)) {
            ClientPlayNetworking.send(new SortPacketC2S());
            return true;
        }
        // left click + lockSlot keybind
        if (button == 0 && BankStorageClient.lockSlotKeyBinding.isPressed()) {
            Slot slot = this.getSlotAt(mouseX, mouseY);
            if (slot instanceof BankSlot hoveredSlot) {
                int hoveredSlotIndex = hoveredSlot.getIndex();
                ItemStack hoveredStack = hoveredSlot.getStack();
                ItemStack cursorStack = this.handler.getCursorStack();
                boolean isLocked = hoveredSlot.isLocked();
                if (!isLocked) {
                    if (hoveredStack.isEmpty()) {
                        if (cursorStack.isEmpty()) { // empty unlocked, cursor empty
                            ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex,
                                    ItemStack.EMPTY, true));
                            this.cancelNextRelease = true;
                            return true;
                        } else { // empty unlocked, cursor has
                            ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex,
                                    cursorStack, true));
                            this.cancelNextRelease = true;
                            return true;
                        }
                    } else {
                        if (cursorStack.isEmpty()) { // has unlocked, cursor empty
                            ClientPlayNetworking
                                    .send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex, hoveredStack,
                                            true));
                            this.cancelNextRelease = true;
                            return true;
                        } else { // has unlocked, cursor has
                            if(ItemStack.canCombine(hoveredStack, cursorStack)){
                                ClientPlayNetworking
                                    .send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex, hoveredStack,
                                            true));
                            }
                            this.cancelNextRelease = true;
                            return true;
                        }
                    }
                } else {
                    if (hoveredStack.isEmpty()) {
                        if (cursorStack.isEmpty()) { // empty locked, cursor empty
                            ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex,
                                    ItemStack.EMPTY, false));
                            this.cancelNextRelease = true;
                            return true;
                        } else { // empty locked, cursor has
                            ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex,
                                    cursorStack, true));
                            this.cancelNextRelease = true;
                            return true;
                        }
                    } else {
                        if (cursorStack.isEmpty()) { // has locked, cursor empty
                            ClientPlayNetworking
                                    .send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex, ItemStack.EMPTY, false));
                            this.cancelNextRelease = true;
                            return true;
                        } else { // has locked, cursor has
                            this.cancelNextRelease = true;
                            return true;
                        }
                    }
                }
                // ItemStack lockedStack = this.handler.getCursorStack().isEmpty() ?
                // hoveredSlot.getStack() : this.handler.getCursorStack();
                // ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId,
                // hoveredSlotIndex, lockedStack));
                // this.cancelNextRelease = true;
                // return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode)) {
            BankStorageClient.lockSlotKeyBinding.setPressed(true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode)) {
            BankStorageClient.lockSlotKeyBinding.setPressed(false);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public BankScreen(BankScreenHandler screenHandler, PlayerInventory playerInventory, Text text, BankType type) {
        super(screenHandler, playerInventory, text);

        this.type = type;
        this.texture = this.type.getGuiTexture();
        this.backgroundWidth = this.type.guiTextureWidth;
        this.backgroundHeight = this.type.guiTextureHeight;

        this.playerInventoryTitleY += this.type.rows * 18 - 52;

    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(
                ButtonWidget
                        .builder(Text.translatable("button.bankstorage.sort"),
                                button -> ClientPlayNetworking.send(new SortPacketC2S()))
                        .dimensions(x + titleX + this.type.guiTextureWidth - 60, y + titleY - 2, 40, 12).build());

        this.addDrawableChild(
                ButtonWidget
                        .builder(Text.translatable("button.bankstorage.pickupmode"),
                                button -> ClientPlayNetworking.send(new PickupModePacketC2S()))
                        .dimensions(x + titleX + this.type.guiTextureWidth - 110, y + titleY - 2, 40, 12).build());

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.setFocused(null);
    }

    @Override
    protected void calculateOffset() {
        ItemStack itemStack = this.handler.getCursorStack();
        if (itemStack.isEmpty() || !this.cursorDragging) {
            return;
        }
        if (this.heldButtonType == 2) {
            this.draggedStackRemainder = itemStack.getMaxCount();
            return;
        }
        this.draggedStackRemainder = itemStack.getCount();
        for (Slot slot : this.cursorDragSlots) {
            ItemStack itemStack2 = slot.getStack();
            int i = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
            int j = slot.getMaxItemCount(itemStack);
            int k = Math.min(ScreenHandler.calculateStackSize(this.cursorDragSlots, (int) this.heldButtonType,
                    (ItemStack) itemStack) + i, j);
            this.draggedStackRemainder -= k - i;
        }
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight,
                (int) Math.ceil(backgroundWidth / 256d) * 256, (int) Math.ceil(backgroundHeight / 256d) * 256);
    }

    @Override
    public void drawSlot(DrawContext context, Slot slot) {
        if (slot instanceof BankSlot bankSlot) {
            drawBankSlot(context, bankSlot);
        } else {
            super.drawSlot(context, slot);
        }

    }

    private void drawBankSlot(DrawContext context, BankSlot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getStack();
        boolean bl = false;
        boolean bl2 = slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && !this.touchIsRightClickDrag;
        ItemStack itemStack2 = this.handler.getCursorStack();
        boolean drawInYellow = false;
        if (slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && this.touchIsRightClickDrag
                && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !itemStack2.isEmpty()) {
            if (this.cursorDragSlots.size() == 1) {
                return;
            }
            if (this.canInsertItemIntoSlot((Slot) slot, (ItemStack) itemStack2, (boolean) true)
                    && this.handler.canInsertIntoSlot(slot)) {
                bl = true;
                int k = slot.getMaxItemCount(itemStack2);
                int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
                int m = this.calculateStackSize(this.cursorDragSlots, (int) this.heldButtonType, (ItemStack) itemStack2)
                        + l;
                if (m > k) {
                    m = k;
                    drawInYellow = true;
                }
                itemStack = itemStack2.copyWithCount(m);
            } else {
                this.cursorDragSlots.remove(slot);
                this.calculateOffset();
            }
        }
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);

        if (slot.isLocked()) {
            context.drawTexture(WIDGETS_TEXTURE, i, j, itemStack.isEmpty() ? 16 : 0, 46, 16, 16);
            // if(slot.getLockedStack().isEmpty() || !itemStack.isEmpty()) context.drawTexture(WIDGETS_TEXTURE, i, j, itemStack.isEmpty() ? 16 : 0, 46, 16, 16);
        }

        if (itemStack.isEmpty() && slot.isEnabled() && slot.isLocked()) {

            context.drawItem(slot.getLockedStack(), i, j);
            context.fill(i, j, i + 16, j + 16, 160, 0x7f8b8b8b);
        }

        if (!bl2) {
            if (bl) {
                context.fill(i, j, i + 16, j + 16, -2130706433);
            }
            context.drawItem(itemStack, i, j, slot.x + slot.y * this.backgroundWidth);
            this.drawItemCountInSlot(context, this.textRenderer, itemStack, i, j, drawInYellow);
        }
        context.getMatrices().pop();
    }

    public int calculateStackSize(Set<Slot> slots, int mode, ItemStack stack) {
        return switch (mode) {
            case 0 -> MathHelper.floor((float) stack.getCount() / (float) slots.size());
            case 1 -> 1;
            case 2 -> stack.getItem().getMaxCount();
            default -> stack.getCount();
        };
    }

    public boolean canInsertItemIntoSlot(/* @Nullable */ Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl = !slot.hasStack();
        if(slot instanceof BankSlot bankSlot){
            if(bankSlot.isLocked()){
                if(!Util.canCombine(stack, bankSlot.getLockedStack())) return false;
            }
        }
        if (!bl && ItemStack.canCombine(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= slot.getMaxItemCount(stack);
        }
        return bl;
    }

    public void drawItemCountInSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y,
            boolean drawInYellow) {
        ClientPlayerEntity clientPlayerEntity;
        float f;
        int l;
        int k;
        if (stack.isEmpty()) {
            return;
        }
        MatrixStack matrices = context.getMatrices();

        matrices.push();

        if (stack.isItemBarVisible()) {
            int i = stack.getItemBarStep();
            int j = stack.getItemBarColor();
            k = x + 2;
            l = y + 13;
            context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, -16777216);
            context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | 0xFF000000);
        }
        if (stack.getCount() != 1 || drawInYellow) {
            String count = ItemCountUtils.toConsiseString(stack.getCount());
            String string = count;
            String formattedString = drawInYellow ? Formatting.YELLOW.toString() + count : count;
            matrices.translate(0.0f, 0.0f, 200.0f);
            float scale = ItemCountUtils.scale(string);

            matrices.translate(x * (1 - scale), y * (1 - scale) + (1 - scale) * 16, 0);
            matrices.scale(scale, scale, 1);

            int textWidth = (int) (textRenderer.getWidth(string) * scale);
            context.drawText(textRenderer, formattedString, x + 19 - 2 - textWidth, y + 6 + 3, 0xFFFFFF, true);
        }
        f = (clientPlayerEntity = this.client.player) == null ? 0.0f
                : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(),
                        this.client.getTickDelta());
        if (f > 0.0f) {
            k = y + MathHelper.floor((float) (16.0f * (1.0f - f)));
            l = k + MathHelper.ceil((float) (16.0f * f));
            context.fill(RenderLayer.getGuiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
        }
        matrices.pop();
    }

    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            ItemStack itemStack = this.focusedSlot.getStack();
            List<Text> tooltip = this.getTooltipFromItem(itemStack);
            if (itemStack.getCount() > 9999) {
                tooltip.add(1, Text.literal(FORMAT.format(itemStack.getCount())).formatted(Formatting.GRAY));
            }
            context.drawTooltip(this.textRenderer, tooltip, itemStack.getTooltipData(), x, y);
        }
    }

}
