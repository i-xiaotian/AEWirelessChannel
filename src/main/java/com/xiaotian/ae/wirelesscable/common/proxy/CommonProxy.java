package com.xiaotian.ae.wirelesscable.common.proxy;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import com.xiaotian.ae.wirelesscable.common.tab.AEWirelessTab;
import net.minecraft.creativetab.CreativeTabs;

public class CommonProxy {

    public static CreativeTabs creativeTab;

    public void preInit() {
        creativeTab = new AEWirelessTab(AEWirelessCable.MOD_ID);
        Blocks.init();
    }

}
