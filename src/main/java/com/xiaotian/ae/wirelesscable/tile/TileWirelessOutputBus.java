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
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class TileWirelessOutputBus extends TileWirelessBus implements ITickable {

    private static final String OUTPUT_BUS_ID_KEY = "wirelessOutputBusId";
    private static final Map<World, Map<String, WeakReference<TileWirelessOutputBus>>> LOADED_OUTPUT_BUSES = new WeakHashMap<>();

    private final Map<String, ConnectionInfo> subGridConnectionMap;
    private String outputBusId = UUID.randomUUID().toString();

    public TileWirelessOutputBus() {
        super();
        subGridConnectionMap = new HashMap<>();
    }

    public void addConnectionInfo(@Nonnull final ConnectionInfo info) {
        if (subGridConnectionMap.put(info.getConnectionKey(), info) != info) markDirty();
    }

    public void removeConnectionInfo(@Nonnull final ConnectionInfo info) {
        if (subGridConnectionMap.remove(info.getConnectionKey()) != null) markDirty();
    }

    public int disconnectConnections() {
        final List<ConnectionInfo> connections = new ArrayList<>(subGridConnectionMap.values());
        int disconnected = 0;
        for (ConnectionInfo connection : connections) {
            if (connection == null) continue;
            final BlockPos inputPos = getConnectionInputBusPosInfo(connection.getConnectionKey());
            if (!world.isBlockLoaded(inputPos)) {
                if (connection.isConnect()) disconnected++;
                removeConnectionInfo(connection);
                continue;
            }

            final TileEntity inputEntity = world.getTileEntity(inputPos);
            if (inputEntity instanceof TileWirelessInputBus) {
                final TileWirelessInputBus inputBus = (TileWirelessInputBus) inputEntity;
                final ConnectionInfo inputConnection = inputBus.getCurrentConnection();
                if (inputConnection.isConnect()
                        && connection.getConnectionKey().equals(inputConnection.getConnectionKey())
                        && inputConnection.getOutputBusX() == pos.getX()
                        && inputConnection.getOutputBusY() == pos.getY()
                        && inputConnection.getOutputBusZ() == pos.getZ()) {
                    disconnected++;
                    inputBus.clearConnectionConfiguration();
                }
            }
            removeConnectionInfo(connection);
        }
        return disconnected;
    }

    public Map<String, ConnectionInfo> getSubGridConnectionMap() {
        return subGridConnectionMap;
    }

    public String getOutputBusId() {
        return outputBusId;
    }

    public static TileWirelessOutputBus findLoaded(final World world, final String outputBusId) {
        final Map<String, WeakReference<TileWirelessOutputBus>> worldBuses = LOADED_OUTPUT_BUSES.get(world);
        if (worldBuses == null) return null;
        final WeakReference<TileWirelessOutputBus> reference = worldBuses.get(outputBusId);
        final TileWirelessOutputBus bus = reference == null ? null : reference.get();
        return bus == null || bus.isInvalid() ? null : bus;
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Blocks.WIRELESS_OUTPUT_BUS);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {

        final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        nbtTagCompound.setString(OUTPUT_BUS_ID_KEY, outputBusId);
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
        loadOutputBusId(compound);
        initConnectionMap(compound, this);
    }

    public void initConnectionMap(final NBTTagCompound compound, final TileWirelessOutputBus wirelessOutputBus) {
        wirelessOutputBus.loadOutputBusId(compound);
        wirelessOutputBus.registerLoadedInstance();
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
            connectionInfo.setNeedUpdateGridNode(connect || needUpdateGridNode);

            wirelessOutputBus.subGridConnectionMap.put(connectionKey, connectionInfo);
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;
        loadWirelessInfo(this);
        super.update();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerLoadedInstance();
    }

    @Override
    public void onChunkUnload() {
        unregisterLoadedInstance();
        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        unregisterLoadedInstance();
        super.invalidate();
    }

    public void loadWirelessInfo(final TileWirelessOutputBus wirelessOutputBus) {
        final World world = wirelessOutputBus.getWorld();
        final BlockPos pos = wirelessOutputBus.getPos();

        final Set<String> connectionKeySet = new HashSet<>(subGridConnectionMap.keySet());
        for (String connectionKey : connectionKeySet) {
            final ConnectionInfo storedConnection = subGridConnectionMap.get(connectionKey);
            if (storedConnection == null) continue;
            final BlockPos busPosInfo = getConnectionInputBusPosInfo(connectionKey);
            if (!world.isBlockLoaded(busPosInfo)) continue;
            final TileEntity inputTileEntity = world.getTileEntity(busPosInfo);
            if (Objects.isNull(inputTileEntity)) continue;
            if (!(inputTileEntity instanceof TileWirelessInputBus)) continue;
            final TileWirelessInputBus wirelessInputBus = (TileWirelessInputBus) inputTileEntity;
            final ConnectionInfo inputBusConnectInfo = wirelessInputBus.getCurrentConnection();
            if (Objects.isNull(inputBusConnectInfo)) continue;

            if (!inputBusConnectInfo.isConnect()) {
                removeConnectionInfo(storedConnection);
                continue;
            }

            final int outputBusX = inputBusConnectInfo.getOutputBusX();
            final int outputBusY = inputBusConnectInfo.getOutputBusY();
            final int outputBusZ = inputBusConnectInfo.getOutputBusZ();
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();
            boolean sameOutputBusPos = outputBusX == x && outputBusY == y && outputBusZ == z;

            if (sameOutputBusPos) {
                if (storedConnection.isNeedUpdateGridNode()) {
                    inputBusConnectInfo.setNeedUpdateGridNode(true);
                    wirelessInputBus.markConnectionRequested();
                    wirelessInputBus.markDirty();
                }
                continue;
            }

            inputBusConnectInfo.setOutputBusX(x);
            inputBusConnectInfo.setOutputBusY(y);
            inputBusConnectInfo.setOutputBusZ(z);
            inputBusConnectInfo.setConnect(true);
            inputBusConnectInfo.setNeedUpdateGridNode(true);
            wirelessInputBus.markConnectionRequested();
            wirelessInputBus.markDirty();
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

    private void loadOutputBusId(final NBTTagCompound compound) {
        if (compound.hasKey(OUTPUT_BUS_ID_KEY)) {
            final String storedId = compound.getString(OUTPUT_BUS_ID_KEY);
            if (!storedId.isEmpty() && !storedId.equals(outputBusId)) {
                unregisterLoadedInstance();
                outputBusId = storedId;
            }
        }
    }

    private void registerLoadedInstance() {
        if (world == null || world.isRemote) return;
        LOADED_OUTPUT_BUSES.computeIfAbsent(world, ignored -> new HashMap<>())
                .put(outputBusId, new WeakReference<>(this));
    }

    private void unregisterLoadedInstance() {
        if (world == null || world.isRemote) return;
        final Map<String, WeakReference<TileWirelessOutputBus>> worldBuses = LOADED_OUTPUT_BUSES.get(world);
        if (worldBuses == null) return;
        final WeakReference<TileWirelessOutputBus> reference = worldBuses.get(outputBusId);
        if (reference == null || reference.get() != this) return;
        worldBuses.remove(outputBusId);
        if (worldBuses.isEmpty()) LOADED_OUTPUT_BUSES.remove(world);
    }
}
