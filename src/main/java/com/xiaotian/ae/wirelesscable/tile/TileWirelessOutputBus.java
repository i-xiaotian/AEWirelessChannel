package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TileWirelessOutputBus extends TileWirelessBus implements ITickable {

    private final Map<String, ConnectionInfo> subGridConnectionMap;

    public TileWirelessOutputBus() {
        super();
        subGridConnectionMap = new HashMap<>();
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_OUTPUT_BUS);
    }

    public Map<String, ConnectionInfo> getSubGridConnectionMap() {
        return subGridConnectionMap;
    }

    @Nonnull
    @Override
    @SuppressWarnings("DuplicatedCode")
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {

        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        if (subGridConnectionMap.isEmpty()) return nbtTagCompound;

        final NBTTagCompound connectionInfoTag = new NBTTagCompound();
        final Set<String> subNodeKeySet = subGridConnectionMap.keySet();
        for (String setKey : subNodeKeySet) {

            final NBTTagCompound subNodeTag = new NBTTagCompound();

            final ConnectionInfo connectionInfo = subGridConnectionMap.get(setKey);
            final String connectionKey = connectionInfo.getConnectionKey();
            final int outputBusX = connectionInfo.getOutputBusX();
            final int outputBusY = connectionInfo.getOutputBusY();
            final int outputBusZ = connectionInfo.getOutputBusZ();
            final boolean connect = connectionInfo.isConnect();
            final boolean needUpdateGridNode = connectionInfo.isNeedUpdateGridNode();
            subNodeTag.setString("connectionKey", connectionKey);
            subNodeTag.setInteger("outputBusX", outputBusX);
            subNodeTag.setInteger("outputBusY", outputBusY);
            subNodeTag.setInteger("outputBusZ", outputBusZ);
            subNodeTag.setBoolean("connect", connect);
            subNodeTag.setBoolean("needUpdateGridNode", needUpdateGridNode);

            connectionInfoTag.setTag(connectionKey, subNodeTag);
        }
        nbtTagCompound.setTag("connectionInfoTag", connectionInfoTag);
        return nbtTagCompound;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (!compound.hasKey("connectionInfoTag")) return;
        final NBTTagCompound connectionInfoTag = compound.getCompoundTag("connectionInfoTag");
        subGridConnectionMap.clear();
        final Set<String> subNodeKeySet = connectionInfoTag.getKeySet();
        for (String setKey : subNodeKeySet) {
            final NBTTagCompound subNodeTag = connectionInfoTag.getCompoundTag(setKey);
            final String connectionKey = subNodeTag.getString("connectionKey");
            final int outputBusX = subNodeTag.getInteger("outputBusX");
            final int outputBusY = subNodeTag.getInteger("outputBusY");
            final int outputBusZ = subNodeTag.getInteger("outputBusZ");
            final boolean connect = subNodeTag.getBoolean("connect");
            final boolean needUpdateGridNode = subNodeTag.getBoolean("needUpdateGridNode");
            final ConnectionInfo connectionInfo = new ConnectionInfo(pos);
            connectionInfo.setConnectionKey(connectionKey);
            connectionInfo.setOutputBusX(outputBusX);
            connectionInfo.setOutputBusY(outputBusY);
            connectionInfo.setOutputBusZ(outputBusZ);
            connectionInfo.setConnect(connect);
            connectionInfo.setNeedUpdateGridNode(needUpdateGridNode);
            subGridConnectionMap.put(setKey, connectionInfo);
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;
        super.update();
    }
}
