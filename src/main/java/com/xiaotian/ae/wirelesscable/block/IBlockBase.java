package com.xiaotian.ae.wirelesscable.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;

public interface IBlockBase {

    String getBlockId();

    @Nonnull
    ItemBlock createItemBlock(Block block);

}
