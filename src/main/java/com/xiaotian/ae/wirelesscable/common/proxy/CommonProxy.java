package com.xiaotian.ae.wirelesscable.common.proxy;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import com.xiaotian.ae.wirelesscable.common.integration.top.TopInfoProvider;
import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import com.xiaotian.ae.wirelesscable.common.tab.AEWirelessTab;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Loader;

public class CommonProxy {

    public static CreativeTabs creativeTab;

    public void preInit() {
        creativeTab = new AEWirelessTab(AEWirelessCable.MOD_ID);
        Blocks.init();
    }

    public void init() {
        if (Loader.isModLoaded("theoneprobe")) {
            final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
            top.registerProvider(new TopInfoProvider());
        }
    }

}
