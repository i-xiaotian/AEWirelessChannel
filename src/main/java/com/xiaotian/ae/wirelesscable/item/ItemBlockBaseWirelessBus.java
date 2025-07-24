package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.util.TooltipUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ItemBlockBaseWirelessBus extends ItemBlockBase {

    private final static String I18N_KEY_PREFIX = "tooltip.aewirelesschannel.tile_wireless_bus.info.";

    public ItemBlockBaseWirelessBus(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void addTooltips(@Nonnull final ItemStack stack, @Nonnull final List<ITextComponent> tooltips) {
        TooltipUtils.addMultiLine(tooltips, I18N_KEY_PREFIX);
    }
}
