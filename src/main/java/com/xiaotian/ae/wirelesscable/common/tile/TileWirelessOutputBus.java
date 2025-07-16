package com.xiaotian.ae.wirelesscable.common.tile;

import com.xiaotian.ae.wirelesscable.common.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TileWirelessOutputBus extends TileWirelessBus {

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
            final int targetX = connectionInfo.getTargetX();
            final int targetY = connectionInfo.getTargetY();
            final int targetZ = connectionInfo.getTargetZ();
            final boolean connect = connectionInfo.isConnect();
            final boolean needUpdateGridNode = connectionInfo.isNeedUpdateGridNode();
            subNodeTag.setString("connectionKey", connectionKey);
            subNodeTag.setInteger("targetX", targetX);
            subNodeTag.setInteger("targetY", targetY);
            subNodeTag.setInteger("targetZ", targetZ);
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
            final int targetX = subNodeTag.getInteger("targetX");
            final int targetY = subNodeTag.getInteger("targetY");
            final int targetZ = subNodeTag.getInteger("targetZ");
            final boolean connect = subNodeTag.getBoolean("connect");
            final boolean needUpdateGridNode = subNodeTag.getBoolean("needUpdateGridNode");
            final ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.setConnectionKey(connectionKey);
            connectionInfo.setTargetX(targetX);
            connectionInfo.setTargetY(targetY);
            connectionInfo.setTargetZ(targetZ);
            connectionInfo.setConnect(connect);
            connectionInfo.setNeedUpdateGridNode(needUpdateGridNode);
            subGridConnectionMap.put(setKey, connectionInfo);
        }
    }
}
