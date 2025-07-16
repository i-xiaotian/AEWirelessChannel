package com.xiaotian.ae.wirelesscable.common.registry;

import com.xiaotian.ae.wirelesscable.common.block.BlockBaseWirelessInputBus;
import com.xiaotian.ae.wirelesscable.common.block.BlockBaseWirelessOutputBus;
import net.minecraft.block.Block;

public class Blocks {

    public static Block WIRELESS_OUTPUT_BUS;
    public static Block WIRELESS_INPUT_BUS;

    public static void preInit() {
        WIRELESS_OUTPUT_BUS = Registry.registerBlock(new BlockBaseWirelessOutputBus());
        WIRELESS_INPUT_BUS = Registry.registerBlock(new BlockBaseWirelessInputBus());
    }

}
