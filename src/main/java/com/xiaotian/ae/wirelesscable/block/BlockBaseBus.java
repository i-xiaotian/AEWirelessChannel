package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public abstract class BlockBaseBus extends Block implements IBlockBase, IBloomTexture {

    private final Map<Direction, VoxelShape> aabbMap = new HashMap<>();

    public BlockBaseBus(AbstractBlock.Properties properties) {
        super(
                properties.setSuffocates((blockState, blockReader, blockPos) -> false)
                        .sound(SoundType.GLASS)
                        .hardnessAndResistance(1.0F, 3.0F)
                        .setOpaque((blockState, blockReader, blockPos) -> false)
                        .setBlocksVision((blockState, blockReader, blockPos) -> false)
                        .notSolid()
        );

        final AxisAlignedBB boundingBox = getBlockBoundingBox();
        final double minA = boundingBox.minX;
        final double minB = boundingBox.minY;
        final double minC = boundingBox.minZ;
        final double maxA = boundingBox.maxX;
        final double maxB = boundingBox.maxY;
        final double maxC = boundingBox.maxZ;
        aabbMap.put(Direction.UP, VoxelShapes.create(boundingBox));
        aabbMap.put(Direction.DOWN, VoxelShapes.create(new AxisAlignedBB(minA, 1 - maxB, minC, maxA, 1 - minB, maxC)));
        aabbMap.put(Direction.NORTH, VoxelShapes.create(new AxisAlignedBB(minA, minC, 1 - maxB, maxA, maxC, 1 - minB)));
        aabbMap.put(Direction.SOUTH, VoxelShapes.create(new AxisAlignedBB(1 - maxA, minC, minB, 1 - minA, maxC, maxB)));
        aabbMap.put(Direction.EAST, VoxelShapes.create(new AxisAlignedBB(maxB, 1 - maxA, minC, minB, 1 - minA, maxC)));
        aabbMap.put(Direction.WEST, VoxelShapes.create(new AxisAlignedBB(1 - maxB, 1 - maxA, minC, 1 - minB, 1 - minA, maxC)));
    }


    @Override
    @ParametersAreNonnullByDefault
    public void onReplaced(final BlockState stateOld, final World world, final BlockPos blockPos, final BlockState stateNew, final boolean p_196243_5_) {
        if (stateOld.getBlock() != stateNew.getBlock()) {
            TileEntity tileentity = world.getTileEntity(blockPos);
            if (tileentity instanceof TileWirelessBus) world.removeTileEntity(blockPos);
        }
        super.onReplaced(stateOld, world, blockPos, stateNew, p_196243_5_);
    }

    @Override
    public String getBloomTextureEndWith() {
        return "_bloom";
    }

    @Nonnull
    protected abstract AxisAlignedBB getBlockBoundingBox();

    protected VoxelShape rotateAABB(final Direction facing) {
        return this.aabbMap.get(facing);
    }


}
