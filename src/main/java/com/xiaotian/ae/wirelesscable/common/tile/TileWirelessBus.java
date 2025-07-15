package com.xiaotian.ae.wirelesscable.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class TileWirelessBus extends TileEntity implements IGridProxyable, IActionHost {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);
    protected final IActionSource actionSource;

    public TileWirelessBus() {
        this.actionSource = new MachineSource(this);
    }

    public abstract ItemStack getVisualItemStack();

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation aePartLocation) {
        return this.proxy.getNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        this.getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.proxy.getNode();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        proxy.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        proxy.invalidate();
    }

    @Override
    public void validate() {
        super.validate();
        proxy.validate();
        proxy.onReady();
    }

    @Override
    protected void setWorldCreate(@Nonnull final World worldIn) {
        setWorld(worldIn);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        super.writeToNBT(compound);
        proxy.writeToNBT(compound);
        NBTTagCompound sidesTag = new NBTTagCompound();
        final EnumSet<EnumFacing> connectableSides = proxy.getConnectableSides();
        for (EnumFacing side : connectableSides) {
            sidesTag.setBoolean(String.valueOf(side.ordinal()), true);
        }
        compound.setTag("validSides", sidesTag);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        proxy.readFromNBT(compound);
        if (!compound.hasKey("validSides")) return;

        final NBTTagCompound sidesTag = compound.getCompoundTag("validSides");
        EnumSet<EnumFacing> newSides = EnumSet.noneOf(EnumFacing.class);
        final EnumFacing[] values = EnumFacing.values();
        for (EnumFacing facing : values) {
            if (!sidesTag.hasKey(String.valueOf(facing.ordinal()))) continue;
            newSides.add(facing);
        }
        proxy.setValidSides(newSides);
    }
}
