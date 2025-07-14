package com.xiaotian.ae.wirelesscable.common.config;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import net.minecraftforge.common.config.Config;

@Config(modid = AEWirelessCable.modID, name = "ae_wireless_cable")
public class AEWirelessCableConfig {

    @Config.Comment("Whether to force the bus to load its chunk")
    public static boolean forceChunk = false;

}
