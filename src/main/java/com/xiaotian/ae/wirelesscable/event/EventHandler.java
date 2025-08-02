package com.xiaotian.ae.wirelesscable.event;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    private static final Tags.IOptionalNamedTag<Item> WRENCH_TAG = ItemTags.createOptional(new ResourceLocation("appliedenergistics2", "quartz_wrench"));

    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final boolean canceled = event.isCanceled();
        if (canceled) return;
        final World world = event.getWorld();
        if (world.isClientSide) return;
        final LivingEntity entityLiving = event.getEntityLiving();
        if (!(entityLiving instanceof PlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) entityLiving;
        if (!player.isCrouching()) return;
        final ItemStack mainHandItem = entityLiving.getItemInHand(Hand.MAIN_HAND);
        if (mainHandItem.isEmpty()) return;
        final Item item = mainHandItem.getItem();
        if (!item.is(WRENCH_TAG)) return;

        final BlockPos pos = event.getPos();
        final TileEntity blockEntity = world.getBlockEntity(pos);
        if (Objects.isNull(blockEntity)) return;
        if (!(blockEntity instanceof TileWirelessBus)) return;
        final TileWirelessBus bus = (TileWirelessBus) blockEntity;
        final IGridNode actionableNode = bus.getActionableNode();
        final IGrid grid = actionableNode.getGrid();
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        if (!(securityGrid.hasPermission(player, SecurityPermissions.BUILD))) return;

        CompoundNBT nbt = bus.save(new CompoundNBT());
        ItemStack itemStack = new ItemStack(bus.getBlockState().getBlock());
        itemStack.setTag(nbt);

        world.removeBlock(pos, false);
        ItemEntity entityItem = new ItemEntity(world,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                itemStack);
        entityItem.setDefaultPickUpDelay();
        world.addFreshEntity(entityItem);

    }

}
