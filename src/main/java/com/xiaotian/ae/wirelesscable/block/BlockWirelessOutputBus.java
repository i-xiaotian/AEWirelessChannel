package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.item.ItemBlockWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class BlockWirelessOutputBus extends BlockBaseWirelessBus implements ITileWithWireless {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.875, 0.75);

    public BlockWirelessOutputBus() {
        super(AbstractBlock.Properties.create(Material.GLASS));
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

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new TileWirelessOutputBus();
    }

    @Override
    @Nonnull
    public BlockItem createItemBlock(final Block block) {
        return new ItemBlockWirelessOutputBus(block);
    }

    @Override
    @Nonnull
    protected AxisAlignedBB getBlockBoundingBox() {
        return BOUNDING_BOX;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final PlayerEntity playerIn, final Hand hand) {
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        final Item item = heldItem.getItem();
        if (item != Items.ITEM_WIRELESS_KEY_CARD) return false;
        if (playerIn.isSneaking()) return false;
        if (worldIn.isRemote) return true;

        final CompoundNBT outputBusBound = writeOutputBusInfoTag(worldIn, pos);
        heldItem.setTag(outputBusBound);

        playerIn.sendMessage(new StringTextComponent(I18n.format("message.aewirelesschannel.bound_out_put_bus")), playerIn.getUniqueID());
        return true;
    }

    private static CompoundNBT writeOutputBusInfoTag(final World worldIn, final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final String dimension = worldIn.getDimensionKey().getRegistryName().getNamespace();
        final CompoundNBT outputBusBound = new CompoundNBT();
        final CompoundNBT outputBusPosNbt = new CompoundNBT();
        outputBusPosNbt.putInt("boundX", x);
        outputBusPosNbt.putInt("boundY", y);
        outputBusPosNbt.putInt("boundZ", z);
        outputBusPosNbt.putString("dimension", dimension);
        outputBusBound.put("outputBusBound", outputBusPosNbt);
        return outputBusBound;
    }

    @Override
    public boolean needInitTagFromItemStack() {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void initAEConnectionFromItemStackTag(final CompoundNBT tagCompound, final TileEntity tileEntity) {
        if (!(tileEntity instanceof TileWirelessOutputBus)) return;
        TileWirelessOutputBus wirelessOutputBus = (TileWirelessOutputBus) tileEntity;
        wirelessOutputBus.initConnectionMap(tagCompound, wirelessOutputBus);

        wirelessOutputBus.loadWirelessInfo(wirelessOutputBus);
    }
}
