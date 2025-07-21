package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGridNode;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.xiaotian.ae.wirelesscable.AEWirelessChannel.log;

public class TileWirelessInputBus extends TileWirelessBus implements ITickable {

    private final ConnectionInfo currentConnection;

    private byte tickCounter = 0;

    public TileWirelessInputBus() {
        super();
        this.currentConnection = new ConnectionInfo(pos);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_INPUT_BUS);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        tickCounter++;
        if (tickCounter % 5 != 0) return;
        if (tickCounter >= 120) tickCounter = 0;
        super.update();

        final boolean connect = currentConnection.isConnect();
        if (!connect) return;

        final boolean needUpdateGridNode = currentConnection.isNeedUpdateGridNode();
        if (!needUpdateGridNode) return;

        final TileWirelessOutputBus wirelessOutputBus = getTileWirelessOutputBus();
        if (Objects.isNull(wirelessOutputBus)) {
            currentConnection.setConnect(false);
            return;
        }

        final IGridNode inputBusNode = this.getProxy().getNode();
        if (Objects.isNull(inputBusNode)) return;

        final IGridNode outputBusNode = wirelessOutputBus.getProxy().getNode();
        if (Objects.isNull(outputBusNode)) return;

        try {
            AEApi.instance().grid().createGridConnection(outputBusNode, inputBusNode);
            currentConnection.setInputBusX(pos.getX());
            currentConnection.setInputBusY(pos.getY());
            currentConnection.setInputBusZ(pos.getZ());
            currentConnection.setNeedUpdateGridNode(false);
            wirelessOutputBus.addConnectionInfo(currentConnection);
        } catch (FailedConnectionException e) {
            log.error(e.getMessage(), e);
            currentConnection.setConnect(false);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        final String connectionKey = currentConnection.getConnectionKey();
        final int inputBusX = pos.getX();
        final int inputBusY = pos.getY();
        final int inputBusZ = pos.getZ();
        final int outputBusX = currentConnection.getOutputBusX();
        final int outputBusY = currentConnection.getOutputBusY();
        final int outputBusZ = currentConnection.getOutputBusZ();
        final boolean connect = currentConnection.isConnect();
        nbtTagCompound.setString("connectionKey", connectionKey);
        nbtTagCompound.setInteger("outputBusX", outputBusX);
        nbtTagCompound.setInteger("outputBusY", outputBusY);
        nbtTagCompound.setInteger("outputBusZ", outputBusZ);
        nbtTagCompound.setInteger("inputBusX", inputBusX);
        nbtTagCompound.setInteger("inputBusY", inputBusY);
        nbtTagCompound.setInteger("inputBusZ", inputBusZ);
        nbtTagCompound.setBoolean("connect", connect);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        initConnectionInfo(compound, this);
    }

    public void initConnectionInfo(final NBTTagCompound compound, final TileWirelessInputBus wirelessInputBus) {
        final String connectionKey = compound.getString("connectionKey");
        final int outputBusX = compound.getInteger("outputBusX");
        final int outputBusY = compound.getInteger("outputBusY");
        final int outputBusZ = compound.getInteger("outputBusZ");
        final int inputBusX = pos.getX();
        final int inputBusY = pos.getY();
        final int inputBusZ = pos.getZ();
        final boolean connect = compound.getBoolean("connect");

        final ConnectionInfo currentConnection = wirelessInputBus.getCurrentConnection();
        currentConnection.setConnectionKey(connectionKey);
        currentConnection.setOutputBusX(outputBusX);
        currentConnection.setOutputBusY(outputBusY);
        currentConnection.setOutputBusZ(outputBusZ);
        currentConnection.setInputBusX(inputBusX);
        currentConnection.setInputBusY(inputBusY);
        currentConnection.setInputBusZ(inputBusZ);
        currentConnection.setConnect(connect);
        currentConnection.setNeedUpdateGridNode(true);
    }

    public ConnectionInfo getCurrentConnection() {
        return this.currentConnection;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        final TileWirelessOutputBus wirelessOutputBus = getTileWirelessOutputBus();
        if (Objects.isNull(wirelessOutputBus)) return;
        wirelessOutputBus.removeConnectionInfo(currentConnection);
    }

    private TileWirelessOutputBus getTileWirelessOutputBus() {
        final int targetX = currentConnection.getOutputBusX();
        final int targetY = currentConnection.getOutputBusY();
        final int targetZ = currentConnection.getOutputBusZ();
        final BlockPos blockPos = new BlockPos(targetX, targetY, targetZ);
        TileEntity outputBusEntity = world.getTileEntity(blockPos);
        if (Objects.isNull(outputBusEntity)) return null;
        if (!(outputBusEntity instanceof TileWirelessOutputBus wirelessOutputBus)) return null;
        return wirelessOutputBus;
    }

}
