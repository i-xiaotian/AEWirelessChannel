package com.xiaotian.ae.wirelesscable.client;

import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.Items;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public final class WirelessLinkRenderer {

    private static final double BEAM_RADIUS = 0.11;
    private static final double CHEVRON_SPACING = 1.0;
    private static final double CHEVRON_SPEED_BLOCKS_PER_TICK = 1.0 / 20.0;
    private static final int REFRESH_INTERVAL_TICKS = 10;

    private World renderWorld;
    private int refreshTicks;

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null) {
            WirelessLinkNetwork.clearSelection();
            renderWorld = null;
            refreshTicks = 0;
            return;
        }
        if (renderWorld != null && renderWorld != minecraft.world) {
            WirelessLinkNetwork.clearSelection();
            renderWorld = minecraft.world;
            refreshTicks = 0;
        }
        if (!isConnectionCardHeld(minecraft)) {
            refreshTicks = 0;
            return;
        }

        final WirelessLinkNetwork.Selection selection = WirelessLinkNetwork.getSelection();
        if (selection == null) return;
        if (selection.dimension != minecraft.world.provider.getDimension()) {
            WirelessLinkNetwork.clearSelection();
            refreshTicks = 0;
            return;
        }

        if (++refreshTicks >= REFRESH_INTERVAL_TICKS) {
            refreshTicks = 0;
            WirelessLinkNetwork.refreshVisualization();
        }
    }

    @SubscribeEvent
    public void renderLinks(final RenderWorldLastEvent event) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null) {
            renderWorld = null;
            return;
        }
        if (renderWorld != null && renderWorld != minecraft.world) {
            WirelessLinkNetwork.clearSelection();
        }
        renderWorld = minecraft.world;

        if (!isConnectionCardHeld(minecraft)) return;
        final WirelessLinkNetwork.Snapshot snapshot = WirelessLinkNetwork.getSnapshot(
                minecraft.world.provider.getDimension());
        if (snapshot == null || snapshot.links.isEmpty()) return;

        final double cameraX = minecraft.getRenderManager().viewerPosX;
        final double cameraY = minecraft.getRenderManager().viewerPosY;
        final double cameraZ = minecraft.getRenderManager().viewerPosZ;
        final double animationTime = minecraft.world.getTotalWorldTime() + event.getPartialTicks();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        try {
            renderCollisionBoxes(minecraft.world, snapshot, buffer, tessellator,
                    cameraX, cameraY, cameraZ);
            renderStripedBeams(minecraft.world, snapshot, buffer, tessellator,
                    cameraX, cameraY, cameraZ, animationTime);
        } finally {
            GL11.glLineWidth(1.0F);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

    private static boolean isConnectionCardHeld(final Minecraft minecraft) {
        return minecraft.player.getHeldItemMainhand().getItem() == Items.ITEM_WIRELESS_KEY_CARD
                || minecraft.player.getHeldItemOffhand().getItem() == Items.ITEM_WIRELESS_KEY_CARD;
    }

    private static void renderCollisionBoxes(final World world, final WirelessLinkNetwork.Snapshot snapshot,
                                             final BufferBuilder buffer, final Tessellator tessellator,
                                             final double cameraX, final double cameraY, final double cameraZ) {
        GL11.glLineWidth(2.0F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (WirelessLinkNetwork.Link link : snapshot.links) {
            drawCollisionBox(buffer, getCollisionBox(world, link.outputBus),
                    cameraX, cameraY, cameraZ, 1.0F, 0.68F, 0.12F);
            drawCollisionBox(buffer, getCollisionBox(world, link.inputBus),
                    cameraX, cameraY, cameraZ, 0.18F, 0.92F, 0.67F);
        }
        tessellator.draw();
    }

    private static AxisAlignedBB getCollisionBox(final World world, final BlockPos pos) {
        if (!world.isBlockLoaded(pos)) return null;
        final IBlockState state = world.getBlockState(pos);
        final AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
        if (box == null || Math.abs(box.maxX - box.minX) < 1.0E-6
                || Math.abs(box.maxY - box.minY) < 1.0E-6
                || Math.abs(box.maxZ - box.minZ) < 1.0E-6) return null;

        return new AxisAlignedBB(
                Math.min(box.minX, box.maxX), Math.min(box.minY, box.maxY), Math.min(box.minZ, box.maxZ),
                Math.max(box.minX, box.maxX), Math.max(box.minY, box.maxY), Math.max(box.minZ, box.maxZ))
                .offset(pos);
    }

    private static void drawCollisionBox(final BufferBuilder buffer, final AxisAlignedBB box,
                                         final double cameraX, final double cameraY, final double cameraZ,
                                         final float red, final float green, final float blue) {
        if (box == null) return;
        final double minX = box.minX - cameraX;
        final double minY = box.minY - cameraY;
        final double minZ = box.minZ - cameraZ;
        final double maxX = box.maxX - cameraX;
        final double maxY = box.maxY - cameraY;
        final double maxZ = box.maxZ - cameraZ;

        addLine(buffer, minX, minY, minZ, maxX, minY, minZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, 1.0F);
        addLine(buffer, minX, minY, maxZ, minX, minY, minZ, red, green, blue, 1.0F);
        addLine(buffer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, 1.0F);
        addLine(buffer, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, 1.0F);
        addLine(buffer, minX, minY, minZ, minX, maxY, minZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, 1.0F);
        addLine(buffer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, 1.0F);
        addLine(buffer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, 1.0F);
    }

    private static void renderStripedBeams(final World world, final WirelessLinkNetwork.Snapshot snapshot,
                                          final BufferBuilder buffer, final Tessellator tessellator,
                                          final double cameraX, final double cameraY, final double cameraZ,
                                          final double animationTime) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (WirelessLinkNetwork.Link link : snapshot.links) {
            final double[] start = getEndpointCenter(world, link.outputBus, cameraX, cameraY, cameraZ);
            final double[] end = getEndpointCenter(world, link.inputBus, cameraX, cameraY, cameraZ);
            drawStripedBeam(buffer, start, end, animationTime);
        }
        tessellator.draw();
    }

    private static double[] getEndpointCenter(final World world, final BlockPos pos,
                                              final double cameraX, final double cameraY, final double cameraZ) {
        final AxisAlignedBB box = getCollisionBox(world, pos);
        if (box == null) {
            return new double[]{pos.getX() + 0.5 - cameraX,
                    pos.getY() + 0.5 - cameraY, pos.getZ() + 0.5 - cameraZ};
        }
        return new double[]{(box.minX + box.maxX) * 0.5 - cameraX,
                (box.minY + box.maxY) * 0.5 - cameraY,
                (box.minZ + box.maxZ) * 0.5 - cameraZ};
    }

    private static void drawStripedBeam(final BufferBuilder buffer, final double[] start,
                                        final double[] end, final double animationTime) {
        final double dx = end[0] - start[0];
        final double dy = end[1] - start[1];
        final double dz = end[2] - start[2];
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.01) return;

        final double dirX = dx / length;
        final double dirY = dy / length;
        final double dirZ = dz / length;
        final double midX = (start[0] + end[0]) * 0.5;
        final double midY = (start[1] + end[1]) * 0.5;
        final double midZ = (start[2] + end[2]) * 0.5;
        final double[] sideA = normalize(cross(dirX, dirY, dirZ, -midX, -midY, -midZ));
        if (sideA == null) {
            final double[] fallback = normalize(cross(dirX, dirY, dirZ, 0.0, 1.0, 0.0));
            if (fallback != null) {
                drawRibbonPlane(buffer, start, dirX, dirY, dirZ, length, fallback, animationTime);
                return;
            }
            final double[] horizontalFallback = normalize(cross(dirX, dirY, dirZ, 1.0, 0.0, 0.0));
            if (horizontalFallback == null) return;
            drawRibbonPlane(buffer, start, dirX, dirY, dirZ, length, horizontalFallback, animationTime);
            return;
        }

        drawRibbonPlane(buffer, start, dirX, dirY, dirZ, length, sideA, animationTime);
    }

    private static void drawRibbonPlane(final BufferBuilder buffer, final double[] start,
                                        final double dirX, final double dirY, final double dirZ,
                                        final double length, final double[] side, final double animationTime) {
        addQuad(buffer,
                point(start, dirX, dirY, dirZ, side, 0.0, -BEAM_RADIUS),
                point(start, dirX, dirY, dirZ, side, length, -BEAM_RADIUS),
                point(start, dirX, dirY, dirZ, side, length, BEAM_RADIUS),
                point(start, dirX, dirY, dirZ, side, 0.0, BEAM_RADIUS),
                0.16F, 0.38F, 0.72F, 0.96F);

        double phase = animationTime * CHEVRON_SPEED_BLOCKS_PER_TICK % CHEVRON_SPACING;
        if (phase < 0.0) phase += CHEVRON_SPACING;
        for (double distance = 0.28 + phase; distance < length; distance += CHEVRON_SPACING) {
            if (distance < 0.20 || distance + 0.20 > length) continue;
            final double[] center = point(start, dirX, dirY, dirZ, side, distance, 0.0);
            drawChevron(buffer, center, dirX, dirY, dirZ, side);
        }
    }

    private static void drawChevron(final BufferBuilder buffer, final double[] center,
                                    final double dirX, final double dirY, final double dirZ,
                                    final double[] side) {
        drawArrowGlyph(buffer, center, dirX, dirY, dirZ, side,
                0.19, BEAM_RADIUS * 0.84, 0.045, 1.0F);
    }

    private static void drawArrowGlyph(final BufferBuilder buffer, final double[] center,
                                       final double dirX, final double dirY, final double dirZ,
                                       final double[] side, final double halfLength,
                                       final double halfWidth, final double thickness, final float alpha) {
        final double[] tip = point(center, dirX, dirY, dirZ, side, halfLength, 0.0);
        final double[] innerTip = point(center, dirX, dirY, dirZ, side,
                halfLength - thickness * 1.5, 0.0);

        addQuad(buffer,
                point(center, dirX, dirY, dirZ, side, -halfLength, halfWidth), tip, innerTip,
                point(center, dirX, dirY, dirZ, side, -halfLength, halfWidth - thickness),
                1.0F, 0.88F, 0.08F, alpha);
        addQuad(buffer,
                point(center, dirX, dirY, dirZ, side, -halfLength, -halfWidth),
                point(center, dirX, dirY, dirZ, side, -halfLength, -halfWidth + thickness),
                innerTip, tip, 1.0F, 0.88F, 0.08F, alpha);
    }

    private static double[] point(final double[] origin,
                                  final double dirX, final double dirY, final double dirZ,
                                  final double[] side, final double along, final double across) {
        return new double[]{origin[0] + dirX * along + side[0] * across,
                origin[1] + dirY * along + side[1] * across,
                origin[2] + dirZ * along + side[2] * across};
    }

    private static double[] cross(final double ax, final double ay, final double az,
                                  final double bx, final double by, final double bz) {
        return new double[]{ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx};
    }

    private static double[] normalize(final double[] vector) {
        final double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        if (length < 1.0E-6) return null;
        return new double[]{vector[0] / length, vector[1] / length, vector[2] / length};
    }

    private static void addQuad(final BufferBuilder buffer,
                                final double[] a, final double[] b, final double[] c, final double[] d,
                                final float red, final float green, final float blue, final float alpha) {
        addVertex(buffer, a, red, green, blue, alpha);
        addVertex(buffer, b, red, green, blue, alpha);
        addVertex(buffer, c, red, green, blue, alpha);
        addVertex(buffer, d, red, green, blue, alpha);
    }

    private static void addVertex(final BufferBuilder buffer, final double[] point,
                                  final float red, final float green, final float blue, final float alpha) {
        buffer.pos(point[0], point[1], point[2]).color(red, green, blue, alpha).endVertex();
    }

    private static void addLine(final BufferBuilder buffer,
                                final double startX, final double startY, final double startZ,
                                final double endX, final double endY, final double endZ,
                                final float red, final float green, final float blue, final float alpha) {
        buffer.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
    }
}
