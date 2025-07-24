package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public interface IHasTooltips {
    void addTooltips(@Nonnull ItemStack stack, @Nonnull final List<ITextComponent> tooltips);
}
