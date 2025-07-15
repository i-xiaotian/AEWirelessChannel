package com.xiaotian.ae.wirelesscable.common.tile;

import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.item.ItemStack;

public class TileWirelessInputBus extends TileWirelessBus {

    public TileWirelessInputBus() {
        super();
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_INPUT_BUS);
    }
}
