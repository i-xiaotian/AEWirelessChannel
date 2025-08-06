package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Items {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, AEWirelessChannel.MOD_ID);

    public static RegistryObject<Item> ITEM_WIRELESS_KEY_CARD;

    public static void register(final IEventBus modEventBus) {
        ITEM_WIRELESS_KEY_CARD = REGISTRY.register("item_wireless_key_card", ItemWirelessKeyCard::new);
        REGISTRY.register(modEventBus);
    }

}
