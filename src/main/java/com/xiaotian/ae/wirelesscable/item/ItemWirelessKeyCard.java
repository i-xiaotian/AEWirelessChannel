package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

public class ItemWirelessKeyCard extends ItemBase {

    public ItemWirelessKeyCard() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addTooltips(final ItemStack stack, final List<ITextComponent> tooltips) {
        tooltips.add(new StringTextComponent(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.output")));
        tooltips.add(new StringTextComponent(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.input")));

        final CompoundNBT tagCompound = stack.getTag();
        if (Objects.isNull(tagCompound)) return;
        if (!tagCompound.contains("outputBusBound")) {
            tooltips.add(new StringTextComponent(TextFormatting.RED + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.not_bound")));
            return;
        }

        final CompoundNBT outputBusBound = tagCompound.getCompound("outputBusBound");
        final int boundX = outputBusBound.getInt("boundX");
        final int boundY = outputBusBound.getInt("boundY");
        final int boundZ = outputBusBound.getInt("boundZ");
        final String dimension = outputBusBound.getString("dimension");
        tooltips.add(new StringTextComponent(I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.bound_info", boundX, boundY, boundZ, dimension)));
    }
}
