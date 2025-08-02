package com.xiaotian.ae.wirelesscable.block;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.item.ItemBlockWirelessInputBus;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
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
import java.util.Objects;

public class BlockWirelessInputBus extends BlockBaseWirelessBus implements ITileWithWireless {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.875, 0.75);

    public BlockWirelessInputBus() {
        super(Properties.of(Material.GLASS));
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

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new TileWirelessInputBus();
    }

    @Override
    @Nonnull
    public BlockItem createItemBlock(final Block block) {
        return new ItemBlockWirelessInputBus(block);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final PlayerEntity playerIn, final Hand hand) {
        final ItemStack heldItem = playerIn.getItemInHand(hand);
        final Item item = heldItem.getItem();
        if (item != Items.ITEM_WIRELESS_KEY_CARD) return false;
        if (playerIn.isCrouching()) return false;
        if (worldIn.isClientSide) return true;

        final CompoundNBT tagCompound = heldItem.getTag();
        if (Objects.isNull(tagCompound)) {
            playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.not_bound")), playerIn.getUUID());
            return false;
        }
        final CompoundNBT outputBusBound = tagCompound.getCompound("outputBusBound");
        final int boundX = outputBusBound.getInt("boundX");
        final int boundY = outputBusBound.getInt("boundY");
        final int boundZ = outputBusBound.getInt("boundZ");
        final BlockPos blockPos = new BlockPos(boundX, boundY, boundZ);
        final TileEntity tileEntity = worldIn.getBlockEntity(blockPos);
        if (Objects.isNull(tileEntity)) {
            playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.not_bound")), playerIn.getUUID());
            return false;
        }
        if (!(tileEntity instanceof TileWirelessOutputBus)) {
            playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.bound_type_error")), playerIn.getUUID());
            return false;
        }
        TileWirelessOutputBus tileWirelessOutputBus = (TileWirelessOutputBus) tileEntity;
        final IGridNode actionableNode = tileWirelessOutputBus.getActionableNode();
        final IGrid grid = actionableNode.getGrid();
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        if (!securityGrid.hasPermission(playerIn, SecurityPermissions.BUILD)) {
            playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.no_permission")), playerIn.getUUID());
            return false;
        }

        final TileEntity inputBus = worldIn.getBlockEntity(pos);
        if (!(inputBus instanceof TileWirelessInputBus)) {
            playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.bound_type_error_input")), playerIn.getUUID());
            return false;
        }
        TileWirelessInputBus tileWirelessInputBus = (TileWirelessInputBus) inputBus;
        final ConnectionInfo currentConnection = tileWirelessInputBus.getCurrentConnection();
        final boolean connect = currentConnection.isConnect();
        if (connect) {
            tileWirelessInputBus.onChunkUnloaded();
            tileWirelessInputBus.onLoad();
        }
        currentConnection.setConnect(true);
        currentConnection.setNeedUpdateGridNode(true);
        currentConnection.setOutputBusX(boundX);
        currentConnection.setOutputBusY(boundY);
        currentConnection.setOutputBusZ(boundZ);
        tileWirelessOutputBus.addConnectionInfo(currentConnection);
        playerIn.sendMessage(new StringTextComponent(I18n.get("message.aewirelesschannel.bound_in_put_bus")), playerIn.getUUID());
        return true;

    }

    @Override
    @Nonnull
    protected AxisAlignedBB getBlockBoundingBox() {
        return BOUNDING_BOX;
    }

    @Override
    public boolean needInitTagFromItemStack() {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void initAEConnectionFromItemStackTag(final CompoundNBT tagCompound, final TileEntity tileEntity) {
        if (!(tileEntity instanceof TileWirelessInputBus)) return;
        TileWirelessInputBus tileWirelessInputBus = (TileWirelessInputBus) tileEntity;
        tileWirelessInputBus.initConnectionInfo(tagCompound, tileWirelessInputBus);
    }
}
