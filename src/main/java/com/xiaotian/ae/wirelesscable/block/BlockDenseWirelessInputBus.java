package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.item.ItemBlockDenseWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessInputBus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDenseWirelessInputBus extends BlockWirelessInputBus {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.875, 0.875);

    public BlockDenseWirelessInputBus() {
        super();
    }

    @Override
    public String getBlockId() {
        return "block_dense_wireless_input_bus";
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDenseWirelessInputBus.class;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new TileDenseWirelessInputBus();
    }

    @Override
    @Nonnull
    protected AxisAlignedBB getBlockBoundingBox() {
        return BOUNDING_BOX;
    }

    @Nonnull
    @Override
    public BlockItem createItemBlock(final Block block) {
        return new ItemBlockDenseWirelessInputBus(block);
    }
}
