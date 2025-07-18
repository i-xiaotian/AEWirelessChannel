package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.common.registry.Items;
import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessOutputBus;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class BlockWirelessOutputBus extends BlockBaseWirelessBus implements ITileEntityProvider, IHasTileEntity {

    public BlockWirelessOutputBus() {
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
    @ParametersAreNonnullByDefault
    public boolean onBlockActivated(final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) return true;
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        final Item item = heldItem.getItem();
        if (item == Items.ITEM_WIRELESS_KEY_CARD) {
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();
            final NBTTagCompound outputBusBound = new NBTTagCompound();
            final NBTTagCompound outputBusPosNbt = new NBTTagCompound();
            outputBusPosNbt.setInteger("boundX", x);
            outputBusPosNbt.setInteger("boundY", y);
            outputBusPosNbt.setInteger("boundZ", z);
            outputBusBound.setTag("outputBusBound", outputBusPosNbt);
            heldItem.setTagCompound(outputBusBound);

            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.bound_out_put_bus")));
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

}
