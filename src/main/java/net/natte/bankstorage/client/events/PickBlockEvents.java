package net.natte.bankstorage.client.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class PickBlockEvents {
    public static boolean pickBlock(Minecraft client) {
        ItemStack right = client.player.getMainHandItem();
        ItemStack left = client.player.getOffhandItem();
        if (pickBlockFromBank(right, true, client))
            return true;
        if (pickBlockFromBank(left, false, client))
            return true;
        return false;
    }

    private static boolean pickBlockFromBank(ItemStack stack, boolean isRight, Minecraft client) {
        if (!Util.isBankLike(stack))
            return false;

        // BankOptions options = Util.getOrCreateOptions(stack);
        BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
        if (options.buildMode != BuildMode.NORMAL)
            return false;

        ItemStack pickedStack = getPickedStack(stack, client);
        if (pickedStack == null)
            return false;
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(stack);
        if (cachedBankStorage == null)
            return false;

        ItemStack currentSelected = cachedBankStorage.getSelectedItem(options.selectedItemSlot);
        if (!currentSelected.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, currentSelected)) {
            return true;
        }
        int slot = -1;
        for (int i = 0; i < cachedBankStorage.blockItems.size(); ++i) {
            ItemStack itemStack = cachedBankStorage.blockItems.get(i);
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, itemStack)) {
                slot = i;
                break;
            }
        }

        if (slot == -1)
            return false;

        options.selectedItemSlot = slot;
        client.getConnection().send(new UpdateBankOptionsPacketC2S(options));
        return true;

    }

    private static ItemStack getPickedStack(ItemStack stack, Minecraft client) {
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos blockPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPos);
        if (blockState.isAir()) {
            return null;
        }
        Block block = blockState.getBlock();
        ItemStack pickBlockStack = block.getPickStack(client.world, blockPos, blockState);

        if (pickBlockStack.isEmpty()) {
            return null;
        }
        return pickBlockStack;

    }
}