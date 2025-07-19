package com.xiaotian.ae.wirelesscable.config;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.minecraftforge.common.config.Config;

@Config(modid = AEWirelessChannel.MOD_ID, name = "ae_wireless_cable")
public class AEWirelessCableConfig {

    @Config.Comment("Whether to force the bus to load its chunk")
    public static boolean forceChunk = false;

}
