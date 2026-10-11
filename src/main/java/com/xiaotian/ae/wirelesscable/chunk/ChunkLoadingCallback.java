package com.xiaotian.ae.wirelesscable.chunk;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.List;

public class ChunkLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(final List<ForgeChunkManager.Ticket> tickets, final World world) {
        NetworkChunkLoading.restoreTickets(tickets, world);
    }
}
