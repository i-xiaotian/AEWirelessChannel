package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.networking.GridFlags;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TileDenseWirelessInputBus extends TileWirelessInputBus {

    public TileDenseWirelessInputBus() {
        super();
        this.getProxy().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.DENSE_WIRELESS_INPUT_BUS);
    }

}
