package com.xiaotian.ae.wirelesscable.client;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderUtils {

    private static final VertexFormat ITEM_FORMAT_WITH_LIGHT_MAP = new VertexFormat(DefaultVertexFormats.ITEM).addElement(DefaultVertexFormats.TEX_2S);

    public static VertexFormat getFormatWithLightMap(final VertexFormat format) {
        if (FMLClientHandler.instance().hasOptifine() || !ForgeModContainer.forgeLightPipelineEnabled) {
            return format;
        }

        if (format == DefaultVertexFormats.BLOCK) {
            return DefaultVertexFormats.BLOCK;
        } else if (format == DefaultVertexFormats.ITEM) {
            return ITEM_FORMAT_WITH_LIGHT_MAP;
        } else if (!format.hasUvOffset(1)) {
            VertexFormat result = new VertexFormat(format);
            result.addElement(DefaultVertexFormats.TEX_2S);
            return result;
        }
        return format;
    }
}
