package com.xiaotian.ae.wirelesscable.util;

import appeng.api.networking.IGridNode;
import appeng.core.AEConfig;

public final class ChannelLimitUtils {
    private ChannelLimitUtils() {
    }

    public static String getLimit(boolean dense, IGridNode node) {
        if (node != null) return format(node.getMaxChannels());
        if (AEConfig.instance().getChannelMode().name().equals("INFINITE")) return "\u221e";
        // In AE2 15.x, normal and dense cable limits are fixed by the cable type.
        return dense ? "32" : "8";
    }

    private static String format(int limit) {
        return limit == Integer.MAX_VALUE ? "\u221e" : Integer.toString(limit);
    }
}
