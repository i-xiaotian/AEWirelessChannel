package com.xiaotian.ae.wirelesscable.chunk;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.config.AEWirelessCableConfig;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Keeps one Forge ticket for all wireless buses in a world. The ticket is
 * deliberately independent of an AE grid, because grids can be rebuilt while
 * chunks are loading and are not a stable ownership boundary for this feature.
 */
public final class NetworkChunkLoading {

    private static final String MEMBERS_TAG = "wirelessBusMembers";
    private static final long TICKET_RETRY_INTERVAL = 200L;
    private static final long RESTORE_GRACE_PERIOD = 100L;
    private static final Map<World, TicketGroup> WORLD_GROUPS = new WeakHashMap<>();

    private NetworkChunkLoading() {
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload event) {
        WORLD_GROUPS.remove(event.getWorld());
    }

    public static void restoreTickets(final List<ForgeChunkManager.Ticket> tickets, final World world) {
        if (tickets.isEmpty()) return;

        final TicketGroup group = getOrCreateGroup(world);
        if (!AEWirelessCableConfig.GENERAL_CONFIG.forceChunk) {
            for (ForgeChunkManager.Ticket ticket : tickets) release(ticket);
            if (group.ticket != null && !tickets.contains(group.ticket)) release(group.ticket);
            WORLD_GROUPS.remove(world);
            return;
        }

        final ForgeChunkManager.Ticket canonical = group.ticket != null ? group.ticket : tickets.get(0);
        final Set<ChunkPos> restoredChunks = new LinkedHashSet<>();
        if (group.ticket != null) restoredChunks.addAll(group.ticket.getChunkList());

        for (ForgeChunkManager.Ticket ticket : tickets) {
            mergeMembers(group.members, ticket.getModData().getTagList(MEMBERS_TAG, 10));
            restoredChunks.addAll(ticket.getChunkList());
        }

        group.ticket = canonical;
        final int maxChunks = Math.max(0, canonical.getMaxChunkListDepth());
        final Set<ChunkPos> chunksToKeep = new LinkedHashSet<>();
        for (ChunkPos chunkPos : getDesiredChunks(group.members)) {
            if (chunksToKeep.size() >= maxChunks) break;
            chunksToKeep.add(chunkPos);
        }
        for (ChunkPos chunkPos : restoredChunks) {
            if (chunksToKeep.size() >= maxChunks) break;
            chunksToKeep.add(chunkPos);
        }

        for (ChunkPos chunkPos : chunksToKeep) {
            if (!canonical.getChunkList().contains(chunkPos)) {
                ForgeChunkManager.forceChunk(canonical, chunkPos);
            }
        }

        for (ForgeChunkManager.Ticket ticket : tickets) {
            if (ticket != canonical) release(ticket);
        }

        group.retainRestoredChunksUntil = world.getTotalWorldTime() + RESTORE_GRACE_PERIOD;
        group.appliedRadius = AEWirelessCableConfig.GENERAL_CONFIG.loadRadius;
        writeMembers(group);
        syncGroupInfo(group, world);
        // Do not query the world here. Forge restores forced chunks before this callback.
    }

    public static void register(final World world, final BlockPos pos) {
        if (world == null || world.isRemote || !AEWirelessCableConfig.GENERAL_CONFIG.forceChunk) return;

        final TicketGroup group = getOrCreateGroup(world);
        final boolean added = group.members.add(pos);
        final boolean hadTicket = group.ticket != null;
        if (group.ticket == null) requestTicket(world, group);
        if (group.ticket != null && (added || !hadTicket
                || group.appliedRadius != AEWirelessCableConfig.GENERAL_CONFIG.loadRadius
                || group.maintenanceDue(world.getTotalWorldTime()))) {
            updateTicket(group, world);
        }
    }

    public static void unregister(final World world, final BlockPos pos) {
        if (world == null || world.isRemote) return;

        final TicketGroup group = WORLD_GROUPS.get(world);
        if (group == null || !group.members.remove(pos)) return;

        if (group.members.isEmpty()) {
            WORLD_GROUPS.remove(world);
            if (group.ticket != null) release(group.ticket);
            group.ticket = null;
        } else if (group.ticket != null) {
            updateTicket(group, world);
        }
    }

    public static int getForcedChunkCount(final World world) {
        final TicketGroup group = WORLD_GROUPS.get(world);
        return group == null || group.ticket == null ? -1 : group.ticket.getChunkList().size();
    }

    public static int getChunkLimit(final World world) {
        final TicketGroup group = WORLD_GROUPS.get(world);
        return group == null || group.ticket == null ? -1 : group.ticket.getMaxChunkListDepth();
    }

