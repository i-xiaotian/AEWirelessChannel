package com.xiaotian.ae.wirelesscable.tab;

import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class AEWirelessItemGroup extends ItemGroup {

    public AEWirelessItemGroup(final String tableName) {
        super(tableName);
    }

    @Override
    @Nonnull
    public ItemStack makeIcon() {
        return new ItemStack(Blocks.DENSE_WIRELESS_OUTPUT_BUS);
    }

}
