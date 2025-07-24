package com.xiaotian.ae.wirelesscable.proxy;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.integration.top.TopInfoProvider;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.registry.TileEntityTypes;
import com.xiaotian.ae.wirelesscable.tab.AEWirelessItemGroup;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy {

    public static AEWirelessItemGroup creativeTab;

    public void setup(final FMLCommonSetupEvent event) {
        creativeTab = new AEWirelessItemGroup(AEWirelessChannel.MOD_ID);
        Blocks.register();
        Items.register();
        TileEntityTypes.register();
    }

    public void init() {
        if (ModList.get().isLoaded("theoneprobe")) {
            final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
            top.registerProvider(new TopInfoProvider());
        }
//        ForgeChunkManager.setForcedChunkLoadingCallback(AEWirelessChannel.instance, new ChunkLoadingCallback());
    }

}
