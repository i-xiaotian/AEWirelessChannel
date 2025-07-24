package com.xiaotian.ae.wirelesscable.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockBaseWirelessBus extends BlockBaseBus {

    private static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private static final Tags.IOptionalNamedTag<Item> WRENCH_TAG = ItemTags.createOptional(new ResourceLocation("appliedenergistics2", "quartz_wrench"));

    public BlockBaseWirelessBus(final AbstractBlock.Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getFace());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            Block block = world.getBlockState(pos).getBlock();
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;

            if (block instanceof ITileWithWireless) {
                ITileWithWireless iTileWithWireless = (ITileWithWireless) tileEntity;
                if (iTileWithWireless.needInitTagFromItemStack()) {
                    CompoundNBT tag = stack.getTag();
                    if (tag != null) {
                        iTileWithWireless.initAEConnectionFromItemStackTag(tag, tileEntity);
                    }
                }
            }

            if (tileEntity instanceof TileWirelessBus && placer instanceof PlayerEntity) {
                TileWirelessBus tileWirelessBus = (TileWirelessBus) tileEntity;
                AENetworkProxy proxy = tileWirelessBus.getProxy();
                proxy.setOwner((PlayerEntity) placer);
                proxy.setValidSides(EnumSet.of(state.get(FACING).getOpposite()));
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockRayTraceResult hit) {

        if (actionWithConnectionCard(world, pos, player, hand)) return ActionResultType.SUCCESS;
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.isEmpty()) return ActionResultType.PASS;
        final Item item = heldItem.getItem();
        if (!item.isIn(WRENCH_TAG)) return ActionResultType.PASS;

        if (world.isRemote) return ActionResultType.SUCCESS;
        if (!player.isSneaking()) return ActionResultType.FAIL;

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return ActionResultType.FAIL;

        CompoundNBT nbt = tile.write(new CompoundNBT());
        ItemStack itemStack = new ItemStack(tile.getBlockState().getBlock());
        itemStack.setTag(nbt);

        world.removeBlock(pos, false);
        ItemEntity entityItem = new ItemEntity(world,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                itemStack);
        entityItem.setDefaultPickupDelay();
        world.addEntity(entityItem);
        return ActionResultType.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.rotateAABB(state.get(FACING));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    protected abstract boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final PlayerEntity playerIn, final Hand hand);

}
