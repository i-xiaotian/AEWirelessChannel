package com.xiaotian.ae.wirelesscable.item;

import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWirelessKeyCard extends Item {
    public ItemWirelessKeyCard(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof WirelessBusBlock bus)) return InteractionResult.PASS;
        return bus.useConnectionCard(context.getLevel(), context.getClickedPos(), context.getPlayer(), context.getHand());
    }
}
