package com.xiaotian.ae.wirelesscable.chunk;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.List;

public class ChunkLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            final ImmutableSet<ChunkPos> chunkList = ticket.getChunkList();
            for (ChunkPos chunkPos : chunkList) {
                ForgeChunkManager.forceChunk(ticket, chunkPos);
            }
        }
    }
}
