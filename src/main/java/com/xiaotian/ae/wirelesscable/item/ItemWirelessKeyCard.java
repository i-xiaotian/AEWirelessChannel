package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.registry.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.output"));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.input"));

        final NBTTagCompound tagCompound = stack.getTagCompound();
        if (Objects.isNull(tagCompound)) return;
        if (!tagCompound.hasKey("outputBusBound")) {
            tooltip.add(TextFormatting.RED + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.not_bound"));
            return;
        }

        if (Objects.isNull(worldIn)) return;
        final NBTTagCompound outputBusBound = tagCompound.getCompoundTag("outputBusBound");
        final int boundX = outputBusBound.getInteger("boundX");
        final int boundY = outputBusBound.getInteger("boundY");
        final int boundZ = outputBusBound.getInteger("boundZ");
        final IBlockState blockState = worldIn.getBlockState(new BlockPos(boundX, boundY, boundZ));
        final Block block = blockState.getBlock();
        if (block != Blocks.WIRELESS_OUTPUT_BUS) {
            tooltip.add(TextFormatting.RED + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.bound_type_error"));
            return;
        }
        tooltip.add(I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.bound_info", boundX, boundY, boundZ));
    }
}
