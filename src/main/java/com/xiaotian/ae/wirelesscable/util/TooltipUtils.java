package com.xiaotian.ae.wirelesscable.util;

import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public final class TooltipUtils {

    private TooltipUtils() {}

    public static void addMultiLine(final List<String> tooltip, final String i18nKeyPrefix) {
        for (int i = 0; i < 100; i++) {
            final String key = i18nKeyPrefix + i;
            final String format = I18n.format(key);
            if (StringUtils.equals(format, key)) break;
            tooltip.add(format);
        }
    }
}
