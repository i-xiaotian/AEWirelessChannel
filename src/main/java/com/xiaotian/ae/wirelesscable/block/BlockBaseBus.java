package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.proxy.CommonProxy;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public abstract class BlockBaseBus extends Block implements IBlockBase {

    private final Map<EnumFacing, AxisAlignedBB> aabbMap = new HashMap<>();

    public BlockBaseBus(Material material) {
        super(material);
        this.setSoundType(SoundType.GLASS);
        this.setHardness(1.0F);
        this.setResistance(5.0F);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, getBlockId()));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + getBlockId());

        final AxisAlignedBB boundingBox = getBlockBoundingBox();
        final double minA = boundingBox.minX;
        final double minB = boundingBox.minY;
        final double minC = boundingBox.minZ;
        final double maxA = boundingBox.maxX;
        final double maxB = boundingBox.maxY;
        final double maxC = boundingBox.maxZ;
        aabbMap.put(EnumFacing.UP, boundingBox);
        aabbMap.put(EnumFacing.DOWN, new AxisAlignedBB(minA, 1 - maxB, minC, maxA, 1 - minB, maxC));
        aabbMap.put(EnumFacing.NORTH, new AxisAlignedBB(minA, minC, 1 - maxB, maxA, maxC, 1 - minB));
        aabbMap.put(EnumFacing.SOUTH, new AxisAlignedBB(1 - maxA, minC, minB, 1 - minA, maxC, maxB));
        aabbMap.put(EnumFacing.EAST, new AxisAlignedBB(maxB, 1 - maxA, minC, minB, 1 - minA, maxC));
        aabbMap.put(EnumFacing.WEST, new AxisAlignedBB(1- maxB, 1 - maxA, minC, 1- minB, 1 - minA, maxC));
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isFullBlock(IBlockState state) {
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

    @Nonnull
    protected abstract AxisAlignedBB getBlockBoundingBox();

    protected AxisAlignedBB rotateAABB(final EnumFacing facing) {
        return this.aabbMap.get(facing);
    }


}
