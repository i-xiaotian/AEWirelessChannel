package com.xiaotian.ae.wirelesscable.common.tile;

import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.item.ItemStack;

public class TileWirelessOutputBus extends TileWirelessBus {

    public TileWirelessOutputBus() {
        super();
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_OUTPUT_BUS);
    }

}
