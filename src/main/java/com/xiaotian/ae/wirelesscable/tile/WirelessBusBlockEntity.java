package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class WirelessBusBlockEntity extends AENetworkedBlockEntity implements IActionHost {
    private static final String OUTPUT_BUS_ID = "wirelessOutputBusId";
    private static final String CONNECTIONS_TAG = "wirelessConnections";
    private static final Map<ServerLevel, Map<UUID, WeakReference<WirelessBusBlockEntity>>> LOADED_OUTPUTS = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<UUID, WeakReference<WirelessBusBlockEntity>>> LOADED_INPUTS = new WeakHashMap<>();

    private UUID outputBusId = UUID.randomUUID();
    private UUID inputConnectionId = UUID.randomUUID();
    private UUID connectedOutputId;
    private BlockPos connectedOutputPos = BlockPos.ZERO;
    private boolean inputConnected;
    private final Map<UUID, ConnectionInfo> outputConnections = new HashMap<>();
    private IGridConnection gridConnection;
    private long lastChunkSync = Long.MIN_VALUE;
    private boolean chunkLoadingEnabled;
    private int chunkLoadingRadius = -1;
    private int chunkLoadingExpected = -1;
    private int chunkLoadingHeld = -1;
    private int chunkLoadingShared = -1;
    private int chunkLoadingLimit = -1;

    public WirelessBusBlockEntity(BlockPos pos, BlockState state) {
        super(getEntityType(state), pos, state);
    }

    private static BlockEntityType<?> getEntityType(BlockState state) {
        if (!(state.getBlock() instanceof WirelessBusBlock block)) {
            throw new IllegalArgumentException("Wireless bus block entity created for a non-wireless bus block");
        }
        if (block.isOutput()) {
            return block.isDense() ? ModContent.DENSE_WIRELESS_OUTPUT_BUS_ENTITY.get()
                    : ModContent.WIRELESS_OUTPUT_BUS_ENTITY.get();
        }
        return block.isDense() ? ModContent.DENSE_WIRELESS_INPUT_BUS_ENTITY.get()
                : ModContent.WIRELESS_INPUT_BUS_ENTITY.get();
    }

    @Override
    protected IManagedGridNode createMainNode() {
        if (getBlockState().getBlock() instanceof WirelessBusBlock block && block.isDense()) {
            return super.createMainNode()
                    .setFlags(GridFlags.DENSE_CAPACITY)
                    .setTagName("wirelessBus")
                    .setInWorldNode(true);
        }
        return super.createMainNode()
                .setTagName("wirelessBus")
                .setInWorldNode(true);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        if (!(getBlockState().getBlock() instanceof WirelessBusBlock block)) return ModContent.WIRELESS_KEY_CARD.get();
        if (block.isOutput()) {
            return block.isDense() ? ModContent.DENSE_WIRELESS_OUTPUT_BUS_ITEM.get() : ModContent.WIRELESS_OUTPUT_BUS_ITEM.get();
        }
        return block.isDense() ? ModContent.DENSE_WIRELESS_INPUT_BUS_ITEM.get() : ModContent.WIRELESS_INPUT_BUS_ITEM.get();
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return isDense() ? AECableType.DENSE_SMART : AECableType.SMART;
    }

    @Override
    public IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    @Override
    public void onReady() {
        super.onReady();
        updateExposedSide();
        if (level instanceof ServerLevel serverLevel) {
            registerLoaded(serverLevel);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) registerLoaded(serverLevel);
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) unregisterLoaded(serverLevel);
        gridConnection = null;
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) unregisterLoaded(serverLevel);
        if (gridConnection != null) {
            gridConnection.destroy();
            gridConnection = null;
        }
        super.setRemoved();
    }

    @Override
    public void loadTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        outputConnections.clear();
        if (isOutput()) {
            loadOutputData(tag);
        } else {
            loadInputData(tag);
        }
        loadChunkInfo(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (isOutput()) {
            tag.putUUID(OUTPUT_BUS_ID, outputBusId);
            ListTag links = new ListTag();
            for (ConnectionInfo info : outputConnections.values()) links.add(info.save());
            tag.put(CONNECTIONS_TAG, links);
        } else {
            tag.putUUID("wirelessInputConnectionId", inputConnectionId);
            tag.putBoolean("wirelessInputConnected", inputConnected);
            tag.putLong("wirelessOutputBusPos", connectedOutputPos.asLong());
            if (connectedOutputId != null) tag.putUUID("wirelessConnectedOutputId", connectedOutputId);
        }
        tag.putBoolean("wirelessChunkLoadingEnabled", chunkLoadingEnabled);
        tag.putInt("wirelessChunkLoadingRadius", chunkLoadingRadius);
        tag.putInt("wirelessChunkLoadingExpected", chunkLoadingExpected);
        tag.putInt("wirelessChunkLoadingHeld", chunkLoadingHeld);
        tag.putInt("wirelessChunkLoadingShared", chunkLoadingShared);
        tag.putInt("wirelessChunkLoadingLimit", chunkLoadingLimit);
    }

    private void loadOutputData(CompoundTag tag) {
        if (tag.hasUUID(OUTPUT_BUS_ID)) {
            setOutputBusId(tag.getUUID(OUTPUT_BUS_ID));
        } else if (tag.contains("wirelessOutputBusId", Tag.TAG_STRING)) {
            setOutputBusId(parseUuid(tag.getString("wirelessOutputBusId"), outputBusId));
        }

        ListTag links = tag.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < links.size(); i++) {
            ConnectionInfo info = ConnectionInfo.load(links.getCompound(i));
            outputConnections.put(info.inputId(), info);
        }

        // Read the 1.12 portable-block format so an old safe-dismantle item keeps its links.
        if (outputConnections.isEmpty() && tag.contains("connectionInfoTag", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = tag.getCompound("connectionInfoTag");
            for (String key : legacy.getAllKeys()) {
                CompoundTag entry = legacy.getCompound(key);
                UUID inputId = parseUuid(entry.getString("connectionKey"), UUID.randomUUID());
                BlockPos inputPos = new BlockPos(entry.getInt("inputBusX"), entry.getInt("inputBusY"), entry.getInt("inputBusZ"));
                outputConnections.put(inputId, new ConnectionInfo(inputId, inputPos));
            }
        }
    }

    private void loadInputData(CompoundTag tag) {
        UUID storedInputId = tag.hasUUID("wirelessInputConnectionId")
                ? tag.getUUID("wirelessInputConnectionId") : UUID.randomUUID();
        setInputConnectionId(storedInputId);
        inputConnected = tag.getBoolean("wirelessInputConnected");
        connectedOutputPos = BlockPos.of(tag.getLong("wirelessOutputBusPos"));
        connectedOutputId = tag.hasUUID("wirelessConnectedOutputId") ? tag.getUUID("wirelessConnectedOutputId") : null;

        // Read the 1.12 portable-block format.
        if (tag.contains("connectionKey", Tag.TAG_STRING)) {
            setInputConnectionId(parseUuid(tag.getString("connectionKey"), inputConnectionId));
            inputConnected = tag.getBoolean("connect");
            connectedOutputPos = new BlockPos(tag.getInt("outputBusX"), tag.getInt("outputBusY"), tag.getInt("outputBusZ"));
            connectedOutputId = null;
        }
    }

    private void loadChunkInfo(CompoundTag tag) {
        chunkLoadingEnabled = tag.getBoolean("wirelessChunkLoadingEnabled");
        chunkLoadingRadius = tag.contains("wirelessChunkLoadingRadius") ? tag.getInt("wirelessChunkLoadingRadius") : -1;
        chunkLoadingExpected = tag.contains("wirelessChunkLoadingExpected") ? tag.getInt("wirelessChunkLoadingExpected") : -1;
        chunkLoadingHeld = tag.contains("wirelessChunkLoadingHeld") ? tag.getInt("wirelessChunkLoadingHeld") : -1;
        chunkLoadingShared = tag.contains("wirelessChunkLoadingShared") ? tag.getInt("wirelessChunkLoadingShared") : -1;
        chunkLoadingLimit = tag.contains("wirelessChunkLoadingLimit") ? tag.getInt("wirelessChunkLoadingLimit") : -1;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WirelessBusBlockEntity bus) {
        if (!(level instanceof ServerLevel serverLevel) || bus.isRemoved()) return;
        if (serverLevel.getGameTime() % 10 == 0) {
            bus.updateActiveState(state);
            bus.updateExposedSide();
        }
        if (serverLevel.getGameTime() % 20 == 0) {
            NetworkChunkLoading.update(bus);
            bus.reconcileConnections();
        }
    }

    private void updateActiveState(BlockState state) {
        boolean active = getMainNode().isActive();
        if (state.getValue(BlockStateProperties.POWERED) == active) return;
        level.setBlock(worldPosition, state.setValue(BlockStateProperties.POWERED, active), 3);
    }

    private void updateExposedSide() {
        if (level == null || !level.isClientSide && !getMainNode().isReady()) return;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        getMainNode().setExposedOnSides(Set.of(facing, facing.getOpposite()));
    }

    private void reconcileConnections() {
        if (isOutput()) {
            reconcileOutputLinks();
        } else if (inputConnected) {
            reconcileInputLink();
        }
    }

    private void reconcileOutputLinks() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Iterator<Map.Entry<UUID, ConnectionInfo>> iterator = outputConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            ConnectionInfo info = iterator.next().getValue();
            WirelessBusBlockEntity input = findLoadedInput(serverLevel, info.inputId());
            if (input == null && serverLevel.hasChunkAt(info.inputPos())) {
                var candidate = serverLevel.getBlockEntity(info.inputPos());
                if (candidate instanceof WirelessBusBlockEntity bus && !bus.isOutput()) input = bus;
            }
            if (input == null) {
                if (serverLevel.hasChunkAt(info.inputPos())) {
                    BlockState inputState = serverLevel.getBlockState(info.inputPos());
                    if (!(inputState.getBlock() instanceof WirelessBusBlock inputBlock) || inputBlock.isOutput()) {
                        iterator.remove();
                        setChangedAndSync();
                    }
                }
                continue;
            }
            if (!input.inputConnected || !inputConnectionIdEquals(input, info.inputId())
                    || input.connectedOutputId != null && !outputBusId.equals(input.connectedOutputId)) {
                iterator.remove();
                setChanged();
                continue;
            }
            if (input.connectedOutputId == null) {
                input.connectedOutputId = outputBusId;
                input.setChangedAndSync();
            }
            if (!info.inputPos().equals(input.getBlockPos())) {
                info.setInputPos(input.getBlockPos());
                setChanged();
            }
            if (!getBlockPos().equals(input.connectedOutputPos)) {
                input.connectedOutputPos = getBlockPos();
                input.setChangedAndSync();
            }
            input.tryCreateGridConnection(this);
        }
    }

    private void reconcileInputLink() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        WirelessBusBlockEntity output = connectedOutputId == null ? null : findLoadedOutput(serverLevel, connectedOutputId);
        if (output == null && serverLevel.hasChunkAt(connectedOutputPos)) {
            var candidate = serverLevel.getBlockEntity(connectedOutputPos);
            if (candidate instanceof WirelessBusBlockEntity bus && bus.isOutput()) output = bus;
        }
        if (output == null || !output.isOutput()) {
            if (serverLevel.hasChunkAt(connectedOutputPos) && !serverLevel.isEmptyBlock(connectedOutputPos)) {
                clearInputConnection();
            }
            return;
        }
        if (connectedOutputId != null && !connectedOutputId.equals(output.outputBusId)) {
            clearInputConnection();
            return;
        }
        if (connectedOutputId == null) connectedOutputId = output.outputBusId;
        if (!connectedOutputPos.equals(output.getBlockPos())) {
            connectedOutputPos = output.getBlockPos();
            setChangedAndSync();
        }
        output.outputConnections.put(inputConnectionId, new ConnectionInfo(inputConnectionId, getBlockPos()));
        output.setChangedAndSync();
        tryCreateGridConnection(output);
    }

    private void tryCreateGridConnection(WirelessBusBlockEntity output) {
        IGridNode inputNode = getMainNode().getNode();
        IGridNode outputNode = output.getMainNode().getNode();
        if (gridConnection != null && inputNode != null && inputNode.getConnections().contains(gridConnection)) return;
        gridConnection = null;
        if (!inputConnected || !output.isOutput() || inputNode == null || outputNode == null
                || inputNode.getGrid() == null || outputNode.getGrid() == null) return;
        try {
            gridConnection = GridHelper.createConnection(outputNode, inputNode);
        } catch (RuntimeException exception) {
            AEWirelessChannel.LOGGER.debug("Could not restore wireless connection between {} and {}",
                    output.getBlockPos(), getBlockPos(), exception);
        }
    }

    public void connectTo(WirelessBusBlockEntity output) {
        if (isOutput() || !output.isOutput() || level == null || output.level != level) return;
        clearInputConnection();
        inputConnected = true;
        connectedOutputId = output.outputBusId;
        connectedOutputPos = output.getBlockPos();
        output.outputConnections.put(inputConnectionId, new ConnectionInfo(inputConnectionId, getBlockPos()));
        setChangedAndSync();
        output.setChangedAndSync();
        tryCreateGridConnection(output);
    }

    public boolean clearInputConnection() {
        boolean hadConnection = inputConnected || connectedOutputId != null;
        if (gridConnection != null) {
            gridConnection.destroy();
            gridConnection = null;
        }
        WirelessBusBlockEntity output = getConnectedOutput();
        if (output != null && output.outputConnections.remove(inputConnectionId) != null) output.setChangedAndSync();
        inputConnected = false;
        connectedOutputId = null;
        connectedOutputPos = BlockPos.ZERO;
        setInputConnectionId(UUID.randomUUID());
        setChangedAndSync();
        return hadConnection;
    }

    public int clearOutputConnections() {
        if (!isOutput()) return 0;
        int cleared = 0;
        for (ConnectionInfo info : new ArrayList<>(outputConnections.values())) {
            WirelessBusBlockEntity input = level instanceof ServerLevel serverLevel
                    ? findLoadedInput(serverLevel, info.inputId()) : null;
            if (input == null && level != null && level.hasChunkAt(info.inputPos())) {
                var candidate = level.getBlockEntity(info.inputPos());
                if (candidate instanceof WirelessBusBlockEntity bus && !bus.isOutput()) input = bus;
            }
            if (input != null && input.inputConnectionId.equals(info.inputId())
                    && outputBusId.equals(input.connectedOutputId)) {
                if (input.clearInputConnection()) cleared++;
            }
        }
        if (!outputConnections.isEmpty()) {
            outputConnections.clear();
            setChangedAndSync();
        }
        return cleared;
    }

    private WirelessBusBlockEntity getConnectedOutput() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        WirelessBusBlockEntity output = connectedOutputId == null ? null : findLoadedOutput(serverLevel, connectedOutputId);
        if (output == null && serverLevel.hasChunkAt(connectedOutputPos)) {
            var candidate = serverLevel.getBlockEntity(connectedOutputPos);
            if (candidate instanceof WirelessBusBlockEntity bus && bus.isOutput()) output = bus;
        }
        return output;
    }

    private void registerLoaded(ServerLevel serverLevel) {
        if (isOutput()) {
            LOADED_OUTPUTS.computeIfAbsent(serverLevel, ignored -> new HashMap<>())
                    .put(outputBusId, new WeakReference<>(this));
        } else {
            LOADED_INPUTS.computeIfAbsent(serverLevel, ignored -> new HashMap<>())
                    .put(inputConnectionId, new WeakReference<>(this));
        }
    }

    private void setOutputBusId(UUID id) {
        if (outputBusId.equals(id)) return;
        if (level instanceof ServerLevel serverLevel) unregisterLoaded(serverLevel);
        outputBusId = id;
        if (level instanceof ServerLevel serverLevel) registerLoaded(serverLevel);
    }

    private void setInputConnectionId(UUID id) {
        if (inputConnectionId.equals(id)) return;
        if (level instanceof ServerLevel serverLevel) unregisterLoaded(serverLevel);
        inputConnectionId = id;
        if (level instanceof ServerLevel serverLevel) registerLoaded(serverLevel);
    }

    private void unregisterLoaded(ServerLevel serverLevel) {
        removeLoaded(LOADED_OUTPUTS, serverLevel, outputBusId, this);
        removeLoaded(LOADED_INPUTS, serverLevel, inputConnectionId, this);
    }

    private static void removeLoaded(Map<ServerLevel, Map<UUID, WeakReference<WirelessBusBlockEntity>>> map,
                                     ServerLevel level, UUID id, WirelessBusBlockEntity entity) {
        Map<UUID, WeakReference<WirelessBusBlockEntity>> entries = map.get(level);
        if (entries == null) return;
        WeakReference<WirelessBusBlockEntity> reference = entries.get(id);
        if (reference != null && (reference.get() == null || reference.get() == entity)) entries.remove(id);
        if (entries.isEmpty()) map.remove(level);
    }

    @Nullable
    public static WirelessBusBlockEntity findLoadedOutput(ServerLevel level, UUID id) {
        return findLoaded(LOADED_OUTPUTS, level, id);
    }

    @Nullable
    public static WirelessBusBlockEntity findLoadedInput(ServerLevel level, UUID id) {
        return findLoaded(LOADED_INPUTS, level, id);
    }

    @Nullable
    private static WirelessBusBlockEntity findLoaded(
            Map<ServerLevel, Map<UUID, WeakReference<WirelessBusBlockEntity>>> map, ServerLevel level, UUID id) {
        Map<UUID, WeakReference<WirelessBusBlockEntity>> entries = map.get(level);
        if (entries == null) return null;
        WeakReference<WirelessBusBlockEntity> reference = entries.get(id);
        WirelessBusBlockEntity entity = reference == null ? null : reference.get();
        return entity == null || entity.isRemoved() ? null : entity;
    }

    private void setChangedAndSync() {
        setChanged();
        markForUpdate();
    }

    private boolean isDense() {
        return getBlockState().getBlock() instanceof WirelessBusBlock block && block.isDense();
    }

    public boolean isOutput() {
        return getBlockState().getBlock() instanceof WirelessBusBlock block && block.isOutput();
    }

    public UUID getOutputBusId() {
        return outputBusId;
    }

    public UUID getInputConnectionId() {
        return inputConnectionId;
    }

    public BlockPos getConnectedOutputPos() {
        return connectedOutputPos;
    }

    public UUID getConnectedOutputId() {
        return connectedOutputId;
    }

    public boolean isInputConnected() {
        return inputConnected;
    }

    public Collection<ConnectionInfo> getOutputConnections() {
        return ListCopy.copy(outputConnections.values());
    }

    public CompoundTag savePortableConnectionData() {
        CompoundTag tag = new CompoundTag();
        if (isOutput()) {
            tag.putUUID(OUTPUT_BUS_ID, outputBusId);
            ListTag links = new ListTag();
            for (ConnectionInfo info : outputConnections.values()) links.add(info.save());
            tag.put(CONNECTIONS_TAG, links);
        } else {
            tag.putUUID("wirelessInputConnectionId", inputConnectionId);
            tag.putBoolean("wirelessInputConnected", inputConnected);
            tag.putLong("wirelessOutputBusPos", connectedOutputPos.asLong());
            if (connectedOutputId != null) tag.putUUID("wirelessConnectedOutputId", connectedOutputId);
        }
        return tag;
    }

    public void loadPortableConnectionData(CompoundTag tag) {
        if (isOutput()) {
            if (tag.hasUUID(OUTPUT_BUS_ID)) setOutputBusId(tag.getUUID(OUTPUT_BUS_ID));
            outputConnections.clear();
            ListTag links = tag.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < links.size(); i++) {
                ConnectionInfo info = ConnectionInfo.load(links.getCompound(i));
                outputConnections.put(info.inputId(), info);
            }
        } else {
            if (tag.hasUUID("wirelessInputConnectionId")) {
                setInputConnectionId(tag.getUUID("wirelessInputConnectionId"));
            }
            inputConnected = tag.getBoolean("wirelessInputConnected");
            connectedOutputPos = BlockPos.of(tag.getLong("wirelessOutputBusPos"));
            connectedOutputId = tag.hasUUID("wirelessConnectedOutputId")
                    ? tag.getUUID("wirelessConnectedOutputId") : null;
        }
        if (level instanceof ServerLevel serverLevel) registerLoaded(serverLevel);
        setChangedAndSync();
    }

    public IGridNode getGridNode() {
        return getMainNode().getNode();
    }

    public boolean isNodeActive() {
        return getMainNode().isActive();
    }

    public boolean mayConfigure(Player player) {
        IGridNode node = getMainNode().getNode();
        if (node == null || node.getOwningPlayerProfileId() == null) return true;
        return node.getOwningPlayerProfileId().equals(player.getUUID()) || player.hasPermissions(2);
    }

    public void setOwner(Player player) {
        getMainNode().setOwningPlayer(player);
    }

    public void setChunkLoadingInfo(boolean enabled, int radius, int expected, int held, int shared, int limit) {
        if (chunkLoadingEnabled == enabled && chunkLoadingRadius == radius && chunkLoadingExpected == expected
                && chunkLoadingHeld == held && chunkLoadingShared == shared && chunkLoadingLimit == limit) return;
        chunkLoadingEnabled = enabled;
        chunkLoadingRadius = radius;
        chunkLoadingExpected = expected;
        chunkLoadingHeld = held;
        chunkLoadingShared = shared;
        chunkLoadingLimit = limit;
        setChangedAndSync();
    }

    public boolean isChunkLoadingEnabled() { return chunkLoadingEnabled; }
    public int getChunkLoadingRadius() { return chunkLoadingRadius; }
    public int getChunkLoadingExpected() { return chunkLoadingExpected; }
    public int getChunkLoadingHeld() { return chunkLoadingHeld; }
    public int getChunkLoadingShared() { return chunkLoadingShared; }
    public int getChunkLoadingLimit() { return chunkLoadingLimit; }

    private static UUID parseUuid(String value, UUID fallback) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static boolean inputConnectionIdEquals(WirelessBusBlockEntity input, UUID id) {
        return input.inputConnectionId.equals(id);
    }

    private static final class ListCopy {
        private static Collection<ConnectionInfo> copy(Collection<ConnectionInfo> values) {
            return java.util.Collections.unmodifiableList(new ArrayList<>(values));
        }
    }
}
