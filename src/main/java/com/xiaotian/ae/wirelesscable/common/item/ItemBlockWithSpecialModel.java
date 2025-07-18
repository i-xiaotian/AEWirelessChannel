package com.xiaotian.ae.wirelesscable.common.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.common.block.BlockBusBase;
import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import com.xiaotian.ae.wirelesscable.common.render.BlockBusItemRender;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class ItemBlockWithSpecialModel extends ItemBlock {

    public ItemBlockWithSpecialModel(final BlockBusBase block, final String blockId) {
        super(block);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, blockId));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + blockId);
        this.setTileEntityItemStackRenderer(new BlockBusItemRender());
    }
}
