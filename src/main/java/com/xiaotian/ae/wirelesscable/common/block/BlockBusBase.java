package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public abstract class BlockBusBase extends Block implements IBlockBase {

    public BlockBusBase(Material material) {
        super(material);
        this.setSoundType(SoundType.GLASS);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, getBlockId()));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + getBlockId());
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isFullBlock(final IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


}
