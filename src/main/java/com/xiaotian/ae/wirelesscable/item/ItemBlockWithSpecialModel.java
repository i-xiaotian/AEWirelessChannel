package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.BlockBaseBus;
import com.xiaotian.ae.wirelesscable.proxy.CommonProxy;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class ItemBlockWithSpecialModel extends ItemBlock {

    public ItemBlockWithSpecialModel(final BlockBaseBus block, final String blockId) {
        super(block);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, blockId));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + blockId);
    }
}
