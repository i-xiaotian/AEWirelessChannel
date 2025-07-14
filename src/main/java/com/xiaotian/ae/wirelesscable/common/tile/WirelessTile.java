package com.xiaotian.ae.wirelesscable.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.xiaotian.ae.wirelesscable.common.entity.ConnectionKey;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WirelessTile extends TileEntity implements IGridProxyable, IActionHost {

    private final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);

    protected ConnectionKey connectionKey;

    public abstract ItemStack getVisualItemStack();

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation aePartLocation) {
        return this.getProxy().getNode();
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

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }

}
