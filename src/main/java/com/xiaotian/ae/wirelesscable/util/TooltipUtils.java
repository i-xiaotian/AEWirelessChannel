package com.xiaotian.ae.wirelesscable.util;

import com.xiaotian.ae.wirelesscable.config.AEWirelessCableConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public static void addDismantlingTools(final List<String> tooltip) {
        final Set<String> localizedNames = new LinkedHashSet<>();
        final String[] configuredTools = AEWirelessCableConfig.dismantlingTools;
        if (configuredTools != null) {
            for (String configuredTool : configuredTools) {
                if (configuredTool == null || configuredTool.trim().isEmpty()) continue;
                addLocalizedToolNames(configuredTool.trim(), localizedNames);
            }
        }

        final String names = localizedNames.isEmpty()
                ? I18n.format("tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools_none")
                : String.join(", ", localizedNames);
        final String key = AEWirelessCableConfig.requireDismantlingTool
                ? "tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools"
                : "tooltip.aewirelesschannel.tile_wireless_bus.dismantling_tools_optional";
        tooltip.add(I18n.format(key, names));
    }

    private static void addLocalizedToolNames(final String configuredTool, final Set<String> names) {
        final String oreName = configuredTool.startsWith("ore:")
                ? configuredTool.substring("ore:".length()) : configuredTool;
        if (!configuredTool.startsWith("ore:") && configuredTool.contains(":")) {
            try {
                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(configuredTool));
                if (item != null) names.add(new ItemStack(item).getDisplayName());
            } catch (IllegalArgumentException ignored) {
                // Invalid config entries are skipped; valid entries still appear in the tooltip.
            }
        }

        for (ItemStack stack : OreDictionary.getOres(oreName)) {
            if (!stack.isEmpty()) names.add(stack.getDisplayName());
        }
    }
}
