package com.xiaotian.ae.wirelesscable.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public class AEWirelessTab extends CreativeTabs {

    public AEWirelessTab(final String tableName) {
        super(tableName);
    }

    @Override
    @SuppressWarnings("all")
    public ItemStack createIcon() {
        Item value = ForgeRegistries.ITEMS.getValue(new ResourceLocation("appliedenergistics2", "network_tool"));
        if (Objects.isNull(value)) return new ItemStack(Blocks.ANVIL);
        return new ItemStack(value);
    }
}
