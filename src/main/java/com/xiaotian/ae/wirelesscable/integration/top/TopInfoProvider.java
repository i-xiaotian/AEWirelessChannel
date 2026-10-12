package com.xiaotian.ae.wirelesscable.integration.top;

import appeng.api.networking.IGridNode;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import com.xiaotian.ae.wirelesscable.util.ChannelLimitUtils;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TopInfoProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(AEWirelessChannel.MOD_ID, "top_info_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level,
                             BlockState state, IProbeHitData hitData) {
        if (!(level.getBlockEntity(hitData.getPos()) instanceof WirelessBusBlockEntity bus)) return;
        boolean active = bus.isNodeActive();
        info.text(Component.translatable(active ? "top.aewirelesschannel.device_online"
                : "top.aewirelesschannel.device_offline"));

        IGridNode node = bus.getGridNode();
        if (node == null) {
            info.text(Component.translatable("top.aewirelesschannel.channel_unavailable"));
        } else {
            String limit = ChannelLimitUtils.getLimit(bus.getBlockState().getBlock() instanceof com.xiaotian.ae.wirelesscable.block.WirelessBusBlock block
                    && block.isDense(), node);
            info.text(Component.translatable("top.aewirelesschannel.channel_usage",
                    node.getUsedChannels(), limit));
        }

        showChunkLoading(info, bus);
        if (ModConfig.TOP_CONNECTION_INFO_SHOW_TYPE.get() && !player.isShiftKeyDown()) return;
        if (bus.isOutput()) showOutputConnections(info, bus);
        else showInputConnection(info, bus);
    }

    private static void showChunkLoading(IProbeInfo info, WirelessBusBlockEntity bus) {
        if (!bus.isChunkLoadingEnabled()) return;
        IProbeInfo section = info.vertical(info.defaultLayoutStyle().spacing(0).borderColor(0x80FFC080));
        int radius = bus.getChunkLoadingRadius();
        section.text(Component.translatable("top.aewirelesschannel.chunk_loading_header"));
        if (radius >= 0) section.text(Component.translatable("top.aewirelesschannel.chunk_loading_area",
                radius * 2 + 1, radius * 2 + 1, radius));
        if (bus.getChunkLoadingHeld() < 0 || bus.getChunkLoadingExpected() < 0) {
            section.text(Component.translatable("top.aewirelesschannel.chunk_loading_waiting"));
        } else {
            section.text(Component.translatable("top.aewirelesschannel.chunk_loading_held",
                    bus.getChunkLoadingHeld(), bus.getChunkLoadingExpected()));
        }
        if (bus.getChunkLoadingShared() >= 0) {
            section.text(Component.translatable("top.aewirelesschannel.chunk_loading_shared",
                    bus.getChunkLoadingShared()));
        }
    }

    private static void showInputConnection(IProbeInfo info, WirelessBusBlockEntity input) {
        IProbeInfo section = info.vertical(info.defaultLayoutStyle().spacing(0).borderColor(0x80FFC080));
        section.text(Component.translatable("top.aewirelesschannel.connection_header"));
        if (!input.isInputConnected()) {
            section.text(Component.translatable("top.aewirelesschannel.input_unconnected"));
            return;
        }
        BlockPos output = input.getConnectedOutputPos();
        section.text(Component.translatable("top.aewirelesschannel.input_source",
                output.getX(), output.getY(), output.getZ()));
    }

    private static void showOutputConnections(IProbeInfo info, WirelessBusBlockEntity output) {
        List<ConnectionInfo> links = new ArrayList<>(output.getOutputConnections());
        links.sort(Comparator.comparingInt((ConnectionInfo link) -> link.inputPos().getX())
                .thenComparingInt(link -> link.inputPos().getY())
                .thenComparingInt(link -> link.inputPos().getZ()));
        IProbeInfo section = info.vertical(info.defaultLayoutStyle().spacing(0).borderColor(0x80FFC080));
        section.text(Component.translatable("top.aewirelesschannel.connection_header"));
        if (links.isEmpty()) {
            section.text(Component.translatable("top.aewirelesschannel.output_no_inputs"));
            return;
        }
        int visible = Math.min(Math.max(0, ModConfig.TOP_CONNECTION_INFO_SHOW_COUNT.get()), links.size());
        for (int i = 0; i < visible; i++) {
            BlockPos pos = links.get(i).inputPos();
            section.text(Component.translatable("top.aewirelesschannel.output_input",
                    i + 1, pos.getX(), pos.getY(), pos.getZ()));
        }
        if (links.size() > visible) {
            section.text(Component.translatable("top.aewirelesschannel.connection_more", links.size() - visible));
        }
    }
}
