package com.xiaotian.ae.wirelesscable.proxy;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.integration.top.TopInfoProvider;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.tab.AEWirelessTab;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Loader;

public class CommonProxy {

    public static CreativeTabs creativeTab;

    public void preInit() {
        creativeTab = new AEWirelessTab(AEWirelessChannel.MOD_ID);
        Blocks.preInit();
        Items.preInit();
    }

    public void init() {
        if (Loader.isModLoaded("theoneprobe")) {
            final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
            top.registerProvider(new TopInfoProvider());
        }
    }

}
