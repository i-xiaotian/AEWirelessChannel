package com.xiaotian.ae.wirelesscable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import com.xiaotian.ae.wirelesscable.network.WirelessLinkNetwork;
import com.xiaotian.ae.wirelesscable.registry.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = AEWirelessChannel.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WirelessLinkRenderer {
    private static final double BEAM_HALF_WIDTH = 0.065;
    private static final double BOX_LINE_HALF_WIDTH = 0.018;
    private static final double ARROW_SPACING = 1.0;
    private static final double ARROW_SPEED_BLOCKS_PER_SECOND = 1.0;

    private WirelessLinkRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !isHoldingCard(minecraft)) return;
        WirelessLinkNetwork.Snapshot snapshot = WirelessLinkNetwork.getSnapshot(minecraft.level.dimension().location());
        if (snapshot == null) return;

        Level level = minecraft.level;
        Vec3 camera = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        try {
            drawCollisionBoxes(level, snapshot, matrix, camera);
            drawBeams(snapshot.links, level, camera, matrix, event.getRenderTick() + event.getPartialTick());
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    private static boolean isHoldingCard(Minecraft minecraft) {
        return minecraft.player.getMainHandItem().is(ModContent.WIRELESS_KEY_CARD.get())
                || minecraft.player.getOffhandItem().is(ModContent.WIRELESS_KEY_CARD.get());
    }

    private static void drawCollisionBoxes(Level level, WirelessLinkNetwork.Snapshot snapshot,
                                           Matrix4f matrix, Vec3 camera) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawCollisionBox(level, snapshot.output, buffer, matrix, camera, 0.40F, 0.82F, 1.0F);
        for (WirelessLinkNetwork.Link link : snapshot.links) {
            drawCollisionBox(level, link.input, buffer, matrix, camera, 1.0F, 0.76F, 0.24F);
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawCollisionBox(Level level, BlockPos pos, BufferBuilder buffer, Matrix4f matrix,
                                         Vec3 camera, float red, float green, float blue) {
        if (!level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.empty());
        if (shape.isEmpty()) return;
        AABB box = shape.bounds().move(pos);
        double x0 = box.minX, y0 = box.minY, z0 = box.minZ;
        double x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y0, z0), new Vec3(x1, y0, z1), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y0, z1), new Vec3(x0, y0, z1), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y0, z1), new Vec3(x0, y0, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y1, z0), new Vec3(x1, y1, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y1, z0), new Vec3(x1, y1, z1), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y1, z1), new Vec3(x0, y1, z1), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y1, z1), new Vec3(x0, y1, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y0, z0), new Vec3(x0, y1, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y0, z0), new Vec3(x1, y1, z0), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x1, y0, z1), new Vec3(x1, y1, z1), red, green, blue);
        drawBoxEdge(buffer, matrix, camera, new Vec3(x0, y0, z1), new Vec3(x0, y1, z1), red, green, blue);
    }

    private static void drawBoxEdge(BufferBuilder buffer, Matrix4f matrix, Vec3 camera,
                                    Vec3 start, Vec3 end, float red, float green, float blue) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 towardCamera = camera.subtract(start.add(end).scale(0.5)).normalize();
        Vec3 side = direction.cross(towardCamera).normalize();
        if (side.lengthSqr() < 1.0e-5) side = direction.cross(new Vec3(0, 1, 0)).normalize();
        if (side.lengthSqr() < 1.0e-5) side = direction.cross(new Vec3(1, 0, 0)).normalize();
        if (side.lengthSqr() < 1.0e-5) return;
        side = side.scale(BOX_LINE_HALF_WIDTH);
        addQuad(buffer, matrix, start.add(side), end.add(side), end.subtract(side), start.subtract(side),
                red, green, blue, 0.98F);
    }

    private static void drawBeams(List<WirelessLinkNetwork.Link> links, Level level, Vec3 camera,
                                  Matrix4f matrix, float renderTick) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        double seconds = (level.getGameTime() + renderTick) / 20.0;
        for (WirelessLinkNetwork.Link link : links) {
            Vec3 start = getCollisionBoxCenter(level, link.output);
            Vec3 end = getCollisionBoxCenter(level, link.input);
            drawBeam(buffer, matrix, start, end, camera, seconds);
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static Vec3 getCollisionBoxCenter(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return Vec3.atCenterOf(pos).add(0, -0.0625, 0);
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty()) return Vec3.atCenterOf(pos);
        AABB box = shape.bounds().move(pos);
        return box.getCenter();
    }

    private static void drawBeam(BufferBuilder buffer, Matrix4f matrix, Vec3 start, Vec3 end,
                                 Vec3 camera, double seconds) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.01) return;
        Vec3 direction = delta.scale(1.0 / length);
        Vec3 midpoint = start.add(end).scale(0.5);
        Vec3 towardCamera = camera.subtract(midpoint).normalize();
        Vec3 side = direction.cross(towardCamera).normalize();
        if (side.lengthSqr() < 1.0e-5) side = direction.cross(new Vec3(0, 1, 0)).normalize();
        if (side.lengthSqr() < 1.0e-5) side = direction.cross(new Vec3(1, 0, 0)).normalize();
        if (side.lengthSqr() < 1.0e-5) return;

        addQuad(buffer, matrix,
                point(start, direction, side, 0, -BEAM_HALF_WIDTH),
                point(start, direction, side, length, -BEAM_HALF_WIDTH),
                point(start, direction, side, length, BEAM_HALF_WIDTH),
                point(start, direction, side, 0, BEAM_HALF_WIDTH),
                0.30F, 0.69F, 0.98F, 0.90F);

        double phase = seconds * ARROW_SPEED_BLOCKS_PER_SECOND % ARROW_SPACING;
        for (double distance = 0.42 + phase; distance < length - 0.2; distance += ARROW_SPACING) {
            double fadePhase = (seconds * 0.75 + distance * 0.31) % 1.0;
            float alpha = (float) (0.25 + 0.75 * Math.sin(Math.PI * fadePhase));
            Vec3 center = point(start, direction, side, distance, 0);
            drawArrow(buffer, matrix, center, direction, side, 0.18, BEAM_HALF_WIDTH * 0.78, alpha);
        }
    }

    private static Vec3 point(Vec3 origin, Vec3 direction, Vec3 side, double along, double across) {
        return origin.add(direction.scale(along)).add(side.scale(across));
    }

    private static void drawArrow(BufferBuilder buffer, Matrix4f matrix, Vec3 center,
                                  Vec3 direction, Vec3 side, double halfLength, double halfWidth, float alpha) {
        Vec3 tip = point(center, direction, side, halfLength, 0);
        Vec3 innerTip = point(center, direction, side, halfLength - 0.06, 0);
        double thick = 0.035;
        addQuad(buffer, matrix,
                point(center, direction, side, -halfLength, halfWidth), tip, innerTip,
                point(center, direction, side, -halfLength, halfWidth - thick),
                1.0F, 0.83F, 0.16F, alpha);
        addQuad(buffer, matrix,
                point(center, direction, side, -halfLength, -halfWidth),
                point(center, direction, side, -halfLength, -halfWidth + thick), innerTip, tip,
                1.0F, 0.83F, 0.16F, alpha);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                float red, float green, float blue, float alpha) {
        addVertex(buffer, matrix, a, red, green, blue, alpha);
        addVertex(buffer, matrix, b, red, green, blue, alpha);
        addVertex(buffer, matrix, c, red, green, blue, alpha);
        addVertex(buffer, matrix, d, red, green, blue, alpha);
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 point,
                                  float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha).endVertex();
    }

}
