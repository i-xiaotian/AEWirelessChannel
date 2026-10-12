package com.xiaotian.ae.wirelesscable.chunk;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class NetworkChunkLoading {
    private static final TicketController TICKET_CONTROLLER = new TicketController(
            new ResourceLocation(AEWirelessChannel.MOD_ID, "wireless_bus"), NetworkChunkLoading::restoreTickets);
    private static final Map<ServerLevel, Map<BlockPos, TicketState>> TICKETS = new WeakHashMap<>();
    private static final Map<ServerLevel, Integer> SHARED_CHUNK_COUNTS = new WeakHashMap<>();
    private static final Map<ServerLevel, Set<BlockPos>> PENDING_OWNER_CHECKS = new WeakHashMap<>();

    private NetworkChunkLoading() {
    }

    public static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
    }

    public static void restoreTickets(ServerLevel level, TicketHelper helper) {
        Map<BlockPos, TicketState> worldTickets = TICKETS.computeIfAbsent(level, ignored -> new HashMap<>());
        for (Map.Entry<BlockPos, TicketSet> entry : new ArrayList<>(helper.getBlockTickets().entrySet())) {
            BlockPos owner = entry.getKey();
            if (!ModConfig.FORCE_CHUNK_LOADING.get()) {
                helper.removeAllTickets(owner);
                continue;
            }
            Set<Long> chunks = new HashSet<>();
            chunks.addAll(entry.getValue().nonTicking());
            chunks.addAll(entry.getValue().ticking());
            worldTickets.put(owner, new TicketState(chunks));
        }
        updateSharedCount(level, worldTickets);
    }

    public static void update(WirelessBusBlockEntity bus) {
        if (!(bus.getLevel() instanceof ServerLevel level)) return;
        if (!ModConfig.FORCE_CHUNK_LOADING.get()) {
            unregister(level, bus.getBlockPos());
            bus.setChunkLoadingInfo(false, -1, -1, -1, -1, -1);
            return;
        }

        BlockPos owner = bus.getBlockPos();
        Map<BlockPos, TicketState> worldTickets = TICKETS.computeIfAbsent(level, ignored -> new HashMap<>());
        TicketState state = worldTickets.computeIfAbsent(owner, ignored -> new TicketState());
        Set<Long> desired = getDesiredChunks(owner, ModConfig.CHUNK_LOADING_RADIUS.get());
        boolean changed = false;
        if (!state.chunks.equals(desired)) {
            for (long previous : new HashSet<>(state.chunks)) {
                if (!desired.contains(previous)) {
                    TICKET_CONTROLLER.forceChunk(level, owner,
                            ChunkPos.getX(previous), ChunkPos.getZ(previous), false, true);
                    TICKET_CONTROLLER.forceChunk(level, owner,
                            ChunkPos.getX(previous), ChunkPos.getZ(previous), false, false);
                    state.chunks.remove(previous);
                    changed = true;
                }
            }
            for (long chunk : desired) {
                if (state.chunks.contains(chunk)) continue;
                boolean forced = TICKET_CONTROLLER.forceChunk(level, owner,
                        ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true, true);
                if (forced) {
                    state.chunks.add(chunk);
                    changed = true;
                }
            }
        }
        state.expected = desired.size();
        state.held = state.chunks.size();
        if (changed) updateSharedCount(level, worldTickets);
        bus.setChunkLoadingInfo(true, ModConfig.CHUNK_LOADING_RADIUS.get(), state.expected,
                state.held, SHARED_CHUNK_COUNTS.getOrDefault(level, 0), -1);
    }

    public static void unregister(ServerLevel level, BlockPos owner) {
        Set<BlockPos> pending = PENDING_OWNER_CHECKS.get(level);
        if (pending != null) {
            pending.remove(owner);
            if (pending.isEmpty()) PENDING_OWNER_CHECKS.remove(level);
        }
        Map<BlockPos, TicketState> worldTickets = TICKETS.get(level);
        TicketState state = worldTickets == null ? null : worldTickets.remove(owner);
        Set<Long> chunks = state == null ? getDesiredChunks(owner, ModConfig.CHUNK_LOADING_RADIUS.get()) : state.chunks;
        for (long chunk : chunks) {
            TICKET_CONTROLLER.forceChunk(level, owner,
                    ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
            TICKET_CONTROLLER.forceChunk(level, owner,
                    ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, false);
        }
        if (worldTickets != null) {
            if (worldTickets.isEmpty()) {
                TICKETS.remove(level);
                SHARED_CHUNK_COUNTS.remove(level);
            } else {
                updateSharedCount(level, worldTickets);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TICKETS.remove(serverLevel);
            SHARED_CHUNK_COUNTS.remove(serverLevel);
            PENDING_OWNER_CHECKS.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Map<BlockPos, TicketState> worldTickets = TICKETS.get(level);
        if (worldTickets == null) return;
        int chunkX = event.getChunk().getPos().x;
        int chunkZ = event.getChunk().getPos().z;
        for (BlockPos owner : new ArrayList<>(worldTickets.keySet())) {
            if ((owner.getX() >> 4) != chunkX || (owner.getZ() >> 4) != chunkZ) continue;
            PENDING_OWNER_CHECKS.computeIfAbsent(level, ignored -> new HashSet<>()).add(owner);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        Set<BlockPos> pending = PENDING_OWNER_CHECKS.remove(level);
        if (pending == null) return;
        for (BlockPos owner : pending) {
            if (level.hasChunkAt(owner) && !(level.getBlockEntity(owner) instanceof WirelessBusBlockEntity)) {
                unregister(level, owner);
            }
        }
    }

    private static void updateSharedCount(ServerLevel level, Map<BlockPos, TicketState> worldTickets) {
        Set<Long> union = new LinkedHashSet<>();
        for (TicketState state : worldTickets.values()) union.addAll(state.chunks);
        SHARED_CHUNK_COUNTS.put(level, union.size());
    }

    private static Set<Long> getDesiredChunks(BlockPos owner, int radius) {
        int centerX = owner.getX() >> 4;
        int centerZ = owner.getZ() >> 4;
        Set<Long> chunks = new LinkedHashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) chunks.add(ChunkPos.asLong(centerX + dx, centerZ + dz));
        }
        return chunks;
    }

    private static final class TicketState {
        private final Set<Long> chunks = new HashSet<>();
        private int expected;
        private int held;

        private TicketState() {
        }

        private TicketState(Set<Long> chunks) {
            this.chunks.addAll(chunks);
            this.expected = chunks.size();
            this.held = chunks.size();
        }
    }
}
