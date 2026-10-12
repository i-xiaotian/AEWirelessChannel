package com.xiaotian.ae.wirelesscable.block;

import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import com.xiaotian.ae.wirelesscable.config.ModConfig;
import com.xiaotian.ae.wirelesscable.item.ItemWirelessKeyCard;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class WirelessBusBlock extends BaseEntityBlock {
    public static final String OUTPUT_BOUND_TAG = "outputBusBound";
    public static final String OUTPUT_ID_TAG = "outputBusId";
    public static final String OUTPUT_POS_TAG = "outputBusPos";
    public static final String DIMENSION_TAG = "dimension";
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String PORTABLE_CONNECTION_TAG = "aewirelesschannel:portableConnection";

    private final boolean output;
    private final boolean dense;
    private final Set<BlockPos> preservingLinksOnRemoval = new HashSet<>();
    private final Map<ServerLevel, Map<BlockPos, PendingSafeDrop>> pendingSafeDrops = new WeakHashMap<>();

    public WirelessBusBlock(boolean output, boolean dense, Properties properties) {
        super(properties);
        this.output = output;
        this.dense = dense;
        registerDefaultState(defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH)
                .setValue(BlockStateProperties.POWERED, false));
    }

    public boolean isOutput() {
        return output;
    }

    public boolean isDense() {
        return dense;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double inset = dense ? 0.125 : 0.25;
        AABB base = new AABB(inset, 0.0, inset, 1.0 - inset, 0.875, 1.0 - inset);
        return Shapes.create(rotateBox(base, state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WirelessBusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type != entityType()) return null;
        return (world, pos, blockState, blockEntity) -> WirelessBusBlockEntity.serverTick(
                world, pos, blockState, (WirelessBusBlockEntity) blockEntity);
    }

    private BlockEntityType<WirelessBusBlockEntity> entityType() {
        if (output) return dense ? ModContent.DENSE_WIRELESS_OUTPUT_BUS_ENTITY.get() : ModContent.WIRELESS_OUTPUT_BUS_ENTITY.get();
        return dense ? ModContent.DENSE_WIRELESS_INPUT_BUS_ENTITY.get() : ModContent.WIRELESS_INPUT_BUS_ENTITY.get();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof WirelessBusBlockEntity bus) {
            if (placer instanceof Player player) bus.setOwner(player);
            if (!level.isClientSide) {
                CompoundTag portableData = stack.getTagElement(PORTABLE_CONNECTION_TAG);
                if (portableData != null) bus.loadPortableConnectionData(portableData);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ItemWirelessKeyCard) {
            return useConnectionCard(level, pos, player, hand);
        }
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        ItemStack tool = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!ModConfig.isDismantlingTool(tool)) tool = player.getItemInHand(otherHand);
        if (ModConfig.REQUIRE_DISMANTLING_TOOL.get() && !ModConfig.isDismantlingTool(tool)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        safeDismantle((ServerLevel) level, pos);
        return InteractionResult.CONSUME;
    }

    public InteractionResult useConnectionCard(Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        BlockEntity raw = level.getBlockEntity(pos);
        if (!(raw instanceof WirelessBusBlockEntity bus)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (!bus.mayConfigure(player)) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "message.aewirelesschannel.no_permission"), true);
                } else if (output) {
                    int count = bus.clearOutputConnections();
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "message.aewirelesschannel.connection_card_disconnected_output", count), true);
                } else {
                    bus.clearInputConnection();
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "message.aewirelesschannel.cleared_input_bus"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (output) {
            if (level.isClientSide) {
                WirelessLinkNetwork.select(pos, hand, null);
            } else {
                CompoundTag root = card.getOrCreateTag();
                CompoundTag bound = new CompoundTag();
                bound.putUUID(OUTPUT_ID_TAG, bus.getOutputBusId());
                bound.putLong(OUTPUT_POS_TAG, pos.asLong());
                bound.putString(DIMENSION_TAG, level.dimension().location().toString());
                root.put(OUTPUT_BOUND_TAG, bound);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.aewirelesschannel.bound_out_put_bus"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            CompoundTag bound = card.getTagElement(OUTPUT_BOUND_TAG);
            if (bound != null && bound.hasUUID(OUTPUT_ID_TAG)) {
                WirelessLinkNetwork.select(BlockPos.of(bound.getLong(OUTPUT_POS_TAG)), hand,
                        bound.getUUID(OUTPUT_ID_TAG));
            }
            return InteractionResult.SUCCESS;
        }

        CompoundTag bound = card.getTagElement(OUTPUT_BOUND_TAG);
        if (bound == null || !bound.hasUUID(OUTPUT_ID_TAG)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.aewirelesschannel.not_bound"), true);
            return InteractionResult.FAIL;
        }
        String boundDimension = bound.getString(DIMENSION_TAG);
        if (!boundDimension.equals(level.dimension().location().toString())) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.aewirelesschannel.not_bound"), true);
            return InteractionResult.FAIL;
        }
        BlockPos outputPos = BlockPos.of(bound.getLong(OUTPUT_POS_TAG));
        WirelessBusBlockEntity outputBus = WirelessBusBlockEntity.findLoadedOutput((ServerLevel) level,
                bound.getUUID(OUTPUT_ID_TAG));
        if (outputBus == null && level.hasChunkAt(outputPos)) {
            BlockEntity outputEntity = level.getBlockEntity(outputPos);
            if (outputEntity instanceof WirelessBusBlockEntity candidate && candidate.isOutput()) outputBus = candidate;
        }
        if (outputBus == null || !outputBus.isOutput()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.aewirelesschannel.bound_type_error"), true);
            return InteractionResult.FAIL;
        }
        if (!bus.mayConfigure(player) || !outputBus.mayConfigure(player)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.aewirelesschannel.no_permission"), true);
            return InteractionResult.FAIL;
        }
        bus.connectTo(outputBus);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.aewirelesschannel.bound_in_put_bus"), true);
        return InteractionResult.CONSUME;
    }

    public void safeDismantle(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack drop = new ItemStack(this);
        if (blockEntity instanceof WirelessBusBlockEntity bus) {
            blockEntity.saveToItem(drop);
            writePortableData(drop, bus.savePortableConnectionData());
        }

        BlockPos immutablePos = pos.immutable();
        preservingLinksOnRemoval.add(immutablePos);
        try {
            level.removeBlock(pos, false);
        } finally {
            preservingLinksOnRemoval.remove(immutablePos);
        }
        ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, drop);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    public boolean isSafeDismantle(Player player, ItemStack tool) {
        return player.isShiftKeyDown() && (!ModConfig.REQUIRE_DISMANTLING_TOOL.get()
                || ModConfig.isDismantlingTool(tool) || ModConfig.isDismantlingTool(player.getOffhandItem()));
    }

    public void captureSafeDrop(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof WirelessBusBlockEntity bus)) return;
        BlockPos key = pos.immutable();
        pendingSafeDrops.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(key, new PendingSafeDrop(bus.savePortableConnectionData(), level.getGameTime()));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        PendingSafeDrop pending = takePendingSafeDrop(level, pos);
        if (pending == null) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
            return;
        }

        ItemStack drop = new ItemStack(this);
        if (blockEntity instanceof WirelessBusBlockEntity) blockEntity.saveToItem(drop);
        writePortableData(drop, pending.connectionData());
        ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, drop);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
        level.levelEvent(2001, pos, Block.getId(state));
    }

    private static void writePortableData(ItemStack drop, CompoundTag portableData) {
        drop.getOrCreateTagElement(BLOCK_ENTITY_TAG).merge(portableData.copy());
        drop.getOrCreateTag().put(PORTABLE_CONNECTION_TAG, portableData.copy());
    }

    @Nullable
    private PendingSafeDrop takePendingSafeDrop(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        Map<BlockPos, PendingSafeDrop> drops = pendingSafeDrops.get(serverLevel);
        if (drops == null) return null;
        PendingSafeDrop pending = drops.remove(pos);
        if (drops.isEmpty()) pendingSafeDrops.remove(serverLevel);
        if (pending == null || serverLevel.getGameTime() - pending.capturedAt() > 1) return null;
        return pending;
    }

    private boolean hasPendingSafeDrop(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        Map<BlockPos, PendingSafeDrop> drops = pendingSafeDrops.get(serverLevel);
        if (drops == null) return false;
        PendingSafeDrop pending = drops.get(pos);
        if (pending == null) return false;
        if (serverLevel.getGameTime() - pending.capturedAt() <= 1) return true;
        drops.remove(pos);
        if (drops.isEmpty()) pendingSafeDrops.remove(serverLevel);
        return false;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !level.isClientSide) {
            if (!preservingLinksOnRemoval.contains(pos) && !hasPendingSafeDrop(level, pos)
                    && level.getBlockEntity(pos) instanceof WirelessBusBlockEntity bus) {
                if (output) bus.clearOutputConnections();
                else bus.clearInputConnection();
            }
            NetworkChunkLoading.unregister((ServerLevel) level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private record PendingSafeDrop(CompoundTag connectionData, long capturedAt) {
    }

    private static AABB rotateBox(AABB box, Direction facing) {
        return switch (facing) {
            case UP -> box;
            case DOWN -> new AABB(box.minX, 1 - box.maxY, box.minZ,
                    box.maxX, 1 - box.minY, box.maxZ);
            case NORTH -> new AABB(box.minX, box.minZ, 1 - box.maxY,
                    box.maxX, box.maxZ, 1 - box.minY);
            case SOUTH -> new AABB(1 - box.maxX, box.minZ, box.minY,
                    1 - box.minX, box.maxZ, box.maxY);
            case EAST -> new AABB(box.maxY, 1 - box.maxX, box.minZ,
                    box.minY, 1 - box.minX, box.maxZ);
            case WEST -> new AABB(1 - box.maxY, 1 - box.maxX, box.minZ,
                    1 - box.minY, 1 - box.minX, box.maxZ);
        };
    }
}
