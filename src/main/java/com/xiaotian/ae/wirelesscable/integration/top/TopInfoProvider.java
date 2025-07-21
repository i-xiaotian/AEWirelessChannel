package com.xiaotian.ae.wirelesscable.integration.top;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.config.AEWirelessCableConfig;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TopInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return AEWirelessChannel.MOD_ID + ":top_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode probeMode, final IProbeInfo iProbeInfo, final EntityPlayer entityPlayer, final World world, final IBlockState iBlockState, final IProbeHitData iProbeHitData) {
        if (!iBlockState.getBlock().hasTileEntity(iBlockState)) return;

        final BlockPos pos = iProbeHitData.getPos();
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (Objects.isNull(tileEntity)) return;
        if (!(tileEntity instanceof final TileWirelessBus tileWirelessBus)) return;

        final AENetworkProxy proxy = tileWirelessBus.getProxy();
        final boolean active = proxy.isActive();
        final String onlineInfo = active ? "{*top.aewirelesschannel.device_online*}" : "{*top.aewirelesschannel.device_offline*}";
        final TextFormatting color = active ? TextFormatting.WHITE : TextFormatting.GRAY;
        iProbeInfo.text(color + onlineInfo);
        if (AEWirelessCableConfig.topConnectionInfoShowType && !entityPlayer.isSneaking()) return;

        if (tileWirelessBus instanceof TileWirelessInputBus wirelessInputBus) {
            showInputBusTopInfo(iProbeInfo, wirelessInputBus);
        } else if (tileWirelessBus instanceof TileWirelessOutputBus wirelessOutputBus) {
            showOutputBusTopInfo(iProbeInfo, wirelessOutputBus);
        }
    }

    private static void showInputBusTopInfo(final IProbeInfo iProbeInfo, final TileWirelessInputBus wirelessInputBus) {
        final ConnectionInfo currentConnection = wirelessInputBus.getCurrentConnection();
        final boolean connect = currentConnection.isConnect();
        if (!connect) return;
        final int outputBusX = currentConnection.getOutputBusX();
        final int outputBusY = currentConnection.getOutputBusY();
        final int outputBusZ = currentConnection.getOutputBusZ();
        final String connectionKey = currentConnection.getConnectionKey();
        final IProbeInfo vertical = getVertical(iProbeInfo);
        final IProbeInfo horizontal = vertical.horizontal();
        horizontal.text("{*top.aewirelesschannel.wireless_bus_connection_info_header*}");
        final IProbeInfo info = getVertical(vertical);
        final IProbeInfo connectionKeyInfo = info.horizontal();
        final IProbeInfo connectionInfo = info.horizontal();
        connectionKeyInfo.text(I18n.format("top.aewirelesschannel.wireless_bus_connection_info", connectionKey));
        connectionInfo.text(I18n.format("top.aewirelesschannel.wireless_input_bus_connection_info", outputBusX, outputBusY, outputBusZ));
    }

    private static void showOutputBusTopInfo(final IProbeInfo iProbeInfo, final TileWirelessOutputBus wirelessOutputBus) {
        final Map<String, ConnectionInfo> gridConnectionMap = wirelessOutputBus.getSubGridConnectionMap();
        if (gridConnectionMap.isEmpty()) return;
        final Set<String> keySet = gridConnectionMap.keySet();
        final IProbeInfo vertical = getVertical(iProbeInfo);
        final IProbeInfo horizontal = vertical.horizontal();
        horizontal.text("{*top.aewirelesschannel.wireless_bus_connection_info_header*}");
        int showCount = 0;
        for (String connectionKey : keySet) {
            final IProbeInfo info = getVertical(vertical);
            final IProbeInfo connectionKeyInfo = info.horizontal();
            final IProbeInfo connectionInfo = info.horizontal();

            final ConnectionInfo inputBusConnectionInfo = gridConnectionMap.get(connectionKey);
            connectionKeyInfo.text(I18n.format("top.aewirelesschannel.wireless_bus_connection_info", connectionKey));
            connectionInfo.text(I18n.format("top.aewirelesschannel.wireless_output_bus_connection_info", inputBusConnectionInfo.getInputBusX(), inputBusConnectionInfo.getInputBusY(), inputBusConnectionInfo.getInputBusZ()));
            showCount++;
            if (showCount >= AEWirelessCableConfig.topConnectionInfoShowCount) {
                final int size = keySet.size();
                final int remainingCount = size - AEWirelessCableConfig.topConnectionInfoShowCount;
                final IProbeInfo moreThanInfoV = getVertical(vertical);
                final IProbeInfo moreThanInfoH = moreThanInfoV.horizontal();
                moreThanInfoH.text(I18n.format("top.aewirelesschannel.wireless_bus_connection_info_more_than", remainingCount));
                break;
            }
        }
    }

    private static IProbeInfo getVertical(final IProbeInfo iProbeInfo) {
        return iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle()
                .spacing(0)
                .borderColor(0x80FFC080));
    }
}
