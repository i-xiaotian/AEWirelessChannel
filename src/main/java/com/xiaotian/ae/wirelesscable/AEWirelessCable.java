package com.xiaotian.ae.wirelesscable;

import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import com.xiaotian.ae.wirelesscable.common.tab.AEWirelessTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Logger;

@Mod(modid = AEWirelessCable.MOD_ID, name = AEWirelessCable.MOD_NAME, version = AEWirelessCable.VERSION,
        dependencies = "required-after:forge@[14.21.0.2371,);" +
                "after:appliedenergistics2@[rv6-stable-7,);",
        acceptedMinecraftVersions = "[1.12, 1.13)")
@SuppressWarnings("unused")
public class AEWirelessCable {

    public static final String MOD_ID = "aewirelesscable";
    public static final String MOD_NAME = "AE-Wireless-Cable";
    public static final String VERSION = Tags.VERSION;
    private static final String COMMON_PROXY = "com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy";
    private static final String CLIENT_PROXY = "com.xiaotian.ae.wirelesscable.common.proxy.ClientProxy";

    public static Logger log;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;
        log = event.getModLog();
        proxy.preInit();
    }

}
