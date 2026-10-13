package com.xiaotian.ae.wirelesscable.client;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        boolean holdingCard = minecraft.player.getMainHandItem().is(ModContent.WIRELESS_KEY_CARD.get())
                || minecraft.player.getOffhandItem().is(ModContent.WIRELESS_KEY_CARD.get());
        if (holdingCard && minecraft.level.getGameTime() % 10 == 0) WirelessLinkNetwork.refresh();
    }
}
