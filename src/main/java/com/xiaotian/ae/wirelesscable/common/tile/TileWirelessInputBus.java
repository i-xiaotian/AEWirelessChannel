package com.xiaotian.ae.wirelesscable.common.tile;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGridNode;
import com.xiaotian.ae.wirelesscable.common.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TileWirelessInputBus extends TileWirelessBus implements ITickable {

    private final ConnectionInfo currentConnection;

    public TileWirelessInputBus() {
        super();
        this.currentConnection = new ConnectionInfo();
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_INPUT_BUS);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        if (this.world.getTotalWorldTime() % 20 != 0) return;

        final boolean connect = currentConnection.isConnect();
        if (!connect) return;

        final boolean needUpdateGridNode = currentConnection.isNeedUpdateGridNode();
        if (!needUpdateGridNode) return;

        final IGridNode inputBusNode = this.getActionableNode();
        if (!inputBusNode.isActive()) return;

        final int targetX = currentConnection.getTargetX();
        final int targetY = currentConnection.getTargetY();
        final int targetZ = currentConnection.getTargetZ();
        TileEntity tileEntity = world.getTileEntity(new BlockPos(targetX, targetY, targetZ));
        if (Objects.isNull(tileEntity)) {
            currentConnection.setConnect(false);
            return;
        }

        if (!(tileEntity instanceof TileWirelessOutputBus tileWirelessOutputBus)) {
            currentConnection.setConnect(false);
            return;
        }

        currentConnection.setNeedUpdateGridNode(false);

        final IGridNode outputBusNode = tileWirelessOutputBus.getActionableNode();

        try {
            AEApi.instance().grid().createGridConnection(outputBusNode, inputBusNode);
        } catch (FailedConnectionException e) {
            throw new RuntimeException(e);
        }


    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        final String connectionKey = currentConnection.getConnectionKey();
        final int targetX = currentConnection.getTargetX();
        final int targetY = currentConnection.getTargetY();
        final int targetZ = currentConnection.getTargetZ();
        final boolean connect = currentConnection.isConnect();
        final boolean needUpdateGridNode = currentConnection.isNeedUpdateGridNode();
        nbtTagCompound.setString("connectionKey", connectionKey);
        nbtTagCompound.setInteger("targetX", targetX);
        nbtTagCompound.setInteger("targetY", targetY);
        nbtTagCompound.setInteger("targetZ", targetZ);
        nbtTagCompound.setBoolean("connect", connect);
        nbtTagCompound.setBoolean("needUpdateGridNode", needUpdateGridNode);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        final String connectionKey = compound.getString("connectionKey");
        final int targetX = compound.getInteger("targetX");
        final int targetY = compound.getInteger("targetY");
        final int targetZ = compound.getInteger("targetZ");
        final boolean connect = compound.getBoolean("connect");
        final boolean needUpdateGridNode = compound.getBoolean("needUpdateGridNode");
        currentConnection.setConnectionKey(connectionKey);
        currentConnection.setTargetX(targetX);
        currentConnection.setTargetY(targetY);
        currentConnection.setTargetZ(targetZ);
        currentConnection.setConnect(connect);
        currentConnection.setNeedUpdateGridNode(needUpdateGridNode);
    }

    public ConnectionInfo getCurrentConnection() {
        return this.currentConnection;
    }



}
