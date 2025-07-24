package com.xiaotian.ae.wirelesscable.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

import javax.annotation.Nonnull;

public interface IBlockBase {

    String getBlockId();

    @Nonnull
    BlockItem createItemBlock(Block block);

}
