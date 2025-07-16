package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.common.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public abstract class BlockBusBase extends Block implements IBlockBase {

    public BlockBusBase(Material material) {
        super(material);
        this.setSoundType(SoundType.GLASS);
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, getBlockId()));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + getBlockId());
    }

}
