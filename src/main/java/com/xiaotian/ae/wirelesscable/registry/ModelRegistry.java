package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.client.model.BloomLightModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = AEWirelessChannel.MOD_ID)
public class ModelRegistry {

    public final static List<Block> BLOOM_BLOCK_LIST = new ArrayList<>();


//    @SubscribeEvent
//    public static void onModelBake(ModelBakeEvent event) {
//        if (BLOOM_BLOCK_LIST.isEmpty()) return;
//        for (Block block : BLOOM_BLOCK_LIST) {
//            final ResourceLocation registryName = block.getRegistryName();
//            if (Objects.isNull(registryName)) continue;
//            final String blockName = registryName.toString();
//            for (Direction facing : Direction.values()) {
//                String variant = String.format("facing=%s,powered=true", facing.getName());
//                ModelResourceLocation modelResLoc = new ModelResourceLocation(blockName, variant);
//                IBakedModel base = event.getModelRegistry().get(modelResLoc);
//                if (base != null) {
//                    event.getModelRegistry().put(modelResLoc, new BloomLightModel(base));
//                }
//            }
//        }
//    }

}
