package com.xiaotian.ae.wirelesscable.common.tile;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TileOutputBusWireless extends TileWireless {

    public static Item visualItemStack;

    public static void setVisualItemStack(Item item) {
        visualItemStack = item;
    }

    public TileOutputBusWireless() {
        super();
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_OUTPUT_BUS);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation aePartLocation) {
        return AECableType.DENSE_SMART;
    }


}
