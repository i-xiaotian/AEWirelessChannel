package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.xiaotian.ae.wirelesscable.block.BlockBaseWirelessBus;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Objects;

public abstract class TileWirelessBus extends TileEntity implements IGridProxyable, IActionHost, ITickableTileEntity {

    private final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);
    protected byte tickCounter = 0;

    public TileWirelessBus(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    public abstract ItemStack getVisualItemStack();

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation aePartLocation) {
        return this.proxy.getNode();
    }

    @Override
    public void securityBreak() {
        if (Objects.isNull(world)) return;
        world.destroyBlock(getPos(), true);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public void saveChanges() {

    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.proxy.getNode();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        proxy.onChunkUnloaded();
    }

    @Override
    public void validate() {
        super.validate();
        proxy.validate();
        proxy.onReady();
    }

    @Override
    @Nonnull
    public CompoundNBT write(@Nonnull final CompoundNBT compound) {
        super.write(compound);
        proxy.writeToNBT(compound);
        final CompoundNBT sidesTag = new CompoundNBT();
        final EnumSet<Direction> connectableSides = proxy.getConnectableSides();
        for (Direction side : connectableSides) {
            sidesTag.putBoolean(String.valueOf(side.ordinal()), true);
        }
        compound.put("validSides", sidesTag);
        return compound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void read(final BlockState state, final CompoundNBT compound) {
        super.read(state, compound);
        proxy.readFromNBT(compound);
        if (!compound.contains("validSides")) return;

        final CompoundNBT sidesTag = compound.getCompound("validSides");
        EnumSet<Direction> newSides = EnumSet.noneOf(Direction.class);
        final Direction[] values = Direction.values();
        for (Direction facing : values) {
            if (!sidesTag.contains(String.valueOf(facing.ordinal()))) continue;
            newSides.add(facing);
        }
        proxy.setValidSides(newSides);
    }

    @Override
    public void tick() {
        if (Objects.isNull(world) || world.isRemote) return;
        final boolean active = proxy.isActive();

        final BlockState currentBlockState = world.getBlockState(pos);
        final Boolean value = currentBlockState.get(BlockBaseWirelessBus.POWERED);

        if (active != value) {
            final BlockState newBlockState = currentBlockState.with(BlockBaseWirelessBus.POWERED, active);
            world.setBlockState(pos, newBlockState, 3);
        }
    }


}
