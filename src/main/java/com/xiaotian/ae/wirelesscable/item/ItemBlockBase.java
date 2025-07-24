package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class ItemBlockBase extends BlockItem implements IHasTooltips {

    public ItemBlockBase(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        addTooltips(stack, tooltip);
    }
}
