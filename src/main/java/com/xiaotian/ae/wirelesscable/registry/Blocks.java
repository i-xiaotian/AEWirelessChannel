package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.block.BlockDenseWirelessInputBus;
import com.xiaotian.ae.wirelesscable.block.BlockDenseWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.block.BlockWirelessInputBus;
import com.xiaotian.ae.wirelesscable.block.BlockWirelessOutputBus;
import net.minecraft.block.Block;

public class Blocks {

    public static Block WIRELESS_OUTPUT_BUS;
    public static Block WIRELESS_INPUT_BUS;
    public static Block DENSE_WIRELESS_OUTPUT_BUS;
    public static Block DENSE_WIRELESS_INPUT_BUS;

    public static void preInit() {
        WIRELESS_OUTPUT_BUS = Registry.registerBlock(new BlockWirelessOutputBus());
        WIRELESS_INPUT_BUS = Registry.registerBlock(new BlockWirelessInputBus());
        DENSE_WIRELESS_OUTPUT_BUS = Registry.registerBlock(new BlockDenseWirelessOutputBus());
        DENSE_WIRELESS_INPUT_BUS = Registry.registerBlock(new BlockDenseWirelessInputBus());
    }

}
