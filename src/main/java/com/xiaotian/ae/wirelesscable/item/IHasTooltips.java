package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IHasTooltips {
    void addTooltips(@Nonnull ItemStack stack, @Nonnull final List<String> tooltips);
}
