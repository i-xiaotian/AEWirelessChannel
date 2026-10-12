package com.xiaotian.ae.wirelesscable.client;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.client.model.BloomLightModel;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RenderType cutout = RenderType.cutoutMipped();
            ItemBlockRenderTypes.setRenderLayer(ModContent.WIRELESS_OUTPUT_BUS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModContent.WIRELESS_INPUT_BUS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModContent.DENSE_WIRELESS_OUTPUT_BUS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModContent.DENSE_WIRELESS_INPUT_BUS.get(), cutout);
        });
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) ->
                location.getNamespace().equals(AEWirelessChannel.MOD_ID)
                        ? new BloomLightModel(model)
                        : model);
    }
}
