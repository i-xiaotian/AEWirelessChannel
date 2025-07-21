package com.xiaotian.ae.wirelesscable.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Objects;

@SuppressWarnings("deprecation")
public abstract class BlockBaseWirelessBus extends BlockBaseBus {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockBaseWirelessBus(final Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(POWERED, false));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, POWERED);
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 0b111);
        boolean powered = (meta & 0b1000) != 0;
        return this.getDefaultState()
                .withProperty(FACING, facing)
                .withProperty(POWERED, powered);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        if (state.getValue(POWERED)) {
            meta |= 0b1000;
        }
        return meta;
    }

    @Override
    @Nonnull
    public IBlockState getStateForPlacement(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final EnumFacing clickedFace,
                                            final float hitX, final float hitY, final float hitZ,
                                            final int meta, @Nonnull final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, clickedFace);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {

            Block block = world.getBlockState(pos).getBlock();
            TileEntity tileEntity = world.getTileEntity(pos);
            if (Objects.isNull(tileEntity)) return;
            if (block instanceof ITileWithWireless iTileWithWireless) {
                final boolean needInitTagFromItemStack = iTileWithWireless.needInitTagFromItemStack();
                if (needInitTagFromItemStack) {
                    final NBTTagCompound tagCompound = stack.getTagCompound();
                    if (Objects.nonNull(tagCompound)) iTileWithWireless.initAEConnectionFromItemStackTag(tagCompound, tileEntity);
                }
            }
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

        if (actionWithConnectionCard(worldIn, pos, playerIn, hand)) return true;
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        if (!heldItem.isEmpty()) {
            final int[] oreIDs = OreDictionary.getOreIDs(heldItem);
            for (int oreID : oreIDs) {
                final String oreName = OreDictionary.getOreName(oreID);
                if (StringUtils.equals("itemQuartzWrench", oreName)) {
                    if (worldIn.isRemote) return true;

                    if (!playerIn.isSneaking()) return false;
                    TileEntity tile = worldIn.getTileEntity(pos);
                    if (Objects.isNull(tile)) return false;
                    final NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    tile.writeToNBT(nbtTagCompound);
                    final Block blockType = tile.getBlockType();

                    worldIn.setBlockToAir(pos);
                    final ItemStack itemStack = new ItemStack(blockType);
                    itemStack.setTagCompound(nbtTagCompound);
                    EntityItem entityItem = new EntityItem(worldIn,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            itemStack
                    );
                    entityItem.setDefaultPickupDelay();
                    worldIn.spawnEntity(entityItem);
                    return true;
                }
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return this.rotateAABB(facing);
    }

    protected abstract boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final EntityPlayer playerIn, final EnumHand hand);

}
