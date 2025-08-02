package com.xiaotian.ae.wirelesscable;

import com.xiaotian.ae.wirelesscable.config.AEWirelessChannelConfig;
import com.xiaotian.ae.wirelesscable.integration.top.TopInfoProvider;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.registry.TileEntityTypes;
import com.xiaotian.ae.wirelesscable.tab.AEWirelessItemGroup;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AEWirelessChannel.MOD_ID)
public class AEWirelessChannel {

    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE-Wireless-Channel";

    public static AEWirelessChannel instance;
    public final static Logger log = LogManager.getLogger();
    public static AEWirelessItemGroup wirelessItemGroup;

    public AEWirelessChannel() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEWirelessChannelConfig.CLIENT_SPEC);
        wirelessItemGroup = new AEWirelessItemGroup(AEWirelessChannel.MOD_ID);
        Blocks.register();
        Items.register();
        TileEntityTypes.register();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        log.info(AEWirelessChannel.MOD_NAME + " common setup start.");

    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
            top.registerProvider(new TopInfoProvider());
        }
    }


}
