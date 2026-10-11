package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.block.BlockBaseWirelessBus;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos,
                                           final EnumFacing side, final float hitX, final float hitY,
                                           final float hitZ, final EnumHand hand) {
        if (world.isRemote || !player.isSneaking()) return EnumActionResult.PASS;

        final Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockBaseWirelessBus)) return EnumActionResult.PASS;
        return ((BlockBaseWirelessBus) block).handleConnectionCardAction(world, pos, player, hand)
                ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public void addTooltips(final ItemStack stack, final List<String> tooltips) {
        tooltips.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.output"));
        tooltips.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.input"));
        tooltips.add(TextFormatting.GRAY + I18n.format("tooltip.aewirelesschannel.item_wireless_key_card.disconnect_output"));

        final NBTTagCompound tagCompound = stack.getTagCompound();
        if (Objects.isNull(tagCompound) || !tagCompound.hasKey("outputBusBound")) {
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
