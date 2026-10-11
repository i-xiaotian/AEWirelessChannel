package com.xiaotian.ae.wirelesscable.util;

import appeng.me.GridNode;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public final class Ae2ChannelLimits {

    private static final int[] CHANNEL_COUNTS = readChannelCounts();

    private Ae2ChannelLimits() {}

    public static void addTooltip(final List<String> tooltip, final boolean dense) {
        final int channelLimit = getChannelLimit(dense);
        if (channelLimit < 0) return;
        tooltip.add(TextFormatting.GRAY + I18n.format(
                "tooltip.aewirelesschannel.channel_limit", channelLimit));
    }

    public static int getChannelLimit(final boolean dense) {
        final int index = dense ? 2 : 1;
        if (CHANNEL_COUNTS == null || CHANNEL_COUNTS.length <= index) return -1;
        return CHANNEL_COUNTS[index];
    }

    private static int[] readChannelCounts() {
        for (Field field : GridNode.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int[].class) continue;
            try {
                field.setAccessible(true);
                final int[] values = (int[]) field.get(null);
                if (values != null && values.length >= 3) return values.clone();
            } catch (ReflectiveOperationException | SecurityException ignored) {
                return null;
            }
        }
        return null;
    }
}
