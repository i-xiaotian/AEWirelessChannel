package com.xiaotian.ae.wirelesscable.registry;

import appeng.api.AECapabilities;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, AEWirelessChannel.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AEWirelessChannel.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AEWirelessChannel.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AEWirelessChannel.MOD_ID);

    public static final DeferredHolder<Block, Block> WIRELESS_OUTPUT_BUS = registerBus("block_wireless_output_bus", true, false);
    public static final DeferredHolder<Block, Block> WIRELESS_INPUT_BUS = registerBus("block_wireless_input_bus", false, false);
    public static final DeferredHolder<Block, Block> DENSE_WIRELESS_OUTPUT_BUS = registerBus("block_dense_wireless_output_bus", true, true);
    public static final DeferredHolder<Block, Block> DENSE_WIRELESS_INPUT_BUS = registerBus("block_dense_wireless_input_bus", false, true);

    public static final DeferredHolder<Item, Item> WIRELESS_KEY_CARD = ITEMS.register("item_wireless_key_card",
            () -> new ItemWirelessKeyCard(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> WIRELESS_OUTPUT_BUS_ITEM = registerBlockItem("block_wireless_output_bus", WIRELESS_OUTPUT_BUS);
    public static final DeferredHolder<Item, Item> WIRELESS_INPUT_BUS_ITEM = registerBlockItem("block_wireless_input_bus", WIRELESS_INPUT_BUS);
    public static final DeferredHolder<Item, Item> DENSE_WIRELESS_OUTPUT_BUS_ITEM = registerBlockItem("block_dense_wireless_output_bus", DENSE_WIRELESS_OUTPUT_BUS);
    public static final DeferredHolder<Item, Item> DENSE_WIRELESS_INPUT_BUS_ITEM = registerBlockItem("block_dense_wireless_input_bus", DENSE_WIRELESS_INPUT_BUS);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessBusBlockEntity>> WIRELESS_OUTPUT_BUS_ENTITY = registerBusEntity("tile_wireless_output_bus", WIRELESS_OUTPUT_BUS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessBusBlockEntity>> WIRELESS_INPUT_BUS_ENTITY = registerBusEntity("tile_wireless_input_bus", WIRELESS_INPUT_BUS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessBusBlockEntity>> DENSE_WIRELESS_OUTPUT_BUS_ENTITY = registerBusEntity("tile_dense_wireless_output_bus", DENSE_WIRELESS_OUTPUT_BUS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessBusBlockEntity>> DENSE_WIRELESS_INPUT_BUS_ENTITY = registerBusEntity("tile_dense_wireless_input_bus", DENSE_WIRELESS_INPUT_BUS);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.aewirelesschannel"))
                    .icon(() -> WIRELESS_KEY_CARD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(WIRELESS_OUTPUT_BUS_ITEM.get());
                        output.accept(WIRELESS_INPUT_BUS_ITEM.get());
                        output.accept(DENSE_WIRELESS_OUTPUT_BUS_ITEM.get());
                        output.accept(DENSE_WIRELESS_INPUT_BUS_ITEM.get());
                        output.accept(WIRELESS_KEY_CARD.get());
                    })
                    .build());

    private ModContent() {
    }

    private static DeferredHolder<Block, Block> registerBus(String id, boolean output, boolean dense) {
        return BLOCKS.register(id, () -> new WirelessBusBlock(output, dense,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
                        .strength(1.0F, 5.0F)
                        .noOcclusion()
                        .isRedstoneConductor((state, level, pos) -> false)));
    }

    private static DeferredHolder<Item, Item> registerBlockItem(String id, DeferredHolder<Block, Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessBusBlockEntity>> registerBusEntity(
            String id, DeferredHolder<Block, Block> block) {
        return BLOCK_ENTITIES.register(id,
                () -> BlockEntityType.Builder.of(WirelessBusBlockEntity::new, block.get()).build(null));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        TABS.register(modBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerGridNodeHost(event, WIRELESS_OUTPUT_BUS_ENTITY.get());
        registerGridNodeHost(event, WIRELESS_INPUT_BUS_ENTITY.get());
        registerGridNodeHost(event, DENSE_WIRELESS_OUTPUT_BUS_ENTITY.get());
        registerGridNodeHost(event, DENSE_WIRELESS_INPUT_BUS_ENTITY.get());
    }

    private static void registerGridNodeHost(RegisterCapabilitiesEvent event,
                                             BlockEntityType<WirelessBusBlockEntity> blockEntityType) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, blockEntityType,
                (blockEntity, context) -> blockEntity);
    }
}
