package com.xiaotian.ae.wirelesscable.registry;

import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import net.minecraft.item.Item;

public class Items {

    public static Item ITEM_WIRELESS_KEY_CARD;

    public static void preInit() {
        ITEM_WIRELESS_KEY_CARD = Registry.registerItem(new ItemWirelessKeyCard());
    }

}
