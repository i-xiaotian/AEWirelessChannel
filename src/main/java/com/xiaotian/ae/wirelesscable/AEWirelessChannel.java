package com.xiaotian.ae.wirelesscable;

import com.mojang.logging.LogUtils;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.config.LegacyConfigMigration;
import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AEWirelessChannel.MOD_ID)
public final class AEWirelessChannel {
    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE2 Wireless Channel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AEWirelessChannel() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContent.register(modBus);
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
        modBus.addListener(this::commonSetup);
        modBus.addListener(NetworkChunkLoading::registerTicketController);
        modBus.addListener(LegacyConfigMigration::onConfigLoading);
        NeoForge.EVENT_BUS.register(NetworkChunkLoading.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(WirelessLinkNetwork::register);
    }
}
