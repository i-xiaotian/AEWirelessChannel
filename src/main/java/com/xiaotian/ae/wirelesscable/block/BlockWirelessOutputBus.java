package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.item.ItemBlockWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BlockWirelessOutputBus extends BlockBaseWirelessBus implements ITileEntityProvider, ITileWithWireless {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.875, 0.75);

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
    @Nonnull
    public ItemBlock createItemBlock(final Block block) {
        return new ItemBlockWirelessOutputBus(block);
    }

    @Override
    @Nonnull
    protected AxisAlignedBB getBlockBoundingBox() {
        return BOUNDING_BOX;
    }

    @Override
    protected boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final EntityPlayer playerIn, final EnumHand hand) {
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        final Item item = heldItem.getItem();
        if (item != Items.ITEM_WIRELESS_KEY_CARD) return false;
        if (playerIn.isSneaking()) return false;
        if (worldIn.isRemote) return true;

        final NBTTagCompound outputBusBound = writeOutputBusInfoTag(worldIn, pos);
        heldItem.setTagCompound(outputBusBound);

        playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.bound_out_put_bus")));
        return true;
    }

    private static NBTTagCompound writeOutputBusInfoTag(final World worldIn, final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final int dimension = worldIn.provider.getDimension();
        final NBTTagCompound outputBusBound = new NBTTagCompound();
        final NBTTagCompound outputBusPosNbt = new NBTTagCompound();
        outputBusPosNbt.setInteger("boundX", x);
        outputBusPosNbt.setInteger("boundY", y);
        outputBusPosNbt.setInteger("boundZ", z);
        outputBusPosNbt.setInteger("dimension", dimension);
        outputBusBound.setTag("outputBusBound", outputBusPosNbt);
        return outputBusBound;
    }

    @Override
    public boolean needInitTagFromItemStack() {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void initAEConnectionFromItemStackTag(final NBTTagCompound tagCompound, final TileEntity tileEntity) {
        if (!(tileEntity instanceof TileWirelessOutputBus wirelessOutputBus)) return;
        wirelessOutputBus.initConnectionMap(tagCompound, wirelessOutputBus);

        wirelessOutputBus.loadWirelessInfo(wirelessOutputBus);
    }
}
