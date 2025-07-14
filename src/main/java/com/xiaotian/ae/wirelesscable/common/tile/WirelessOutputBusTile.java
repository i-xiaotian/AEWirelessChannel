package com.xiaotian.ae.wirelesscable.common.tile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class WirelessOutputBusTile extends WirelessTile {

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Items.BANNER);
    }
}
