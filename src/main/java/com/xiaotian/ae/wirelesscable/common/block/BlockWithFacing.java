package com.xiaotian.ae.wirelesscable.common.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.common.tile.TileOutputBusWireless;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public abstract class BlockWithFacing extends Block {

    protected static final PropertyDirection FACING = PropertyDirection.create("facing");


    public BlockWithFacing(final Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileOutputBusWireless && placer instanceof EntityPlayer) {
                TileOutputBusWireless outputBusWireless = (TileOutputBusWireless) tileEntity;
                final AENetworkProxy proxy = outputBusWireless.getProxy();
                proxy.setOwner((EntityPlayer) placer);
                final EnumFacing opposite = state.getValue(FACING).getOpposite();
                proxy.setValidSides(EnumSet.of(opposite));
            }
        }
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing clickedFace,
                                            float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, clickedFace);
    }


}
