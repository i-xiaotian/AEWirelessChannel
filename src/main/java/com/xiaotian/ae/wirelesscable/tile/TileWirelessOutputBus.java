package com.xiaotian.ae.wirelesscable.tile;

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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TileWirelessOutputBus extends TileWirelessBus implements ITickableTileEntity {

    private final Map<String, ConnectionInfo> subGridConnectionMap;

    public TileWirelessOutputBus() {
        super(TileEntityTypes.TILE_WIRELESS_OUTPUT_BUS);
        subGridConnectionMap = new HashMap<>();
    }

    public TileWirelessOutputBus(TileEntityType<?> type) {
        super(type);
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
    public CompoundNBT save(@Nonnull final CompoundNBT compound) {
        final CompoundNBT nbtTagCompound = super.save(compound);
        if (subGridConnectionMap.isEmpty()) return nbtTagCompound;

        final CompoundNBT connectionInfoTag = new CompoundNBT();
        final Set<String> subNodeKeySet = subGridConnectionMap.keySet();
        for (String setKey : subNodeKeySet) {

            final CompoundNBT subNodeTag = new CompoundNBT();
            final ConnectionInfo connectionInfo = subGridConnectionMap.get(setKey);
            final String connectionKey = connectionInfo.getConnectionKey();
            final int inputBusX = connectionInfo.getInputBusX();
            final int inputBusY = connectionInfo.getInputBusY();
            final int inputBusZ = connectionInfo.getInputBusZ();
            final boolean connect = connectionInfo.isConnect();
            final boolean needUpdateGridNode = connectionInfo.isNeedUpdateGridNode();
            subNodeTag.putString("connectionKey", connectionKey);
            subNodeTag.putInt("inputBusX", inputBusX);
            subNodeTag.putInt("inputBusY", inputBusY);
            subNodeTag.putInt("inputBusZ", inputBusZ);
            subNodeTag.putBoolean("connect", connect);
            subNodeTag.putBoolean("needUpdateGridNode", needUpdateGridNode);

            connectionInfoTag.put(connectionKey, subNodeTag);
        }
        nbtTagCompound.put("connectionInfoTag", connectionInfoTag);
        return nbtTagCompound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(final BlockState state, final CompoundNBT compound) {
        super.load(state, compound);
        initConnectionMap(compound, this);
    }

    public void initConnectionMap(final CompoundNBT compound, final TileWirelessOutputBus wirelessOutputBus) {
        if (!compound.contains("connectionInfoTag")) return;
        final CompoundNBT connectionInfoTag = compound.getCompound("connectionInfoTag");
        final Set<String> subNodeKeySet = connectionInfoTag.getAllKeys();
        for (String setKey : subNodeKeySet) {
            final CompoundNBT subNodeTag = connectionInfoTag.getCompound(setKey);
            final String connectionKey = subNodeTag.getString("connectionKey");
            final int inputBusX = subNodeTag.getInt("inputBusX");
            final int inputBusY = subNodeTag.getInt("inputBusY");
            final int inputBusZ = subNodeTag.getInt("inputBusZ");
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
    public void tick() {
        if (Objects.isNull(level)) return;
        if (level.isClientSide) return;
        if (level.getGameTime() % 20 != 0) return;
        super.tick();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.loadWirelessInfo(this);
    }

    public void loadWirelessInfo(final TileWirelessOutputBus wirelessOutputBus) {
        final World world = wirelessOutputBus.getLevel();
        if (Objects.isNull(world)) return;
        final BlockPos pos = wirelessOutputBus.getBlockPos();

        final Set<String> connectionKeySet = subGridConnectionMap.keySet();
        for (String connectionKey : connectionKeySet) {
            final BlockPos busPosInfo = getConnectionInputBusPosInfo(connectionKey);
            final TileEntity inputTileEntity = world.getBlockEntity(busPosInfo);
            if (Objects.isNull(inputTileEntity)) continue;
            if (!(inputTileEntity instanceof TileWirelessInputBus)) continue;
            TileWirelessInputBus wirelessInputBus = (TileWirelessInputBus) inputTileEntity;
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
