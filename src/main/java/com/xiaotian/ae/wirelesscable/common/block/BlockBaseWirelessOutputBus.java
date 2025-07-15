package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessOutputBus;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBaseWirelessOutputBus extends BlockBaseWirelessBus implements ITileEntityProvider, IHasTileEntity {

    public BlockBaseWirelessOutputBus() {
        super(Material.GLASS);
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileWirelessOutputBus.class;
    }

    @Override
    public String getTileEntityId() {
        return getBlockId().replace("block_", "tile_");
    }

    @Override
    public String getBlockId() {
        return "block_wireless_output_bus";
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new TileWirelessOutputBus();
    }

    @Override
    public boolean onBlockActivated(final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) return true;
        playerIn.sendMessage(new TextComponentString("点了,15"));
        return true;
    }

}
