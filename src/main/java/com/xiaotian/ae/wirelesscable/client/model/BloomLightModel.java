package com.xiaotian.ae.wirelesscable.client.model;

import com.xiaotian.ae.wirelesscable.block.IBloomTexture;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class BloomLightModel implements IBakedModel {

//    private static final float MAX_LIGHT = 0xF000F0;
    private final IBakedModel base;

    private final Map<BakedQuad, BakedQuad> cache = new IdentityHashMap<>();

    public BloomLightModel(IBakedModel base) {
        this.base = base;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @Nonnull final Random rand) {
        if (Objects.isNull(state)) return Collections.emptyList();
        List<BakedQuad> originalQuads = base.getQuads(state, side, rand);
        final Block block = state.getBlock();
        if (!(block instanceof IBloomTexture)) return originalQuads;
        List<BakedQuad> quads = new ArrayList<>(originalQuads.size());

        IBloomTexture iBloomTexture = (IBloomTexture) block;
        for (BakedQuad quad : originalQuads) {
            String name = quad.getSprite().getName().getNamespace();
            if (name.endsWith(iBloomTexture.getBloomTextureEndWith())) {
                BakedQuad brightQuad = cache.get(quad);
                if (Objects.isNull(brightQuad)) {
                    brightQuad = makeFullBrightQuad(quad);
                    cache.put(quad, brightQuad);
                }
                quads.add(brightQuad);
            } else {
                quads.add(quad);
            }
        }
        return quads;
    }

    public static BakedQuad makeFullBrightQuad(BakedQuad quad) {
        int[] originalData = quad.getVertices();
        int[] newData = originalData.clone();

        int vertexCount = 4;
        int intsPerVertex = originalData.length / vertexCount;

        // 自己打包最大光照，天空和区块光都是15
        int maxBlockLight = 15;
        int maxSkyLight = 15;
        int packedLight = (maxBlockLight & 0xF) | ((maxSkyLight & 0xF) << 4);


        // 光照坐标的偏移（float，两个int）一般是第6和7个int
        int lightmapUOffset = 4;

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            int baseIndex = vertex * intsPerVertex;

            // 写入最大光照，先转float再写入int数组
            newData[baseIndex + lightmapUOffset] = Float.floatToRawIntBits((float) packedLight);
            newData[baseIndex + lightmapUOffset + 1] = 0;
        }

        return new BakedQuad(newData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return base.getOverrides();
    }
}
