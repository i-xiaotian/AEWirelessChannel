package com.xiaotian.ae.wirelesscable;

import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = AEWirelessChannel.MOD_ID, name = AEWirelessChannel.MOD_NAME, version = AEWirelessChannel.VERSION,
        dependencies = "required-after:forge@[14.21.0.2371,);" +
                "after:appliedenergistics2@[rv6-stable-7,);",
        acceptedMinecraftVersions = "[1.12, 1.13)")
@SuppressWarnings("unused")
public class AEWirelessChannel {

    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE-Wireless-Channel";
    public static final String VERSION = Tags.VERSION;
    private static final String COMMON_PROXY = "com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy";
    private static final String CLIENT_PROXY = "com.xiaotian.ae.wirelesscable.common.proxy.ClientProxy";

    public static Logger log;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        event.getModMetadata().version = VERSION;
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

}
