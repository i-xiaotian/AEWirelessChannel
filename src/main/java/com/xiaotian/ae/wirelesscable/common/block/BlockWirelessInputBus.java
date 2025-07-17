package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessInputBus;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockWirelessInputBus extends BlockBaseWirelessBus implements ITileEntityProvider, IHasTileEntity {

    public BlockWirelessInputBus() {
        super(Material.GLASS);
    }

    @Override
    public String getBlockId() {
        return "block_wireless_input_bus";
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileWirelessInputBus.class;
    }

    @Override
    public String getTileEntityId() {
        return getBlockId().replace("block_", "tile_");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new TileWirelessInputBus();
    }
}
