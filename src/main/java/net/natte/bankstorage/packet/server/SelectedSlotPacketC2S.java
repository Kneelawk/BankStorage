package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectedSlotPacketC2S(boolean isRight, int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectedSlotPacketC2S> TYPE = new CustomPacketPayload.Type<>(Util.ID("selected_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectedSlotPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SelectedSlotPacketC2S::isRight,
            ByteBufCodecs.INT,
            SelectedSlotPacketC2S::slot,
            SelectedSlotPacketC2S::new);


    @Override
    public Type<SelectedSlotPacketC2S> type() {
        return TYPE;
    }

    public static void handle(SelectedSlotPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ItemStack stack = player.getItemInHand(packet.isRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
            BankItemStorage bankItemStorage = Util.getBankItemStorage(Util.getUUID(stack));
            if (bankItemStorage == null)
                return;
            BankOptions options = Util.getOrCreateOptions(stack);
            options.selectedItemSlot = packet.slot;
            int size = bankItemStorage.getBlockItems().size();
            options.selectedItemSlot = size == 0 ? 0
                    : Math.min(Math.max(options.selectedItemSlot, 0), size - 1);
            Util.setOptions(stack, options);

        }
    }
}
