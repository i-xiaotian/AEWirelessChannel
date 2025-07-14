package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import com.xiaotian.ae.wirelesscable.common.registry.BlockRegistry;
import com.xiaotian.ae.wirelesscable.common.tile.TileOutputBusWireless;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WirelessOutputBus extends Block implements ITileEntityProvider, IHasTileEntity {

    private static final String BLOCK_ID = "block_wireless_output_bus";
    private static final String TILE_ID = BLOCK_ID.replace("block_", "tile_");

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileOutputBusWireless.class;
    }

    @Override
    public String getTileEntityId() {
        return TILE_ID;
    }

    public WirelessOutputBus() {
        super(Material.GLASS);
        this.setSoundType(SoundType.GLASS);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessCable.MOD_ID, "block_wireless_output_bus"));
        this.setTranslationKey(AEWirelessCable.MOD_ID + ".block_wireless_output_bus");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new TileOutputBusWireless();
    }

    @Override
    public boolean onBlockActivated(final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) return true;
        playerIn.sendMessage(new TextComponentString("点了,15"));
        return true;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
//            TileEntity te = world.getTileEntity(pos);
//            if (te instanceof TileOutputBusWireless && placer instanceof EntityPlayer) {
//                TileOutputBusWireless tileWirelessOutputBus = (TileOutputBusWireless) te;
//                tileWirelessOutputBus.getActionableNode();
//            }
        }
    }
}
