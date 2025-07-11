package com.xiaotian.ae.wirelesscable;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = AEWirelessCable.modID, name = AEWirelessCable.modName, version = AEWirelessCable.version,
        dependencies = "required-after:forge@[14.21.0.2371,);" +
                "after:appliedenergistics2@[rv6-stable-7,);",
        acceptedMinecraftVersions = "[1.12, 1.13)")
@SuppressWarnings("unused")
public class AEWirelessCable {

    public static final String modID = "aewirelesscable";
    public static final String modName = "AE: Wireless Cable";
    public static final String version = Tags.VERSION;



}
