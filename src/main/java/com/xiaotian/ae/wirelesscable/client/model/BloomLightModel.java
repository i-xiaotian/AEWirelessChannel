package com.xiaotian.ae.wirelesscable.client.model;

import com.xiaotian.ae.wirelesscable.block.IBloomTexture;
import com.xiaotian.ae.wirelesscable.client.RenderUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class BloomLightModel implements IBakedModel {

    private static final float MAX_LIGHT = 0.007f;
    private final IBakedModel base;

    private final Map<BakedQuad, BakedQuad> cache = new IdentityHashMap<>();

    public BloomLightModel(IBakedModel base) {
        this.base = base;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> originalQuads = base.getQuads(state, side, rand);
        final Block block = state.getBlock();
        if (!(block instanceof IBloomTexture iBloomTexture)) return originalQuads;
        List<BakedQuad> quads = new ArrayList<>(originalQuads.size());

        for (BakedQuad quad : originalQuads) {
            String name = quad.getSprite().getIconName();
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



    private BakedQuad makeFullBrightQuad(BakedQuad quad) {

        VertexFormat newFormat = RenderUtils.getFormatWithLightMap(quad.getFormat());

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(newFormat);

        builder.setQuadTint(quad.getTintIndex());
        builder.setQuadOrientation(quad.getFace());
        builder.setTexture(quad.getSprite());
        builder.setApplyDiffuseLighting(false);

        final VertexLighterFlat trans = getVertexLighterFlat(builder);
        quad.pipe(trans);

        return builder.build();
    }

    private static VertexLighterFlat getVertexLighterFlat(final UnpackedBakedQuad.Builder builder) {
        VertexLighterFlat trans = new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors()) {
            @Override
            @ParametersAreNonnullByDefault
            protected void updateLightmap(float[] normal, float[] lightMap, float x, float y, float z) {
                lightMap[0] = MAX_LIGHT;
                lightMap[1] = MAX_LIGHT;
            }

        };
        trans.setParent(builder);
        return trans;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return base.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return base.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return base.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return base.getOverrides();
    }

}