    public static int getForcedChunksAround(final World world, final BlockPos pos) {
        final TicketGroup group = WORLD_GROUPS.get(world);
        if (group == null || group.ticket == null) return -1;

        final Set<ChunkPos> forcedChunks = group.ticket.getChunkList();
        final int centerX = pos.getX() >> 4;
        final int centerZ = pos.getZ() >> 4;
        final int radius = AEWirelessCableConfig.GENERAL_CONFIG.loadRadius;
        int forced = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (forcedChunks.contains(new ChunkPos(centerX + dx, centerZ + dz))) forced++;
            }
        }
        return forced;
    }

    public static void syncMember(final World world, final BlockPos pos) {
        if (world == null || world.isRemote || !world.isBlockLoaded(pos)) return;
        final TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileWirelessBus)) return;

        if (!AEWirelessCableConfig.GENERAL_CONFIG.forceChunk) {
            ((TileWirelessBus) tile).setChunkLoadingInfo(false, -1, -1, -1, -1, -1);
            return;
        }

        final TicketGroup group = WORLD_GROUPS.get(world);
        final int radius = AEWirelessCableConfig.GENERAL_CONFIG.loadRadius;
        final int area = (radius * 2 + 1) * (radius * 2 + 1);
        if (group == null || group.ticket == null) {
            ((TileWirelessBus) tile).setChunkLoadingInfo(true, radius, area, -1, -1, -1);
            return;
        }

        ((TileWirelessBus) tile).setChunkLoadingInfo(true, radius, area,
                getForcedChunksAround(world, pos), getForcedChunkCount(world), getChunkLimit(world));
    }

    private static TicketGroup getOrCreateGroup(final World world) {
        TicketGroup group = WORLD_GROUPS.get(world);
        if (group == null) {
            group = new TicketGroup();
            WORLD_GROUPS.put(world, group);
        }
        return group;
    }

    private static void requestTicket(final World world, final TicketGroup group) {
        final long worldTime = world.getTotalWorldTime();
        if (group.lastTicketAttempt != Long.MIN_VALUE
                && worldTime - group.lastTicketAttempt < TICKET_RETRY_INTERVAL) return;

        group.lastTicketAttempt = world.getTotalWorldTime();
        group.ticket = ForgeChunkManager.requestTicket(
                AEWirelessChannel.instance, world, ForgeChunkManager.Type.NORMAL);
        if (group.ticket == null) {
            AEWirelessChannel.log.warn("No Forge chunk-loading ticket available in dimension {}",
                    world.provider.getDimension());
        }
    }

    private static void updateTicket(final TicketGroup group, final World world) {
        if (group.ticket == null) return;

        final Set<ChunkPos> desired = getDesiredChunks(group.members);
        final int maxChunks = Math.max(0, group.ticket.getMaxChunkListDepth());
        final Set<ChunkPos> limited = new LinkedHashSet<>();
        for (ChunkPos chunkPos : desired) {
            if (limited.size() >= maxChunks) break;
            limited.add(chunkPos);
        }

        if (world.getTotalWorldTime() < group.retainRestoredChunksUntil) {
            for (ChunkPos chunkPos : group.ticket.getChunkList()) {
                if (limited.size() >= maxChunks) break;
                limited.add(chunkPos);
            }
        } else {
            group.retainRestoredChunksUntil = Long.MIN_VALUE;
        }

        for (ChunkPos chunkPos : group.ticket.getChunkList()) {
            if (!limited.contains(chunkPos)) ForgeChunkManager.unforceChunk(group.ticket, chunkPos);
        }
        for (ChunkPos chunkPos : limited) {
            if (!group.ticket.getChunkList().contains(chunkPos)) {
                ForgeChunkManager.forceChunk(group.ticket, chunkPos);
            }
        }
        writeMembers(group);
        syncGroupInfo(group, world);
        group.appliedRadius = AEWirelessCableConfig.GENERAL_CONFIG.loadRadius;
    }

    private static void syncGroupInfo(final TicketGroup group, final World world) {
        for (BlockPos pos : group.members) syncMember(world, pos);
    }

    private static Set<ChunkPos> getDesiredChunks(final Set<BlockPos> members) {
        if (members.isEmpty()) return Collections.emptySet();

        final LinkedHashSet<ChunkPos> chunks = new LinkedHashSet<>();
        final int radius = AEWirelessCableConfig.GENERAL_CONFIG.loadRadius;
        for (BlockPos pos : members) {
            final int centerX = pos.getX() >> 4;
            final int centerZ = pos.getZ() >> 4;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    chunks.add(new ChunkPos(centerX + dx, centerZ + dz));
                }
            }
        }
        return chunks;
    }

    private static void writeMembers(final TicketGroup group) {
        if (group.ticket == null) return;

        final NBTTagList members = new NBTTagList();
        for (BlockPos pos : group.members) {
            final NBTTagCompound member = new NBTTagCompound();
            member.setInteger("x", pos.getX());
            member.setInteger("y", pos.getY());
            member.setInteger("z", pos.getZ());
            members.appendTag(member);
        }
        group.ticket.getModData().setTag(MEMBERS_TAG, members);
    }

    private static void mergeMembers(final Set<BlockPos> members, final NBTTagList savedMembers) {
        for (int i = 0; i < savedMembers.tagCount(); i++) {
            final NBTTagCompound member = savedMembers.getCompoundTagAt(i);
            members.add(new BlockPos(member.getInteger("x"), member.getInteger("y"), member.getInteger("z")));
        }
    }

    private static void release(final ForgeChunkManager.Ticket ticket) {
        for (ChunkPos chunkPos : ticket.getChunkList()) {
            ForgeChunkManager.unforceChunk(ticket, chunkPos);
        }
        ForgeChunkManager.releaseTicket(ticket);
    }

    private static final class TicketGroup {
        private ForgeChunkManager.Ticket ticket;
        private final Set<BlockPos> members = new LinkedHashSet<>();
        private long lastTicketAttempt = Long.MIN_VALUE;
        private long retainRestoredChunksUntil = Long.MIN_VALUE;
        private int appliedRadius = Integer.MIN_VALUE;

        private boolean maintenanceDue(final long worldTime) {
            return retainRestoredChunksUntil != Long.MIN_VALUE && worldTime >= retainRestoredChunksUntil;
        }
    }
}
