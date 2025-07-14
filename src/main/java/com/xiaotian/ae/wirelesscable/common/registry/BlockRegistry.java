package com.xiaotian.ae.wirelesscable.common.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import com.xiaotian.ae.wirelesscable.common.block.IHasTileEntity;
import com.xiaotian.ae.wirelesscable.common.tile.TileOutputBusWireless;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = AEWirelessCable.MOD_ID)
public class BlockRegistry {

    private static final List<Block> BLOCK_LIST = new ArrayList<>();

    private static final List<Item> ITEM_LIST = new ArrayList<>();

    public static <T extends Block> T registerBlock(T block) {
        BLOCK_LIST.add(block);
        return block;
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        AEWirelessCable.log.info("where is my block?: {}", BLOCK_LIST.size());
        event.getRegistry().registerAll(BLOCK_LIST.toArray(new Block[0]));
        for (Block block : BLOCK_LIST) {
            if (!(block instanceof IHasTileEntity)) return;
            IHasTileEntity hasTileEntity = (IHasTileEntity) block;
            final Class<? extends TileEntity> tileEntityClass = hasTileEntity.getTileEntityClass();
            final String tileEntityId = hasTileEntity.getTileEntityId();
            GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(AEWirelessCable.MOD_ID, tileEntityId));
        }
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCK_LIST) {
            final ItemBlock itemBlock = new ItemBlock(block);
            final ResourceLocation registryName = block.getRegistryName();
            final String translationKey = block.getTranslationKey();
            if (Objects.nonNull(registryName)) itemBlock.setRegistryName(registryName);
            itemBlock.setTranslationKey(translationKey);
            event.getRegistry().register(itemBlock);
            ITEM_LIST.add(itemBlock);
        }
    }


    @SubscribeEvent
    public static void registerModel(ModelRegistryEvent event) {
        ITEM_LIST.forEach(item -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory")));
    }


}
