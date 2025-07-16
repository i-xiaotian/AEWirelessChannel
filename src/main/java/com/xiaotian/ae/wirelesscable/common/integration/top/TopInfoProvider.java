package com.xiaotian.ae.wirelesscable.common.integration.top;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.common.tile.TileWirelessBus;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Objects;

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
    }
}
