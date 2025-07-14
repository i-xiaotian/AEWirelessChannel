package com.xiaotian.ae.wirelesscable.common.registry;

import com.xiaotian.ae.wirelesscable.common.block.WirelessOutputBus;

public class Blocks {

    public static WirelessOutputBus WIRELESS_OUTPUT_BUS;

    public static void init() {
        WIRELESS_OUTPUT_BUS = BlockRegistry.registerBlock(new WirelessOutputBus());
    }

}
