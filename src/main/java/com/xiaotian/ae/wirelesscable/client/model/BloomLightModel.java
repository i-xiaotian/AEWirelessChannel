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
        if (originalQuads.isEmpty()) return originalQuads;

        final Block block = state.getBlock();
        if (!(block instanceof IBloomTexture)) return originalQuads;
        List<BakedQuad> quads = new ArrayList<>(originalQuads.size());

        IBloomTexture iBloomTexture = (IBloomTexture) block;
        for (BakedQuad quad : originalQuads) {
            String name = quad.getSprite().getName().getPath();
            if (name.endsWith(iBloomTexture.getBloomTextureEndWith())) {
                BakedQuad brightQuad = cache.get(quad);
                if (Objects.isNull(brightQuad)) {
                    brightQuad = makeFullBrightQuad(quad);
//                    cache.put(quad, brightQuad);
                }
                quads.add(brightQuad);
            } else {
                quads.add(quad);
            }
        }
        return quads;
    }

    public static BakedQuad makeFullBrightQuad(BakedQuad quad) {
        int[] vertexData = quad.getVertices().clone();
        int step = vertexData.length / 4;

        vertexData[6] = 0x00F000F0;
        vertexData[6 + step] = 0x00F000F0;
        vertexData[6 + 2 * step] = 0x00F000F0;
        vertexData[6 + 3 * step] = 0x00F000F0;

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                true
        );
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
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
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
