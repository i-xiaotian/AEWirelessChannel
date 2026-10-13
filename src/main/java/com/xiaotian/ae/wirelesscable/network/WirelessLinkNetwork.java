package com.xiaotian.ae.wirelesscable.network;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class WirelessLinkNetwork {
    private static final String PROTOCOL = "2";
    private static final int MAX_LINKS = 128;
    private static final AtomicInteger REQUEST_IDS = new AtomicInteger();
    private static final Map<UUID, UUID> AUTHORIZED_OUTPUTS = new WeakHashMap<>();
    private static volatile int latestRequestId;
    private static volatile Selection selection;
    private static volatile Snapshot snapshot;

    private WirelessLinkNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL);
        registrar.playToServer(Request.TYPE, Request.STREAM_CODEC,
                (message, context) -> {
                    if (context.player() instanceof ServerPlayer player) respond(player, message);
                });
        registrar.playToClient(Response.TYPE, Response.STREAM_CODEC,
                (message, context) -> handleResponse(message));
    }

    public static void select(BlockPos pos, InteractionHand hand, UUID outputId) {
        Selection current = new Selection(pos, hand, outputId);
        selection = current;
        snapshot = null;
        send(current);
    }

    public static void refresh() {
        Selection current = selection;
        if (current != null) send(current);
    }

    private static void send(Selection current) {
        int requestId = REQUEST_IDS.incrementAndGet();
        latestRequestId = requestId;
        PacketDistributor.sendToServer(new Request(requestId, current.pos, current.hand, current.outputId));
    }

    public static Selection getSelection() {
        return selection;
    }

    public static Snapshot getSnapshot(ResourceLocation dimension) {
        Snapshot current = snapshot;
        return current != null && current.dimension.equals(dimension) ? current : null;
    }

    public static void clearSnapshot() {
        snapshot = null;
    }

    private static void respond(ServerPlayer player, Request request) {
        ItemStack held = player.getItemInHand(request.hand);
        if (held.isEmpty() || held.getItem() != ModContent.WIRELESS_KEY_CARD.get()) return;
        Level level = player.level();
        WirelessBusBlockEntity output = null;
        if (request.outputId != null) {
            UUID boundId = getBoundOutputId(held);
            UUID authorized = AUTHORIZED_OUTPUTS.get(player.getUUID());
            if (!request.outputId.equals(boundId) && !request.outputId.equals(authorized)) return;
            output = WirelessBusBlockEntity.findLoadedOutput((net.minecraft.server.level.ServerLevel) level, request.outputId);
            if (output == null && level.hasChunkAt(request.pos)) {
                BlockEntity entity = level.getBlockEntity(request.pos);
                if (entity instanceof WirelessBusBlockEntity bus && bus.isOutput()
                        && request.outputId.equals(bus.getOutputBusId())) output = bus;
            }
        } else if (level.hasChunkAt(request.pos)) {
            BlockEntity entity = level.getBlockEntity(request.pos);
            if (entity instanceof WirelessBusBlockEntity bus && bus.isOutput()
                    && player.distanceToSqr(request.pos.getX() + 0.5, request.pos.getY() + 0.5,
                    request.pos.getZ() + 0.5) <= 64.0) {
                output = bus;
                AUTHORIZED_OUTPUTS.put(player.getUUID(), bus.getOutputBusId());
            }
        }

        if (output == null) {
            sendResponse(player, new Response(request.id, level.dimension().location(), null, null, List.of()));
            return;
        }

        List<Link> links = new ArrayList<>();
        for (ConnectionInfo info : output.getOutputConnections()) {
            if (links.size() >= MAX_LINKS) break;
            links.add(new Link(output.getBlockPos(), info.inputPos()));
        }
        if (links.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.aewirelesschannel.connection_card_no_connections"), true);
        }
        sendResponse(player, new Response(request.id, level.dimension().location(), output.getOutputBusId(),
                output.getBlockPos(), links));
    }

    private static UUID getBoundOutputId(ItemStack stack) {
        var bound = com.xiaotian.ae.wirelesscable.util.ItemConnectionData.get(
                stack, WirelessBusBlock.OUTPUT_BOUND_TAG);
        return bound != null && bound.hasUUID(WirelessBusBlock.OUTPUT_ID_TAG)
                ? bound.getUUID(WirelessBusBlock.OUTPUT_ID_TAG) : null;
    }

    private static void sendResponse(ServerPlayer player, Response response) {
        PacketDistributor.sendToPlayer(player, response);
    }

    private static void handleResponse(Response response) {
        if (response.id != latestRequestId) return;
        Selection current = selection;
        if (current != null && response.outputId != null) current.outputId = response.outputId;
        snapshot = response.outputPos == null ? null
                : new Snapshot(response.dimension, response.outputPos, response.links);
    }

    public static final class Link {
        public final BlockPos output;
        public final BlockPos input;

        private Link(BlockPos output, BlockPos input) {
            this.output = output.immutable();
            this.input = input.immutable();
        }
    }

    public static final class Snapshot {
        public final ResourceLocation dimension;
        public final BlockPos output;
        public final List<Link> links;

        private Snapshot(ResourceLocation dimension, BlockPos output, List<Link> links) {
            this.dimension = dimension;
            this.output = output.immutable();
            this.links = Collections.unmodifiableList(new ArrayList<>(links));
        }
    }

    public static final class Selection {
        public final BlockPos pos;
        public final InteractionHand hand;
        private UUID outputId;

        private Selection(BlockPos pos, InteractionHand hand, UUID outputId) {
            this.pos = pos.immutable();
            this.hand = hand;
            this.outputId = outputId;
        }
    }

    private static final class Request implements CustomPacketPayload {
        private static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AEWirelessChannel.MOD_ID, "request"));
        private static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC =
                StreamCodec.of(Request::encode, Request::decode);
        private final int id;
        private final BlockPos pos;
        private final InteractionHand hand;
        private final UUID outputId;

        private Request(int id, BlockPos pos, InteractionHand hand, UUID outputId) {
            this.id = id;
            this.pos = pos;
            this.hand = hand;
            this.outputId = outputId;
        }

        @Override
        public Type<Request> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buffer, Request message) {
            buffer.writeInt(message.id);
            buffer.writeBlockPos(message.pos);
            buffer.writeByte(message.hand.ordinal());
            buffer.writeBoolean(message.outputId != null);
            if (message.outputId != null) buffer.writeUUID(message.outputId);
        }

        private static Request decode(RegistryFriendlyByteBuf buffer) {
            int id = buffer.readInt();
            BlockPos pos = buffer.readBlockPos();
            int handIndex = buffer.readUnsignedByte();
            InteractionHand hand = handIndex == InteractionHand.OFF_HAND.ordinal()
                    ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            UUID outputId = buffer.readBoolean() ? buffer.readUUID() : null;
            return new Request(id, pos, hand, outputId);
        }
    }

    private static final class Response implements CustomPacketPayload {
        private static final Type<Response> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AEWirelessChannel.MOD_ID, "response"));
        private static final StreamCodec<RegistryFriendlyByteBuf, Response> STREAM_CODEC =
                StreamCodec.of(Response::encode, Response::decode);
        private final int id;
        private final ResourceLocation dimension;
        private final UUID outputId;
        private final BlockPos outputPos;
        private final List<Link> links;

        private Response(int id, ResourceLocation dimension, UUID outputId, BlockPos outputPos, List<Link> links) {
            this.id = id;
            this.dimension = dimension;
            this.outputId = outputId;
            this.outputPos = outputPos == null ? null : outputPos.immutable();
            this.links = List.copyOf(links);
        }

        @Override
        public Type<Response> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buffer, Response message) {
            buffer.writeInt(message.id);
            buffer.writeResourceLocation(message.dimension);
            buffer.writeBoolean(message.outputPos != null);
            if (message.outputPos != null) buffer.writeBlockPos(message.outputPos);
            buffer.writeBoolean(message.outputId != null);
            if (message.outputId != null) buffer.writeUUID(message.outputId);
            int count = Math.min(message.links.size(), MAX_LINKS);
            buffer.writeVarInt(count);
            for (int i = 0; i < count; i++) {
                buffer.writeBlockPos(message.links.get(i).output);
                buffer.writeBlockPos(message.links.get(i).input);
            }
        }

        private static Response decode(RegistryFriendlyByteBuf buffer) {
            int id = buffer.readInt();
            ResourceLocation dimension = buffer.readResourceLocation();
            BlockPos outputPos = buffer.readBoolean() ? buffer.readBlockPos() : null;
            UUID outputId = buffer.readBoolean() ? buffer.readUUID() : null;
            int count = Math.min(buffer.readVarInt(), MAX_LINKS);
            List<Link> links = new ArrayList<>(count);
            for (int i = 0; i < count; i++) links.add(new Link(buffer.readBlockPos(), buffer.readBlockPos()));
            return new Response(id, dimension, outputId, outputPos, links);
        }
    }
}
