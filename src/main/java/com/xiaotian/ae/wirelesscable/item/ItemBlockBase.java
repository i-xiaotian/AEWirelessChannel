package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

public abstract class ItemBlockBase extends BlockItem implements IHasTooltips {

    public ItemBlockBase(Block block, Properties properties) {
        super(block, properties.tab(AEWirelessChannel.wirelessItemGroup));
        final ResourceLocation registryName = block.getRegistryName();
        if (Objects.nonNull(registryName)) setRegistryName(registryName);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        addTooltips(stack, tooltip);
    }
}
