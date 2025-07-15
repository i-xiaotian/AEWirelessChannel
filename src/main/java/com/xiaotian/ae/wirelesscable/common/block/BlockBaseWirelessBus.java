package com.xiaotian.ae.wirelesscable.common.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessBus;
import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessOutputBus;
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

@SuppressWarnings("deprecation")
public abstract class BlockBaseWirelessBus extends BlockBusBase {

    protected static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockBaseWirelessBus(final Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    @Nonnull
    public IBlockState getStateForPlacement(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final EnumFacing clickedFace,
                                            final float hitX, final float hitY, final float hitZ,
                                            final int meta, @Nonnull final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, clickedFace);
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
            if (tileEntity instanceof final TileWirelessBus tileWirelessBus && placer instanceof EntityPlayer) {
                final AENetworkProxy proxy = tileWirelessBus.getProxy();
                proxy.setOwner((EntityPlayer) placer);
                final EnumFacing opposite = state.getValue(FACING).getOpposite();
                proxy.setValidSides(EnumSet.of(opposite));
            }
        }
    }
}
