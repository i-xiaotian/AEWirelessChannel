package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileDenseWirelessOutputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.xiaotian.ae.wirelesscable.registry.Blocks.*;

public class TileEntityTypes {

    private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, AEWirelessChannel.MOD_ID);

    public static RegistryObject<TileEntityType<TileWirelessInputBus>> TILE_WIRELESS_INPUT_BUS;
    public static RegistryObject<TileEntityType<TileWirelessOutputBus>> TILE_WIRELESS_OUTPUT_BUS;
    public static RegistryObject<TileEntityType<TileDenseWirelessInputBus>> TILE_DENSE_WIRELESS_INPUT_BUS;
    public static RegistryObject<TileEntityType<TileDenseWirelessOutputBus>> TILE_DENSE_WIRELESS_OUTPUT_BUS;


    @SuppressWarnings("ConstantConditions")
    public static void register(final IEventBus modEventBus) {
        TILE_WIRELESS_INPUT_BUS = REGISTRY.register("tile_wireless_input_bus", () ->
                TileEntityType.Builder.of(TileWirelessInputBus::new, WIRELESS_INPUT_BUS.get()).build(null));

        TILE_WIRELESS_OUTPUT_BUS = REGISTRY.register("tile_wireless_output_bus", () ->
                TileEntityType.Builder.of(TileWirelessOutputBus::new, WIRELESS_OUTPUT_BUS.get()).build(null));

        TILE_DENSE_WIRELESS_INPUT_BUS = REGISTRY.register("tile_dense_wireless_input_bus", () ->
                TileEntityType.Builder.of(TileDenseWirelessInputBus::new, DENSE_WIRELESS_INPUT_BUS.get()).build(null));

        TILE_DENSE_WIRELESS_OUTPUT_BUS = REGISTRY.register("tile_dense_wireless_output_bus", () ->
                TileEntityType.Builder.of(TileDenseWirelessOutputBus::new, DENSE_WIRELESS_OUTPUT_BUS.get()).build(null));

        REGISTRY.register(modEventBus);
    }

}
