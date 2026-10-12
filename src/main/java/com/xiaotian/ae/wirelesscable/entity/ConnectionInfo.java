package com.xiaotian.ae.wirelesscable.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class ConnectionInfo {
    private final UUID inputId;
    private BlockPos inputPos;

    public ConnectionInfo(UUID inputId, BlockPos inputPos) {
        this.inputId = inputId;
        this.inputPos = inputPos;
    }

    public UUID inputId() {
        return inputId;
    }

    public BlockPos inputPos() {
        return inputPos;
    }

    public void setInputPos(BlockPos inputPos) {
        this.inputPos = inputPos;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("inputId", inputId);
        tag.putLong("inputPos", inputPos.asLong());
        return tag;
    }

    public static ConnectionInfo load(CompoundTag tag) {
        UUID id = tag.hasUUID("inputId") ? tag.getUUID("inputId") : UUID.randomUUID();
        BlockPos pos = BlockPos.of(tag.getLong("inputPos"));
        return new ConnectionInfo(id, pos);
    }
}
