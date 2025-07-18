package com.xiaotian.ae.wirelesscable.common.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessBus;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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
    @ParametersAreNonnullByDefault
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
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

    @Override
    public boolean rotateBlock(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing axis) {
        if (world.isRemote) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        EnumFacing currentFacing = state.getValue(FACING);

        EnumFacing newFacing = currentFacing.rotateAround(axis.getAxis());

        world.setBlockState(pos, state.withProperty(FACING, newFacing), 3);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof final TileWirelessBus tileWirelessBus) {
            final AENetworkProxy proxy = tileWirelessBus.getProxy();
            final EnumFacing opposite = newFacing.getOpposite();
            proxy.setValidSides(EnumSet.of(opposite));
        }
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onBlockActivated(final World worldIn, final BlockPos pos, final IBlockState state, final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) return true;
        if (!playerIn.isSneaking()) return true;
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        if (heldItem.isEmpty()) return true;

        final int[] oreIDs = OreDictionary.getOreIDs(heldItem);
        for (int oreID : oreIDs) {
            final String oreName = OreDictionary.getOreName(oreID);
            if (StringUtils.equals("itemQuartzWrench", oreName)) {
                worldIn.destroyBlock(pos, true);
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
