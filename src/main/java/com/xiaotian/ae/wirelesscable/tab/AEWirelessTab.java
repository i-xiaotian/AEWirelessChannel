package com.xiaotian.ae.wirelesscable.tab;

import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class AEWirelessTab extends CreativeTabs {

    public AEWirelessTab(final String tableName) {
        super(tableName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public ItemStack createIcon() {
        return new ItemStack(Blocks.DENSE_WIRELESS_OUTPUT_BUS);
    }
}
