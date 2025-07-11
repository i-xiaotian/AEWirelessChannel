package com.xiaotian.ae.wirelesscable.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public abstract class WirelessTile extends TileEntity implements IGridProxyable {

    private final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);

    protected String wirelessKey;

    public abstract ItemStack getVisualItemStack();

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation aePartLocation) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void securityBreak() {
        this.getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public DimensionalCoord getLocation() {
        return null;
    }

    @Override
    public void gridChanged() {

    }
}
