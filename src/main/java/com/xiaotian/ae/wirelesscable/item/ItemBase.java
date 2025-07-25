package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class ItemBase extends Item implements IHasTooltips {

    public ItemBase(Properties properties) {
        super(properties.tab(AEWirelessChannel.wirelessItemGroup));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        addTooltips(stack, tooltip);
    }

}
