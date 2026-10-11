package com.xiaotian.ae.wirelesscable.network;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.entity.ConnectionInfo;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessInputBus;
import com.xiaotian.ae.wirelesscable.tile.TileWirelessOutputBus;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class WirelessLinkNetwork {

    private static final int MAX_LINKS = 128;
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(AEWirelessChannel.MOD_ID);
    private static final AtomicInteger REQUEST_IDS = new AtomicInteger();
    private static volatile int latestRequestId;
    private static volatile Snapshot snapshot;
    private static volatile Selection selection;
    private static boolean initialized;

    private WirelessLinkNetwork() {
    }

    public static void initialize() {
        if (initialized) return;
        CHANNEL.registerMessage(RequestHandler.class, RequestMessage.class, 0, Side.SERVER);
        CHANNEL.registerMessage(ResponseHandler.class, ResponseMessage.class, 1, Side.CLIENT);
        initialized = true;
    }

    public static void requestVisualization(final BlockPos pos, final EnumHand hand, final int dimension) {
        selection = new Selection(pos, hand, dimension);
        snapshot = null;
        sendRequest(pos, hand, dimension, false, null);
    }

    public static void refreshVisualization() {
        final Selection current = selection;
        if (current != null) sendRequest(current.pos, current.hand, current.dimension, true, current.outputBusId);
    }

    private static void sendRequest(final BlockPos pos, final EnumHand hand, final int dimension,
                                   final boolean silent, final String outputBusId) {
        final int requestId = REQUEST_IDS.incrementAndGet();
        latestRequestId = requestId;
        CHANNEL.sendToServer(new RequestMessage(requestId, pos, hand, dimension, silent, outputBusId));
    }

    public static Selection getSelection() {
        return selection;
    }

    public static Snapshot getSnapshot(final int dimension) {
        final Snapshot current = snapshot;
        if (current == null) return null;
        return current.dimension == dimension ? current : null;
    }

    public static void clearSnapshot() {
        snapshot = null;
    }

    public static void clearSelection() {
        selection = null;
        snapshot = null;
    }

    private static void acceptResponse(final ResponseMessage response) {
        if (response.requestId != latestRequestId) return;
        final Selection currentSelection = selection;
        if (currentSelection != null && response.outputBusId != null) {
            currentSelection.outputBusId = response.outputBusId;
        }
        if (response.links.isEmpty()) {
            snapshot = null;
        } else {
            snapshot = new Snapshot(response.dimension, response.links);
        }
    }

    private static List<Link> findLinks(final World world, final TileWirelessBus bus) {
        final List<Link> links = new ArrayList<>();
        if (bus instanceof TileWirelessInputBus) {
            final ConnectionInfo connection = ((TileWirelessInputBus) bus).getCurrentConnection();
            if (connection.isConnect()) {
                links.add(new Link(
                        new BlockPos(connection.getOutputBusX(), connection.getOutputBusY(), connection.getOutputBusZ()),
                        bus.getPos()));
            }
        } else if (bus instanceof TileWirelessOutputBus) {
            final TileWirelessOutputBus outputBus = (TileWirelessOutputBus) bus;
            for (ConnectionInfo connection : outputBus.getSubGridConnectionMap().values()) {
                if (connection == null || !connection.isConnect()) continue;
                final BlockPos inputPos = outputBus.getConnectionInputBusPosInfo(connection.getConnectionKey());
                if (world.isBlockLoaded(inputPos)) {
                    final TileEntity inputEntity = world.getTileEntity(inputPos);
                    if (!(inputEntity instanceof TileWirelessInputBus)) continue;
                    final ConnectionInfo liveConnection = ((TileWirelessInputBus) inputEntity).getCurrentConnection();
                    if (!liveConnection.isConnect()
                            || !connection.getConnectionKey().equals(liveConnection.getConnectionKey())
                            || liveConnection.getOutputBusX() != bus.getPos().getX()
                            || liveConnection.getOutputBusY() != bus.getPos().getY()
                            || liveConnection.getOutputBusZ() != bus.getPos().getZ()) continue;
                    links.add(new Link(bus.getPos(), inputEntity.getPos()));
                } else {
                    links.add(new Link(bus.getPos(), inputPos));
                }
                if (links.size() >= MAX_LINKS) break;
            }
        }
        return links;
    }

    public static final class Link {
        public final BlockPos outputBus;
        public final BlockPos inputBus;

        private Link(final BlockPos outputBus, final BlockPos inputBus) {
            this.outputBus = outputBus;
            this.inputBus = inputBus;
        }
    }

    public static final class Snapshot {
        public final int dimension;
        public final List<Link> links;

        private Snapshot(final int dimension, final List<Link> links) {
            this.dimension = dimension;
            this.links = Collections.unmodifiableList(new ArrayList<>(links));
        }
    }

    public static final class RequestMessage implements IMessage {
        private int requestId;
        private BlockPos pos;
        private EnumHand hand;
        private int dimension;
        private boolean silent;
        private String outputBusId;

        public RequestMessage() {
        }

        private RequestMessage(final int requestId, final BlockPos pos, final EnumHand hand,
                               final int dimension, final boolean silent, final String outputBusId) {
            this.requestId = requestId;
            this.pos = pos;
            this.hand = hand;
            this.dimension = dimension;
            this.silent = silent;
            this.outputBusId = outputBusId;
        }

        @Override
        public void fromBytes(final ByteBuf buffer) {
            requestId = buffer.readInt();
            pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
            final int handIndex = buffer.readUnsignedByte();
            hand = handIndex < EnumHand.values().length ? EnumHand.values()[handIndex] : EnumHand.MAIN_HAND;
            dimension = buffer.readInt();
            silent = buffer.readBoolean();
            outputBusId = buffer.readBoolean() ? ByteBufUtils.readUTF8String(buffer) : null;
        }

        @Override
        public void toBytes(final ByteBuf buffer) {
            buffer.writeInt(requestId);
            buffer.writeInt(pos.getX());
            buffer.writeInt(pos.getY());
            buffer.writeInt(pos.getZ());
            buffer.writeByte(hand.ordinal());
            buffer.writeInt(dimension);
            buffer.writeBoolean(silent);
            buffer.writeBoolean(outputBusId != null);
            if (outputBusId != null) ByteBufUtils.writeUTF8String(buffer, outputBusId);
        }
    }

    public static final class ResponseMessage implements IMessage {
        private int requestId;
        private int dimension;
        private List<Link> links = Collections.emptyList();
        private String outputBusId;

        public ResponseMessage() {
        }

        private ResponseMessage(final int requestId, final int dimension, final String outputBusId,
                                final List<Link> links) {
            this.requestId = requestId;
            this.dimension = dimension;
            this.outputBusId = outputBusId;
            this.links = links;
        }

        @Override
        public void fromBytes(final ByteBuf buffer) {
            requestId = buffer.readInt();
            dimension = buffer.readInt();
            outputBusId = buffer.readBoolean() ? ByteBufUtils.readUTF8String(buffer) : null;
            final int count = Math.min(buffer.readUnsignedShort(), MAX_LINKS);
            final List<Link> decoded = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                final BlockPos output = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                final BlockPos input = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                decoded.add(new Link(output, input));
            }
            links = decoded;
        }

        @Override
        public void toBytes(final ByteBuf buffer) {
            final int count = Math.min(links.size(), MAX_LINKS);
            buffer.writeInt(requestId);
            buffer.writeInt(dimension);
            buffer.writeBoolean(outputBusId != null);
            if (outputBusId != null) ByteBufUtils.writeUTF8String(buffer, outputBusId);
            buffer.writeShort(count);
            for (int index = 0; index < count; index++) {
                final Link link = links.get(index);
                buffer.writeInt(link.outputBus.getX());
                buffer.writeInt(link.outputBus.getY());
                buffer.writeInt(link.outputBus.getZ());
                buffer.writeInt(link.inputBus.getX());
                buffer.writeInt(link.inputBus.getY());
                buffer.writeInt(link.inputBus.getZ());
            }
        }
    }

    public static final class RequestHandler implements IMessageHandler<RequestMessage, IMessage> {
        @Override
        public IMessage onMessage(final RequestMessage message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            final IThreadListener serverThread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
            serverThread.addScheduledTask(() -> handleRequest(player, message));
            return null;
        }

        private static void handleRequest(final EntityPlayerMP player, final RequestMessage message) {
            final ItemStack held = player.getHeldItem(message.hand);
            if (held.isEmpty() || held.getItem() != Items.ITEM_WIRELESS_KEY_CARD) return;
            if (player.world.provider.getDimension() != message.dimension) return;

            final World world = player.world;
            TileWirelessBus bus = null;
            if (message.outputBusId != null) {
                bus = TileWirelessOutputBus.findLoaded(world, message.outputBusId);
                if (bus == null && world.isBlockLoaded(message.pos)) {
                    final TileEntity selectedTile = world.getTileEntity(message.pos);
                    if (selectedTile instanceof TileWirelessOutputBus
                            && message.outputBusId.equals(((TileWirelessOutputBus) selectedTile).getOutputBusId())) {
                        bus = (TileWirelessOutputBus) selectedTile;
                    }
                }
            } else if (world.isBlockLoaded(message.pos)) {
                final TileEntity tile = world.getTileEntity(message.pos);
                if (tile instanceof TileWirelessBus) bus = (TileWirelessBus) tile;
            }

            if (bus == null) {
                if (message.outputBusId == null && (message.silent || world.isBlockLoaded(message.pos))) {
                    CHANNEL.sendTo(new ResponseMessage(message.requestId, world.provider.getDimension(),
                            null, Collections.emptyList()), player);
                } else if (message.outputBusId != null && world.isBlockLoaded(message.pos)) {
                    CHANNEL.sendTo(new ResponseMessage(message.requestId, world.provider.getDimension(),
                            message.outputBusId, Collections.emptyList()), player);
                }
                return;
            }

            final BlockPos busPos = bus.getPos();
            if (message.outputBusId == null && player.getDistanceSq(busPos.getX() + 0.5, busPos.getY() + 0.5,
                    busPos.getZ() + 0.5) > 64.0) {
                return;
            }

            final List<Link> links = findLinks(world, bus);
            if (links.isEmpty() && !message.silent) {
                player.sendMessage(new TextComponentTranslation(
                        "message.aewirelesschannel.connection_card_no_connections"));
            }
            final String outputBusId = bus instanceof TileWirelessOutputBus
                    ? ((TileWirelessOutputBus) bus).getOutputBusId() : null;
            CHANNEL.sendTo(new ResponseMessage(message.requestId, world.provider.getDimension(),
                    outputBusId, links), player);
        }
    }

    public static final class ResponseHandler implements IMessageHandler<ResponseMessage, IMessage> {
        @Override
        public IMessage onMessage(final ResponseMessage message, final MessageContext context) {
            final IThreadListener clientThread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
            clientThread.addScheduledTask(() -> acceptResponse(message));
            return null;
        }
    }

    public static final class Selection {
        public final BlockPos pos;
        public final EnumHand hand;
        public final int dimension;
        private String outputBusId;

        private Selection(final BlockPos pos, final EnumHand hand, final int dimension) {
            this.pos = pos;
            this.hand = hand;
            this.dimension = dimension;
            this.outputBusId = null;
        }
    }
}
