package com.xiaotian.ae.wirelesscable.proxy;

import com.xiaotian.ae.wirelesscable.client.WirelessLinkRenderer;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        MinecraftForge.EVENT_BUS.register(new WirelessLinkRenderer());
    }

    @Override
    public void init() {
        super.init();
    }
}
