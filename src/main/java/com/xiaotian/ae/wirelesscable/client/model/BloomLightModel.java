package com.xiaotian.ae.wirelesscable.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BloomLightModel extends BakedModelWrapper<BakedModel> {
    private static final String BLOOM_TEXTURE_PATH = "block/bloom/";
    private static final int BLOCK_VERTEX_SIZE = DefaultVertexFormat.BLOCK.getIntegerSize();
    private static final int LIGHTMAP_OFFSET = findLightmapOffset();

    private final ConcurrentMap<BakedQuad, BakedQuad> fullBrightQuads = new ConcurrentHashMap<>();

    public BloomLightModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return makeBloomQuadsFullBright(originalModel.getQuads(state, side, random));
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                                    ModelData modelData, RenderType renderType) {
        return makeBloomQuadsFullBright(originalModel.getQuads(state, side, random, modelData, renderType));
    }

    private List<BakedQuad> makeBloomQuadsFullBright(List<BakedQuad> originalQuads) {
        List<BakedQuad> result = null;
        for (int i = 0; i < originalQuads.size(); i++) {
            BakedQuad quad = originalQuads.get(i);
            if (!isBloomTexture(quad.getSprite())) continue;

            if (result == null) result = new ArrayList<>(originalQuads);
            result.set(i, fullBrightQuads.computeIfAbsent(quad, BloomLightModel::makeFullBright));
        }
        return result == null ? originalQuads : List.copyOf(result);
    }

    private static boolean isBloomTexture(TextureAtlasSprite sprite) {
        return sprite.contents().name().getNamespace().equals(AEWirelessChannel.MOD_ID)
                && sprite.contents().name().getPath().startsWith(BLOOM_TEXTURE_PATH);
    }

    private static BakedQuad makeFullBright(BakedQuad quad) {
        int[] originalVertices = quad.getVertices();
        if (LIGHTMAP_OFFSET < 0 || originalVertices.length % BLOCK_VERTEX_SIZE != 0) return quad;

        int[] vertices = originalVertices.clone();
        for (int vertex = 0; vertex < vertices.length; vertex += BLOCK_VERTEX_SIZE) {
            vertices[vertex + LIGHTMAP_OFFSET] = LightTexture.FULL_BRIGHT;
        }

        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(),
                quad.isShade(), quad.hasAmbientOcclusion());
    }

    private static int findLightmapOffset() {
        int elementIndex = DefaultVertexFormat.BLOCK.getElements().indexOf(DefaultVertexFormat.ELEMENT_UV2);
        return elementIndex < 0 ? -1 : DefaultVertexFormat.BLOCK.getOffset(elementIndex) / Integer.BYTES;
    }
}
