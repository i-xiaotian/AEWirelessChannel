package com.xiaotian.ae.wirelesscable.common.tile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class WirelessInputBusTile extends WirelessTile {
    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Items.GLASS_BOTTLE);
    }
}
