package com.xiaotian.ae.wirelesscable.common.render;

import com.xiaotian.ae.wirelesscable.common.registry.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

public abstract class WirelessItemRender extends TileEntityItemStackRenderer {

    @Override
    @ParametersAreNonnullByDefault
    public void renderByItem(final ItemStack itemStackIn) {
        GlStateManager.pushMatrix();
        final Float[] renderScale = getRenderScale();
        if (renderScale == null || renderScale.length != 3) {
            GlStateManager.scale(0.3F, 0.3F, 0.3F);
        } else  GlStateManager.scale(renderScale[0], renderScale[1], renderScale[2]);

        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(
                Blocks.WIRELESS_OUTPUT_BUS.getDefaultState(), 1.0F);

        GlStateManager.popMatrix();
    }

    protected abstract Float[] getRenderScale();
}
