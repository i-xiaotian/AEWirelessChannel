package com.xiaotian.ae.wirelesscable.integration.top;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.config.AEWirelessCableConfig;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.util.Ae2ChannelLimits;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TopInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return AEWirelessChannel.MOD_ID + ":top_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode probeMode, final IProbeInfo iProbeInfo,
                             final EntityPlayer entityPlayer, final World world,
                             final IBlockState blockState, final IProbeHitData hitData) {
        if (!blockState.getBlock().hasTileEntity(blockState)) return;

        final TileEntity tileEntity = world.getTileEntity(hitData.getPos());
        if (!(tileEntity instanceof TileWirelessBus)) return;

        final TileWirelessBus wirelessBus = (TileWirelessBus) tileEntity;
        final AENetworkProxy proxy = wirelessBus.getProxy();
        final boolean active = proxy.isActive();
        final String onlineInfo = active
                ? "{*top.aewirelesschannel.device_online*}"
                : "{*top.aewirelesschannel.device_offline*}";
        iProbeInfo.text((active ? TextFormatting.WHITE : TextFormatting.GRAY) + onlineInfo);

        showChannelInfo(iProbeInfo, wirelessBus);
        showChunkLoadingInfo(iProbeInfo, wirelessBus);
        if (AEWirelessCableConfig.topConnectionInfoShowType && !entityPlayer.isSneaking()) return;

        if (wirelessBus instanceof TileWirelessInputBus) {
            showInputBusInfo(iProbeInfo, (TileWirelessInputBus) wirelessBus);
        } else if (wirelessBus instanceof TileWirelessOutputBus) {
            showOutputBusInfo(iProbeInfo, (TileWirelessOutputBus) wirelessBus);
        }
    }

    private static void showChannelInfo(final IProbeInfo probeInfo, final TileWirelessBus wirelessBus) {
        final IGridNode node = wirelessBus.getProxy().getNode();
        if (!(node instanceof GridNode)) {
            probeInfo.text(I18n.format("top.aewirelesschannel.channel_unavailable"));
            return;
        }

        final GridNode gridNode = (GridNode) node;
        final int channelLimit = Ae2ChannelLimits.getChannelLimit(node.hasFlag(GridFlags.DENSE_CAPACITY));
        if (channelLimit < 0) {
            probeInfo.text(I18n.format("top.aewirelesschannel.channel_usage_unknown", gridNode.usedChannels()));
            return;
        }
        probeInfo.text(I18n.format("top.aewirelesschannel.channel_usage", gridNode.usedChannels(), channelLimit));
    }

    private static void showChunkLoadingInfo(final IProbeInfo probeInfo, final TileWirelessBus wirelessBus) {
        if (!wirelessBus.isChunkLoadingEnabled()) return;

        final int radius = wirelessBus.getChunkLoadingRadius();
        final int expected = wirelessBus.getChunkLoadingExpected();
        final int held = wirelessBus.getChunkLoadingHeld();
        final int shared = wirelessBus.getChunkLoadingShared();
        final int limit = wirelessBus.getChunkLoadingLimit();

        final IProbeInfo vertical = getVertical(probeInfo);
        vertical.text(I18n.format("top.aewirelesschannel.chunk_loading_header"));
        if (radius >= 0) {
            vertical.text(I18n.format("top.aewirelesschannel.chunk_loading_area",
                    radius * 2 + 1, radius * 2 + 1, radius));
        }
        if (held < 0 || expected < 0) {
            vertical.text(I18n.format("top.aewirelesschannel.chunk_loading_waiting"));
        } else {
            vertical.text(I18n.format("top.aewirelesschannel.chunk_loading_held", held, expected));
        }
        if (shared >= 0 && limit >= 0) {
            vertical.text(I18n.format("top.aewirelesschannel.chunk_loading_shared", shared, limit));
        }
    }

    private static void showInputBusInfo(final IProbeInfo probeInfo, final TileWirelessInputBus inputBus) {
        final ConnectionInfo connection = inputBus.getCurrentConnection();
        final IProbeInfo vertical = getVertical(probeInfo);
        vertical.text(I18n.format("top.aewirelesschannel.connection_header"));
        if (!connection.isConnect()) {
            vertical.text(I18n.format("top.aewirelesschannel.input_unconnected"));
            return;
        }

        vertical.text(I18n.format("top.aewirelesschannel.input_source",
                connection.getOutputBusX(), connection.getOutputBusY(), connection.getOutputBusZ()));
    }

    private static void showOutputBusInfo(final IProbeInfo probeInfo, final TileWirelessOutputBus outputBus) {
        final List<ConnectionInfo> inputs = new ArrayList<>();
        for (Map.Entry<String, ConnectionInfo> entry : outputBus.getSubGridConnectionMap().entrySet()) {
            final ConnectionInfo info = entry.getValue();
            if (info != null && info.isConnect()) inputs.add(info);
        }
        inputs.sort(Comparator.comparingInt(ConnectionInfo::getInputBusX)
                .thenComparingInt(ConnectionInfo::getInputBusY)
                .thenComparingInt(ConnectionInfo::getInputBusZ));

        final IProbeInfo vertical = getVertical(probeInfo);
        vertical.text(I18n.format("top.aewirelesschannel.connection_header"));
        if (inputs.isEmpty()) {
            vertical.text(I18n.format("top.aewirelesschannel.output_no_inputs"));
            return;
        }

        final int showCount = Math.max(0, AEWirelessCableConfig.topConnectionInfoShowCount);
        final int visibleCount = Math.min(showCount, inputs.size());
        for (int index = 0; index < visibleCount; index++) {
            final ConnectionInfo input = inputs.get(index);
            vertical.text(I18n.format("top.aewirelesschannel.output_input",
                    index + 1, input.getInputBusX(), input.getInputBusY(), input.getInputBusZ()));
        }
        final int remaining = inputs.size() - visibleCount;
        if (remaining > 0) {
            vertical.text(I18n.format("top.aewirelesschannel.connection_more", remaining));
        }
    }

    private static IProbeInfo getVertical(final IProbeInfo probeInfo) {
        return probeInfo.vertical(probeInfo.defaultLayoutStyle()
                .spacing(0)
                .borderColor(0x80FFC080));
    }
}
