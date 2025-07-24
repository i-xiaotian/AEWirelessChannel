package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import net.minecraft.tileentity.TileEntityType;

import static com.xiaotian.ae.wirelesscable.registry.Blocks.*;

public class TileEntityTypes {

    public static TileEntityType<TileWirelessInputBus> TILE_WIRELESS_INPUT_BUS;

    public static TileEntityType<TileWirelessOutputBus> TILE_WIRELESS_OUTPUT_BUS;

    public static TileEntityType<TileDenseWirelessInputBus> TILE_DENSE_WIRELESS_INPUT_BUS;

    public static TileEntityType<TileDenseWirelessOutputBus> TILE_DENSE_WIRELESS_OUTPUT_BUS;


    public static void register() {
        TILE_WIRELESS_INPUT_BUS = Registry.createType("tile_wireless_input_bus", TileWirelessInputBus::new, WIRELESS_INPUT_BUS);
        TILE_WIRELESS_OUTPUT_BUS= Registry.createType("tile_wireless_output_bus", TileWirelessOutputBus::new, WIRELESS_OUTPUT_BUS);
        TILE_DENSE_WIRELESS_INPUT_BUS = Registry.createType("tile_dense_wireless_input_bus", TileDenseWirelessInputBus::new, DENSE_WIRELESS_INPUT_BUS);
        TILE_DENSE_WIRELESS_OUTPUT_BUS = Registry.createType("tile_dense_wireless_output_bus", TileDenseWirelessOutputBus::new, DENSE_WIRELESS_OUTPUT_BUS);
    }
}
