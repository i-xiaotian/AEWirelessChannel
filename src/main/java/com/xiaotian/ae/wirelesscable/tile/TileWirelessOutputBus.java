package com.xiaotian.ae.wirelesscable.tile;

import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TileWirelessOutputBus extends TileWirelessBus implements ITickable {

    private final Map<String, ConnectionInfo> subGridConnectionMap;

    public TileWirelessOutputBus() {
        super();
        subGridConnectionMap = new HashMap<>();
    }

    public void addConnectionInfo(@Nonnull final ConnectionInfo info) {
        subGridConnectionMap.put(info.getConnectionKey(), info);
    }

    public void removeConnectionInfo(@Nonnull final ConnectionInfo info) {
        subGridConnectionMap.remove(info.getConnectionKey());
    }

    public Map<String, ConnectionInfo> getSubGridConnectionMap() {
        return subGridConnectionMap;
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_OUTPUT_BUS);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {

        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        if (subGridConnectionMap.isEmpty()) return nbtTagCompound;

        final NBTTagCompound connectionInfoTag = new NBTTagCompound();
        final Set<String> subNodeKeySet = subGridConnectionMap.keySet();
        for (String setKey : subNodeKeySet) {

            final NBTTagCompound subNodeTag = new NBTTagCompound();

            final ConnectionInfo connectionInfo = subGridConnectionMap.get(setKey);
            final String connectionKey = connectionInfo.getConnectionKey();
            final int inputBusX = connectionInfo.getInputBusX();
            final int inputBusY = connectionInfo.getInputBusY();
            final int inputBusZ = connectionInfo.getInputBusZ();
            final boolean connect = connectionInfo.isConnect();
            final boolean needUpdateGridNode = connectionInfo.isNeedUpdateGridNode();
            subNodeTag.setString("connectionKey", connectionKey);
            subNodeTag.setInteger("inputBusX", inputBusX);
            subNodeTag.setInteger("inputBusY", inputBusY);
            subNodeTag.setInteger("inputBusZ", inputBusZ);
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
        initConnectionMap(compound, this);
    }

    public void initConnectionMap(final NBTTagCompound compound, final TileWirelessOutputBus wirelessOutputBus) {
        if (!compound.hasKey("connectionInfoTag")) return;
        final NBTTagCompound connectionInfoTag = compound.getCompoundTag("connectionInfoTag");
        final Set<String> subNodeKeySet = connectionInfoTag.getKeySet();
        for (String setKey : subNodeKeySet) {
            final NBTTagCompound subNodeTag = connectionInfoTag.getCompoundTag(setKey);
            final String connectionKey = subNodeTag.getString("connectionKey");
            final int inputBusX = subNodeTag.getInteger("inputBusX");
            final int inputBusY = subNodeTag.getInteger("inputBusY");
            final int inputBusZ = subNodeTag.getInteger("inputBusZ");
            final boolean connect = subNodeTag.getBoolean("connect");
            final boolean needUpdateGridNode = subNodeTag.getBoolean("needUpdateGridNode");
            final ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.setConnectionKey(connectionKey);
            connectionInfo.setInputBusX(inputBusX);
            connectionInfo.setInputBusY(inputBusY);
            connectionInfo.setInputBusZ(inputBusZ);
            connectionInfo.setConnect(connect);
            connectionInfo.setNeedUpdateGridNode(needUpdateGridNode);

            wirelessOutputBus.addConnectionInfo(connectionInfo);
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;
        super.update();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.loadWirelessInfo(this);
    }

    public void loadWirelessInfo(final TileWirelessOutputBus wirelessOutputBus) {
        final World world = wirelessOutputBus.getWorld();
        final BlockPos pos = wirelessOutputBus.getPos();

        final Set<String> connectionKeySet = subGridConnectionMap.keySet();
        for (String connectionKey : connectionKeySet) {
            final BlockPos busPosInfo = getConnectionInputBusPosInfo(connectionKey);
            final TileEntity inputTileEntity = world.getTileEntity(busPosInfo);
            if (Objects.isNull(inputTileEntity)) continue;
            if (!(inputTileEntity instanceof TileWirelessInputBus wirelessInputBus)) continue;
            final ConnectionInfo inputBusConnectInfo = wirelessInputBus.getCurrentConnection();
            if (Objects.isNull(inputBusConnectInfo)) continue;

            final int outputBusX = inputBusConnectInfo.getOutputBusX();
            final int outputBusY = inputBusConnectInfo.getOutputBusY();
            final int outputBusZ = inputBusConnectInfo.getOutputBusZ();
            final boolean connect = inputBusConnectInfo.isConnect();
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();
            boolean sameOutputBusPos = outputBusX == x && outputBusY == y && outputBusZ == z;

            if (sameOutputBusPos && connect) continue;

            inputBusConnectInfo.setOutputBusX(x);
            inputBusConnectInfo.setOutputBusY(y);
            inputBusConnectInfo.setOutputBusZ(z);
            inputBusConnectInfo.setConnect(true);
            inputBusConnectInfo.setNeedUpdateGridNode(true);
        }
    }

    public BlockPos getConnectionInputBusPosInfo(final String connectionKey) {
        final ConnectionInfo connectionInfo = subGridConnectionMap.get(connectionKey);
        if (Objects.isNull(connectionInfo)) return new BlockPos(0, 0, 0);
        final int inputBusX = connectionInfo.getInputBusX();
        final int inputBusY = connectionInfo.getInputBusY();
        final int inputBusZ = connectionInfo.getInputBusZ();

        return new BlockPos(inputBusX, inputBusY, inputBusZ);
    }
}
