package com.xiaotian.ae.wirelesscable.registry;

import com.mojang.datafixers.DSL;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.IBlockBase;
import com.xiaotian.ae.wirelesscable.block.IBloomTexture;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.xiaotian.ae.wirelesscable.AEWirelessChannel.log;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT, Dist.DEDICATED_SERVER}, modid = AEWirelessChannel.MOD_ID)
public class Registry {

    public static final List<Block> BLOCK_LIST = new ArrayList<>();
    public static final List<TileEntityType<?>> TILE_LIST = new ArrayList<>();
    public static final List<Item> ITEM_LIST = new ArrayList<>();

    @Nonnull
    public static <T extends Block> T registerBlock(String name, @Nonnull T block) {
        block.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, name));
        BLOCK_LIST.add(block);
        return block;
    }

    @Nonnull
    public static <T extends Item> T registerItem(String name, @Nonnull T item) {
        item.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, name));
        ITEM_LIST.add(item);
        return item;
    }

    @Nonnull
    public static <T extends TileEntity> TileEntityType<T> createType(String registryName, Supplier<T> factory, Block... validBlocks) {
        TileEntityType<T> type = TileEntityType.Builder
                .of(factory, validBlocks)
                .build(DSL.remainderType());
        type.setRegistryName(registryName);
        TILE_LIST.add(type);
        return type;
    }

    @SubscribeEvent
    public static void onRegisterBlock(RegistryEvent.Register<Block> event) {
        log.info("block size: {}", BLOCK_LIST.size());
        BLOCK_LIST.forEach(block -> {
            event.getRegistry().register(block);
            if (block instanceof IBloomTexture) ModelRegistry.BLOOM_BLOCK_LIST.add(block);
        });
    }

    @SubscribeEvent
    public static void onRegisterTileEntities(@Nonnull RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(TILE_LIST.toArray(new TileEntityType[0]));
    }

    @SubscribeEvent
    public static void onRegisterItem(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCK_LIST) {
            BlockItem itemBlock;
            if (block instanceof IBlockBase) {
                IBlockBase blockBase = (IBlockBase) block;
                itemBlock = blockBase.createItemBlock(block);
            } else {
                itemBlock = new BlockItem(block, new Item.Properties());
                final ResourceLocation registryName = block.getRegistryName();
                if (Objects.nonNull(registryName)) itemBlock.setRegistryName(registryName);
            }
            event.getRegistry().register(itemBlock);
        }
        event.getRegistry().registerAll(ITEM_LIST.toArray(new Item[0]));
    }


}
