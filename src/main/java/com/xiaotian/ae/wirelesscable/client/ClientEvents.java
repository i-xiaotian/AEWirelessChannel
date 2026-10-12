package com.xiaotian.ae.wirelesscable.client;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        boolean holdingCard = minecraft.player.getMainHandItem().is(ModContent.WIRELESS_KEY_CARD.get())
                || minecraft.player.getOffhandItem().is(ModContent.WIRELESS_KEY_CARD.get());
        if (holdingCard && minecraft.level.getGameTime() % 10 == 0) WirelessLinkNetwork.refresh();
    }
}
