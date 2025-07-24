package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import net.minecraft.item.Item;

public class Items {

    public static Item ITEM_WIRELESS_KEY_CARD;

    public static void register() {
        ITEM_WIRELESS_KEY_CARD = Registry.registerItem("item_wireless_key_card", new ItemWirelessKeyCard());
    }

}
