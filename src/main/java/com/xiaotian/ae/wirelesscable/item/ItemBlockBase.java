package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

public abstract class ItemBlockBase extends ItemBlock implements IHasTooltips {

    public ItemBlockBase(Block block) {
        super(block);
        final ResourceLocation registryName = block.getRegistryName();
        if (Objects.nonNull(registryName)) setRegistryName(block.getRegistryName());
        setTranslationKey(block.getTranslationKey());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addTooltips(stack, tooltip);
    }
}
