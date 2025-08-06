package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Blocks {

    private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, AEWirelessChannel.MOD_ID);
    private static final List<Block> translucentBlockList = new ArrayList<>();

    public static RegistryObject<BlockBaseWirelessBus> WIRELESS_INPUT_BUS;
    public static RegistryObject<BlockBaseWirelessBus> WIRELESS_OUTPUT_BUS;
    public static RegistryObject<BlockBaseWirelessBus> DENSE_WIRELESS_INPUT_BUS;
    public static RegistryObject<BlockBaseWirelessBus> DENSE_WIRELESS_OUTPUT_BUS;

    public static void register(final IEventBus modEventBus) {
        WIRELESS_INPUT_BUS = registerBlock(BlockWirelessInputBus::new);
        WIRELESS_OUTPUT_BUS = registerBlock(BlockWirelessOutputBus::new);
        DENSE_WIRELESS_INPUT_BUS = registerBlock(BlockDenseWirelessInputBus::new);
        DENSE_WIRELESS_OUTPUT_BUS = registerBlock(BlockDenseWirelessOutputBus::new);
        REGISTRY.register(modEventBus);
    }

    private static <T extends BlockBaseWirelessBus> RegistryObject<BlockBaseWirelessBus> registerBlock(Supplier<T> sup) {
        final BlockBaseWirelessBus blockBaseWirelessBus = sup.get();
        translucentBlockList.add(blockBaseWirelessBus);
        ModelRegistry.BLOOM_BLOCK_LIST.add(blockBaseWirelessBus);
        Items.REGISTRY.register(blockBaseWirelessBus.getBlockId(), () -> blockBaseWirelessBus.createItemBlock(blockBaseWirelessBus));
        return REGISTRY.register(blockBaseWirelessBus.getBlockId(), () -> blockBaseWirelessBus);
    }

    public static void setTranslucentBlockRenderType() {
        translucentBlockList.forEach(block -> RenderTypeLookup.setRenderLayer(block, RenderType.translucent()));
    }

}
