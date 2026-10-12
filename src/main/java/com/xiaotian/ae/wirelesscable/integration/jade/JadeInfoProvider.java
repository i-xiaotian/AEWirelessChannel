package com.xiaotian.ae.wirelesscable.integration.jade;

import appeng.api.networking.IGridNode;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import com.xiaotian.ae.wirelesscable.util.ChannelLimitUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class JadeInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final JadeInfoProvider INSTANCE = new JadeInfoProvider();
    private static final ResourceLocation ID = new ResourceLocation(AEWirelessChannel.MOD_ID, "jade_info_provider");

    private JadeInfoProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof WirelessBusBlockEntity bus)) return;

        data.putBoolean("active", bus.isNodeActive());
        IGridNode node = bus.getGridNode();
        data.putBoolean("channelAvailable", node != null);
        if (node != null) {
            boolean dense = bus.getBlockState().getBlock() instanceof WirelessBusBlock block && block.isDense();
            data.putInt("channels", node.getUsedChannels());
            data.putString("channelLimit", ChannelLimitUtils.getLimit(dense, node));
        }
        data.putBoolean("chunkLoading", bus.isChunkLoadingEnabled());
        data.putInt("chunkRadius", bus.getChunkLoadingRadius());
        data.putInt("chunkExpected", bus.getChunkLoadingExpected());
        data.putInt("chunkHeld", bus.getChunkLoadingHeld());
        data.putInt("chunkShared", bus.getChunkLoadingShared());
        data.putBoolean("output", bus.isOutput());
        data.putBoolean("inputConnected", bus.isInputConnected());
        data.putLong("connectedOutputPos", bus.getConnectedOutputPos().asLong());

        if (bus.isOutput()) {
            List<ConnectionInfo> links = new ArrayList<>(bus.getOutputConnections());
            links.sort(Comparator.comparingInt((ConnectionInfo link) -> link.inputPos().getX())
                    .thenComparingInt(link -> link.inputPos().getY())
                    .thenComparingInt(link -> link.inputPos().getZ()));
            data.putInt("linkCount", links.size());
            ListTag encodedLinks = new ListTag();
            for (ConnectionInfo link : links) {
                CompoundTag encoded = new CompoundTag();
                encoded.putLong("pos", link.inputPos().asLong());
                encodedLinks.add(encoded);
            }
            data.put("links", encodedLinks);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof WirelessBusBlockEntity bus)) return;
        CompoundTag data = accessor.getServerData();
        if (!data.contains("active", Tag.TAG_BYTE)) return;

        tooltip.add(Component.translatable(data.getBoolean("active") ? "top.aewirelesschannel.device_online"
                : "top.aewirelesschannel.device_offline"));

        if (!data.getBoolean("channelAvailable")) {
            tooltip.add(Component.translatable("top.aewirelesschannel.channel_unavailable"));
        } else {
            tooltip.add(Component.translatable("top.aewirelesschannel.channel_usage",
                    data.getInt("channels"), data.getString("channelLimit")));
        }

        showChunkLoading(tooltip, data);
        Player player = accessor.getPlayer();
        if (ModConfig.TOP_CONNECTION_INFO_SHOW_TYPE.get() && player != null && !player.isShiftKeyDown()) return;
        if (data.getBoolean("output")) showOutputConnections(tooltip, data);
        else showInputConnection(tooltip, data);
    }

    private static void showChunkLoading(ITooltip tooltip, CompoundTag data) {
        if (!data.getBoolean("chunkLoading")) return;
        int radius = data.getInt("chunkRadius");
        tooltip.add(Component.translatable("top.aewirelesschannel.chunk_loading_header"));
        if (radius >= 0) {
            tooltip.add(Component.translatable("top.aewirelesschannel.chunk_loading_area",
                    radius * 2 + 1, radius * 2 + 1, radius));
        }
        if (data.getInt("chunkHeld") < 0 || data.getInt("chunkExpected") < 0) {
            tooltip.add(Component.translatable("top.aewirelesschannel.chunk_loading_waiting"));
        } else {
            tooltip.add(Component.translatable("top.aewirelesschannel.chunk_loading_held",
                    data.getInt("chunkHeld"), data.getInt("chunkExpected")));
        }
        if (data.getInt("chunkShared") >= 0) {
            tooltip.add(Component.translatable("top.aewirelesschannel.chunk_loading_shared",
                    data.getInt("chunkShared")));
        }
    }

    private static void showInputConnection(ITooltip tooltip, CompoundTag data) {
        tooltip.add(Component.translatable("top.aewirelesschannel.connection_header"));
        if (!data.getBoolean("inputConnected")) {
            tooltip.add(Component.translatable("top.aewirelesschannel.input_unconnected"));
            return;
        }
        BlockPos output = BlockPos.of(data.getLong("connectedOutputPos"));
        tooltip.add(Component.translatable("top.aewirelesschannel.input_source",
                output.getX(), output.getY(), output.getZ()));
    }

    private static void showOutputConnections(ITooltip tooltip, CompoundTag data) {
        ListTag links = data.getList("links", Tag.TAG_COMPOUND);
        tooltip.add(Component.translatable("top.aewirelesschannel.connection_header"));
        if (links.isEmpty()) {
            tooltip.add(Component.translatable("top.aewirelesschannel.output_no_inputs"));
            return;
        }
        int visible = Math.min(Math.max(0, ModConfig.TOP_CONNECTION_INFO_SHOW_COUNT.get()), links.size());
        for (int i = 0; i < visible; i++) {
            BlockPos pos = BlockPos.of(links.getCompound(i).getLong("pos"));
            tooltip.add(Component.translatable("top.aewirelesschannel.output_input",
                    i + 1, pos.getX(), pos.getY(), pos.getZ()));
        }
        if (data.getInt("linkCount") > visible) {
            tooltip.add(Component.translatable("top.aewirelesschannel.connection_more", data.getInt("linkCount") - visible));
        }
    }
}
