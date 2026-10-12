package com.xiaotian.ae.wirelesscable.client;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import com.xiaotian.ae.wirelesscable.util.ChannelLimitUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TooltipHandler {
    private TooltipHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.getItem() instanceof ItemWirelessKeyCard) {
            tooltip.add(Component.translatable("tooltip.aewirelesschannel.item_wireless_key_card.output"));
            tooltip.add(Component.translatable("tooltip.aewirelesschannel.item_wireless_key_card.input"));
            tooltip.add(Component.translatable("tooltip.aewirelesschannel.item_wireless_key_card.clear"));
            CompoundTag bound = stack.getTagElement(WirelessBusBlock.OUTPUT_BOUND_TAG);
            if (bound == null || !bound.hasUUID(WirelessBusBlock.OUTPUT_ID_TAG)) {
                tooltip.add(Component.translatable("tooltip.aewirelesschannel.item_wireless_key_card.not_bound"));
            } else {
                var pos = net.minecraft.core.BlockPos.of(bound.getLong(WirelessBusBlock.OUTPUT_POS_TAG));
                tooltip.add(Component.translatable("tooltip.aewirelesschannel.item_wireless_key_card.bound_info",
                        pos.getX(), pos.getY(), pos.getZ(), bound.getString(WirelessBusBlock.DIMENSION_TAG)));
            }
            return;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof WirelessBusBlock bus)) return;

        tooltip.add(Component.translatable("tooltip.aewirelesschannel.tile_wireless_bus.info"));
        List<String> tools = ModConfig.dismantlingToolNames();
        String toolNames = tools.isEmpty() ? Component.translatable(
                "tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools_none").getString()
                : tools.stream().collect(Collectors.joining(", "));
        tooltip.add(Component.translatable(ModConfig.REQUIRE_DISMANTLING_TOOL.get()
                        ? "tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools"
                        : "tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools_optional", toolNames));
        tooltip.add(Component.translatable("tooltip.aewirelesschannel.channel_limit",
                ChannelLimitUtils.getLimit(bus.isDense(), null)));
    }
}
