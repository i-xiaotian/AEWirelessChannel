package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.proxy.CommonProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class ItemBase extends Item implements IHasTooltips {

    public ItemBase() {
        super();
        this.setCreativeTab(CommonProxy.creativeTab);
        this.setRegistryName(new ResourceLocation(AEWirelessChannel.MOD_ID, getItemId()));
        this.setTranslationKey(AEWirelessChannel.MOD_ID + "." + getItemId());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addTooltips(stack, tooltip);
    }

    public abstract String getItemId();

}
