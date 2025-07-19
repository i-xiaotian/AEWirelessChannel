package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.client.model.BloomLightModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = AEWirelessChannel.MOD_ID)
public class ModelRegistry {

    public final static List<Block> BLOCK_LIST = new ArrayList<>();

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (Block block : BLOCK_LIST) {
            final ResourceLocation registryName = block.getRegistryName();
            if (Objects.isNull(registryName)) continue;
            final String blockName = registryName.toString();
            for (EnumFacing facing : EnumFacing.values()) {
                String variant = String.format("facing=%s,powered=true", facing.getName());
                ModelResourceLocation modelResLoc = new ModelResourceLocation(blockName, variant);
                IBakedModel base = event.getModelRegistry().getObject(modelResLoc);
                if (base != null) {
                    event.getModelRegistry().putObject(modelResLoc, new BloomLightModel(base));
                }
            }
        }
    }
}
