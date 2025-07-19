package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.block.BlockBaseWirelessBus;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
public class TileWirelessInputBus extends TileWirelessBus implements ITickable {

    private final ConnectionInfo currentConnection;

    public TileWirelessInputBus() {
        super();
        this.currentConnection = new ConnectionInfo(this.pos);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_INPUT_BUS);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        if (this.world.getTotalWorldTime() % 20 != 0) return;
        super.update();

        final boolean connect = currentConnection.isConnect();
        if (!connect) return;

        final boolean needUpdateGridNode = currentConnection.isNeedUpdateGridNode();
        if (!needUpdateGridNode) return;

        final TileEntity inputBusEntity = world.getTileEntity(pos);
        if (!(inputBusEntity instanceof IGridHost inputBusGridHose)) {
            currentConnection.setConnect(false);
            return;
        }
        final IGridNode inputBusNode = inputBusGridHose.getGridNode(AEPartLocation.INTERNAL);
        if (Objects.isNull(inputBusNode)) return;

        final int targetX = currentConnection.getOutputBusX();
        final int targetY = currentConnection.getOutputBusY();
        final int targetZ = currentConnection.getOutputBusZ();
        final BlockPos blockPos = new BlockPos(targetX, targetY, targetZ);
        TileEntity outputBusEntity = world.getTileEntity(blockPos);
        if (Objects.isNull(outputBusEntity)) {
            currentConnection.setConnect(false);
            return;
        }

        if (!(outputBusEntity instanceof IGridHost outputBusGridHose)) {
            currentConnection.setConnect(false);
            return;
        }

        final IGridNode outputBusNode = outputBusGridHose.getGridNode(AEPartLocation.INTERNAL);
        if (Objects.isNull(outputBusNode)) return;

        try {
            AEApi.instance().grid().createGridConnection(outputBusNode, inputBusNode);
            currentConnection.setNeedUpdateGridNode(false);
        } catch (FailedConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        final String connectionKey = currentConnection.getConnectionKey();
        final int outputBusX = currentConnection.getOutputBusX();
        final int outputBusY = currentConnection.getOutputBusY();
        final int outputBusZ = currentConnection.getOutputBusZ();
        final boolean connect = currentConnection.isConnect();
        nbtTagCompound.setString("connectionKey", connectionKey);
        nbtTagCompound.setInteger("outputBusX", outputBusX);
        nbtTagCompound.setInteger("outputBusY", outputBusY);
        nbtTagCompound.setInteger("outputBusZ", outputBusZ);
        nbtTagCompound.setBoolean("connect", connect);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        final String connectionKey = compound.getString("connectionKey");
        final int outputBusX = compound.getInteger("outputBusX");
        final int outputBusY = compound.getInteger("outputBusY");
        final int outputBusZ = compound.getInteger("outputBusZ");
        final boolean connect = compound.getBoolean("connect");
        currentConnection.setConnectionKey(connectionKey);
        currentConnection.setOutputBusX(outputBusX);
        currentConnection.setOutputBusY(outputBusY);
        currentConnection.setOutputBusZ(outputBusZ);
        currentConnection.setConnect(connect);
        currentConnection.setNeedUpdateGridNode(true);
    }

    public ConnectionInfo getCurrentConnection() {
        return this.currentConnection;
    }

}
