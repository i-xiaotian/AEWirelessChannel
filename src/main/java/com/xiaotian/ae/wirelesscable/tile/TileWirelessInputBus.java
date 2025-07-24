package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGridNode;
import appeng.core.Api;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import com.xiaotian.ae.wirelesscable.registry.TileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static com.xiaotian.ae.wirelesscable.AEWirelessChannel.log;

public class TileWirelessInputBus extends TileWirelessBus implements ITickableTileEntity {

    private ConnectionInfo currentConnection;

    public TileWirelessInputBus() {
        super(TileEntityTypes.TILE_WIRELESS_INPUT_BUS);
        this.currentConnection = new ConnectionInfo(pos);
    }

    public TileWirelessInputBus(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_INPUT_BUS);
    }

    @Override
    public void tick() {
        if (Objects.isNull(world)) return;
        if (world.isRemote) return;
        tickCounter++;
        if (tickCounter % 5 != 0) return;
        if (tickCounter >= 120) tickCounter = 0;
        super.tick();

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
            Api.instance().grid().createGridConnection(outputBusNode, inputBusNode);
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
    public CompoundNBT write(@Nonnull final CompoundNBT compound) {
        final CompoundNBT nbtTagCompound = super.write(compound);
        final String connectionKey = currentConnection.getConnectionKey();
        final int inputBusX = pos.getX();
        final int inputBusY = pos.getY();
        final int inputBusZ = pos.getZ();
        final int outputBusX = currentConnection.getOutputBusX();
        final int outputBusY = currentConnection.getOutputBusY();
        final int outputBusZ = currentConnection.getOutputBusZ();
        final boolean connect = currentConnection.isConnect();
        nbtTagCompound.putString("connectionKey", connectionKey);
        nbtTagCompound.putInt("outputBusX", outputBusX);
        nbtTagCompound.putInt("outputBusY", outputBusY);
        nbtTagCompound.putInt("outputBusZ", outputBusZ);
        nbtTagCompound.putInt("inputBusX", inputBusX);
        nbtTagCompound.putInt("inputBusY", inputBusY);
        nbtTagCompound.putInt("inputBusZ", inputBusZ);
        nbtTagCompound.putBoolean("connect", connect);
        return nbtTagCompound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void read(final BlockState state, final CompoundNBT compound) {
        super.read(state, compound);
        initConnectionInfo(compound, this);
    }

    public void initConnectionInfo(final CompoundNBT compound, final TileWirelessInputBus wirelessInputBus) {
        final String connectionKey = compound.getString("connectionKey");
        final int outputBusX = compound.getInt("outputBusX");
        final int outputBusY = compound.getInt("outputBusY");
        final int outputBusZ = compound.getInt("outputBusZ");
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
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        final TileWirelessOutputBus wirelessOutputBus = getTileWirelessOutputBus();
        if (Objects.isNull(wirelessOutputBus)) return;
        wirelessOutputBus.removeConnectionInfo(currentConnection);
    }

    private TileWirelessOutputBus getTileWirelessOutputBus() {
        if (Objects.isNull(world)) return null;
        final int targetX = currentConnection.getOutputBusX();
        final int targetY = currentConnection.getOutputBusY();
        final int targetZ = currentConnection.getOutputBusZ();
        final BlockPos blockPos = new BlockPos(targetX, targetY, targetZ);
        TileEntity outputBusEntity = world.getTileEntity(blockPos);
        if (Objects.isNull(outputBusEntity)) return null;
        if (!(outputBusEntity instanceof TileWirelessOutputBus)) return null;
        return (TileWirelessOutputBus) outputBusEntity;
    }

}
