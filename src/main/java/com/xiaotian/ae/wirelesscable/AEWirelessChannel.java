package com.xiaotian.ae.wirelesscable;

import com.mojang.logging.LogUtils;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.config.LegacyConfigMigration;
import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.slf4j.Logger;

@Mod(AEWirelessChannel.MOD_ID)
public final class AEWirelessChannel {
    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE2 Wireless Channel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AEWirelessChannel(IEventBus modBus, ModContainer container) {
        ModContent.register(modBus);
        container.registerConfig(Type.COMMON, ModConfig.SPEC);
        modBus.addListener(WirelessLinkNetwork::register);
        modBus.addListener(NetworkChunkLoading::registerTicketController);
        modBus.addListener(LegacyConfigMigration::onConfigLoading);
        modBus.addListener(this::enqueueTopIntegration);
        NeoForge.EVENT_BUS.register(NetworkChunkLoading.class);
    }

    private void enqueueTopIntegration(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("theoneprobe")) return;
        try {
            Class.forName("com.xiaotian.ae.wirelesscable.integration.top.TopIntegration")
                    .getMethod("enqueue").invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("Failed to enqueue The One Probe integration", exception);
        }
    }

}
