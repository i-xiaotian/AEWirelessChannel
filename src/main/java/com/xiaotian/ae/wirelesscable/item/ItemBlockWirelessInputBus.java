package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.util.TooltipUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockWirelessInputBus extends ItemBlockBaseWirelessBus {

    private final static String I18N_KEY_PREFIX = "tooltip.aewirelesschannel.tile_wireless_bus_input.info.";

    public ItemBlockWirelessInputBus(Block block) {
        super(block);
    }

    @Override
    public void addTooltips(@Nonnull final ItemStack stack, @Nonnull final List<String> tooltips) {
        super.addTooltips(stack, tooltips);
        TooltipUtils.addMultiLine(tooltips, I18N_KEY_PREFIX);
    }

}
