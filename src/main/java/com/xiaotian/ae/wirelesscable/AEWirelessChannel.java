package com.xiaotian.ae.wirelesscable;

import com.mojang.logging.LogUtils;
import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.config.LegacyConfigMigration;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        modBus.addListener(this::enqueueOptionalIntegrations);
        modBus.addListener(LegacyConfigMigration::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(NetworkChunkLoading.class);
        ForgeChunkManager.setForcedChunkLoadingCallback(MOD_ID, NetworkChunkLoading::restoreTickets);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(WirelessLinkNetwork::register);
    }

    private void enqueueOptionalIntegrations(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("theoneprobe")) return;
        try {
            Class.forName("com.xiaotian.ae.wirelesscable.integration.top.TopIntegration")
                    .getMethod("enqueue").invoke(null);
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("Failed to enqueue The One Probe integration", exception);
        }
    }
}
