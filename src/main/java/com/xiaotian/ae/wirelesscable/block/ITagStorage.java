package com.xiaotian.ae.wirelesscable.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITagStorage {

    void onPlaced(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack);

    void onBreak();
}
