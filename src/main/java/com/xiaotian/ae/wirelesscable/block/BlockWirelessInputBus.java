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
import java.util.Objects;

public class BlockWirelessInputBus extends BlockBaseWirelessBus implements ITileEntityProvider, ITileWithWireless {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.875, 0.75);

    public BlockWirelessInputBus() {
        super(Material.GLASS);
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new TileWirelessInputBus();
    }

    @Override
    @Nonnull
    public ItemBlock createItemBlock(final Block block) {
        return new ItemBlockWirelessInputBus(block);
    }

    @Override
    protected boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final EntityPlayer playerIn, final EnumHand hand) {
        final ItemStack heldItem = playerIn.getHeldItem(hand);
        final Item item = heldItem.getItem();
        if (item != Items.ITEM_WIRELESS_KEY_CARD) return false;
        if (playerIn.isSneaking()) return false;
        if (worldIn.isRemote) return true;

        final NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (Objects.isNull(tagCompound)) {
            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.not_bound")));
            return false;
        }
        final NBTTagCompound outputBusBound = tagCompound.getCompoundTag("outputBusBound");
        final int boundX = outputBusBound.getInteger("boundX");
        final int boundY = outputBusBound.getInteger("boundY");
        final int boundZ = outputBusBound.getInteger("boundZ");
        final BlockPos blockPos = new BlockPos(boundX, boundY, boundZ);
        final TileEntity tileEntity = worldIn.getTileEntity(blockPos);
        if (Objects.isNull(tileEntity)) {
            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.not_bound")));
            return false;
        }
        if (!(tileEntity instanceof TileWirelessOutputBus tileWirelessOutputBus)) {
            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.bound_type_error")));
            return false;
        }
        final IGridNode actionableNode = tileWirelessOutputBus.getActionableNode();
        final IGrid grid = actionableNode.getGrid();
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        if (!securityGrid.hasPermission(playerIn, SecurityPermissions.BUILD)) {
            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.no_permission")));
            return false;
        }

        final TileEntity inputBus = worldIn.getTileEntity(pos);
        if (!(inputBus instanceof TileWirelessInputBus tileWirelessInputBus)) {
            playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.bound_type_error_input")));
            return false;
        }
        final ConnectionInfo currentConnection = tileWirelessInputBus.getCurrentConnection();
        final boolean connect = currentConnection.isConnect();
        if (connect) {
            tileWirelessInputBus.invalidate();
            tileWirelessInputBus.validate();
        }
        currentConnection.setConnect(true);
        currentConnection.setNeedUpdateGridNode(true);
        currentConnection.setOutputBusX(boundX);
        currentConnection.setOutputBusY(boundY);
        currentConnection.setOutputBusZ(boundZ);

        playerIn.sendMessage(new TextComponentString(I18n.format("message.aewirelesschannel.bound_in_put_bus")));
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
    public void initAEConnectionFromItemStackTag(final NBTTagCompound tagCompound, final TileEntity tileEntity) {
        if (!(tileEntity instanceof TileWirelessInputBus tileWirelessInputBus)) return;
        tileWirelessInputBus.initConnectionInfo(tagCompound, tileWirelessInputBus);
    }
}
