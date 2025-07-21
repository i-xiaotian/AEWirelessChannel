package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.util.TooltipUtils;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockDenseWirelessInputBus extends ItemBlockBaseWirelessBus {

    private final static String I18N_KEY_PREFIX = "tooltip.aewirelesschannel.tile_dense_wireless_bus_input.info.";

    public ItemBlockDenseWirelessInputBus(Block block) {
        super(block);
    }

    @Override
    public void addTooltips(@Nonnull final ItemStack stack, @Nonnull final List<String> tooltips) {
        super.addTooltips(stack, tooltips);
        TooltipUtils.addMultiLine(tooltips, I18N_KEY_PREFIX);
    }
}
