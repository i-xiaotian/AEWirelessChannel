package com.xiaotian.ae.wirelesscable.block;

import appeng.me.helpers.AENetworkProxy;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import mcp.MethodsReturnNonnullByDefault;
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
import java.util.Objects;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockBaseWirelessBus extends BlockBaseBus {

    private static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");



    public BlockBaseWirelessBus(final Properties properties) {
        super(properties);
        this.defaultBlockState().setValue(FACING, Direction.NORTH)
                .setValue(POWERED, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace())
                .setValue(POWERED, Boolean.FALSE);
    }

    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide) {
            Block block = world.getBlockState(pos).getBlock();
            TileEntity tileEntity = world.getBlockEntity(pos);
            if (Objects.isNull(tileEntity)) return;

            if (block instanceof ITileWithWireless) {
                ITileWithWireless iTileWithWireless = (ITileWithWireless) block;
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
                proxy.setValidSides(EnumSet.of(state.getValue(FACING).getOpposite()));
            }
        }
    }


    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit) {
        if (actionWithConnectionCard(world, pos, player, hand)) return ActionResultType.SUCCESS;
        return super.use(state, world, pos, player, hand, hit);
    }


    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.rotateAABB(state.getValue(FACING));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    protected abstract boolean actionWithConnectionCard(final World worldIn, final BlockPos pos, final PlayerEntity playerIn, final Hand hand);

}
