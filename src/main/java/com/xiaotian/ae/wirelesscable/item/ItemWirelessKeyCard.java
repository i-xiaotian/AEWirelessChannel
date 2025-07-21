package com.xiaotian.ae.wirelesscable.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

public class ItemWirelessKeyCard extends ItemBase {

    public ItemWirelessKeyCard() {
        super();
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemId() {
        return "item_wireless_key_card";
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addTooltips(final ItemStack stack, final List<String> tooltips) {
        tooltips.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.output"));
        tooltips.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.input"));

        final NBTTagCompound tagCompound = stack.getTagCompound();
        if (Objects.isNull(tagCompound)) return;
        if (!tagCompound.hasKey("outputBusBound")) {
            tooltips.add(TextFormatting.RED + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.not_bound"));
            return;
        }

        final NBTTagCompound outputBusBound = tagCompound.getCompoundTag("outputBusBound");
        final int boundX = outputBusBound.getInteger("boundX");
        final int boundY = outputBusBound.getInteger("boundY");
        final int boundZ = outputBusBound.getInteger("boundZ");
        final int dimension = outputBusBound.getInteger("dimension");
        tooltips.add(I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.bound_info", boundX, boundY, boundZ, dimension));
    }
}
