package com.xiaotian.ae.wirelesscable.common.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public abstract class ItemBase extends Item {

    public ItemBase() {
        super();
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, getItemId()));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + getItemId());
    }

    public abstract String getItemId();

}
