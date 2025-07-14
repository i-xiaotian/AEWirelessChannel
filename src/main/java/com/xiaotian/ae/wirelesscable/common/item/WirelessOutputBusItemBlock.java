package com.xiaotian.ae.wirelesscable.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import java.util.Objects;

public class WirelessOutputBusItemBlock extends ItemBlock {

    public WirelessOutputBusItemBlock(final Block block) {
        super(block);
        this.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        this.setTranslationKey(block.getTranslationKey());
    }
}
