package com.xiaotian.ae.wirelesscable.proxy;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.integration.top.TopInfoProvider;
import com.xiaotian.ae.wirelesscable.tab.AEWirelessItemGroup;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import static com.xiaotian.ae.wirelesscable.AEWirelessChannel.log;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.DEDICATED_SERVER, Dist.CLIENT}, modid = AEWirelessChannel.MOD_ID)
public class ModSetupComponent {

    public static AEWirelessItemGroup wirelessItemGroup;

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        log.info(AEWirelessChannel.MOD_NAME + " common setup start.");
        if (ModList.get().isLoaded("theoneprobe")) {
            final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
            top.registerProvider(new TopInfoProvider());
        }
    }

}
