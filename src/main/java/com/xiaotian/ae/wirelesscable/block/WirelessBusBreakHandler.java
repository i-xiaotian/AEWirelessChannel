package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class WirelessBusBreakHandler {
    private WirelessBusBreakHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof ItemWirelessKeyCard
                || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof WirelessBusBlock bus)
                || !bus.isSafeDismantle(event.getEntity(), event.getItemStack())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel() instanceof ServerLevel level) bus.safeDismantle(level, event.getPos());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getState().getBlock() instanceof WirelessBusBlock bus)
                || !(event.getLevel() instanceof ServerLevel level)
                || event.getPlayer().getAbilities().instabuild
                || !event.getState().canHarvestBlock(level, event.getPos(), event.getPlayer())
                || !bus.isSafeDismantle(event.getPlayer(), event.getPlayer().getMainHandItem())) {
            return;
        }
        bus.captureSafeDrop(level, event.getPos());
    }
}
