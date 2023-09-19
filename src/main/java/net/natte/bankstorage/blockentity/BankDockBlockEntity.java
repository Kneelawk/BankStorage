package net.natte.bankstorage.blockentity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.natte.bankstorage.BankStorage;

public class BankDockBlockEntity extends BlockEntity {

    private ItemStack bankItem = ItemStack.EMPTY;


    public BankDockBlockEntity(BlockPos pos, BlockState state) {
        super(BankStorage.BANK_DOCK_BLOCK_ENTITY, pos, state);
    }

    public boolean hasBank(){
        return !this.bankItem.isEmpty();
    }

    public ItemStack getBank(){
        return this.bankItem;
    }

    public ItemStack pickUpBank(){
        ItemStack bank = this.bankItem;
        this.bankItem = ItemStack.EMPTY;
        this.markDirty();
        return bank;
    }

    public void putBank(ItemStack bank){
        this.bankItem = bank.copy();
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtCompound itemAsNbt = new NbtCompound();
        this.bankItem.writeNbt(itemAsNbt);
        nbt.put("bank", itemAsNbt);
        // System.out.println("saving " + nbt);
        // System.out.println("saved " + this.bankItem);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        // System.out.println("loading " + nbt);

        this.bankItem = ItemStack.fromNbt(nbt.getCompound("bank"));
        // System.out.println("loaded " + this.bankItem);
    }
    
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
      return createNbt();
    }

}
