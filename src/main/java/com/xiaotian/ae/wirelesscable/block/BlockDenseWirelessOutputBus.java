package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessOutputBus;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDenseWirelessOutputBus extends BlockWirelessOutputBus {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.875, 0.875);

    public BlockDenseWirelessOutputBus() {
        super();
    }

    @Override
    public String getBlockId() {
        return "block_dense_wireless_output_bus";
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDenseWirelessOutputBus.class;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new TileDenseWirelessOutputBus();
    }

    @Override
    @Nonnull
    protected AxisAlignedBB getBlockBoundingBox() {
        return BOUNDING_BOX;
    }
}
