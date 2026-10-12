package com.xiaotian.ae.wirelesscable.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ModConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue FORCE_CHUNK_LOADING;
    public static final ModConfigSpec.IntValue CHUNK_LOADING_RADIUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISMANTLING_TOOLS;
    public static final ModConfigSpec.BooleanValue REQUIRE_DISMANTLING_TOOL;
    public static final ModConfigSpec.BooleanValue TOP_CONNECTION_INFO_SHOW_TYPE;
    public static final ModConfigSpec.IntValue TOP_CONNECTION_INFO_SHOW_COUNT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        FORCE_CHUNK_LOADING = builder
                .comment("Allow wireless buses to keep their surrounding chunks loaded.")
                .define("forceChunk", true);
        CHUNK_LOADING_RADIUS = builder
                .comment("Chunk radius around each bus. 1 loads a 3x3 area.")
                .defineInRange("loadRadius", 1, 0, 10);
        DISMANTLING_TOOLS = builder
                .comment("Item IDs or item tags accepted for safe dismantling, for example ae2:certus_quartz_wrench or #forge:tools/wrenches.")
                .defineList("dismantlingTools", List.of("ae2:certus_quartz_wrench"), value -> value instanceof String);
        REQUIRE_DISMANTLING_TOOL = builder
                .comment("Require a configured tool to safely dismantle a wireless bus.")
                .define("requireDismantlingTool", true);
        TOP_CONNECTION_INFO_SHOW_TYPE = builder
                .comment("Only show connection details in TOP while sneaking.")
                .define("topConnectionInfoShowType", false);
        TOP_CONNECTION_INFO_SHOW_COUNT = builder
                .comment("Maximum number of output links shown in TOP.")
                .defineInRange("topConnectionInfoShowCount", 10, 0, 128);
        builder.pop();
        SPEC = builder.build();
    }

    private ModConfig() {
    }

    public static boolean isDismantlingTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return false;
        for (String configured : DISMANTLING_TOOLS.get()) {
            if (configured == null || configured.isBlank()) continue;
            String entry = configured.trim();
            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                if (tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId))) return true;
            } else if (itemId.toString().equals(entry)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> dismantlingToolNames() {
        List<String> names = new ArrayList<>();
        for (String configured : DISMANTLING_TOOLS.get()) {
            if (configured == null || configured.isBlank()) continue;
            String entry = configured.trim();
            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                if (tagId == null) continue;
                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, tagId);
                String exampleName = BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.builtInRegistryHolder().is(itemTag))
                        .map(item -> new ItemStack(item).getHoverName().getString())
                        .findFirst().orElse(tagId.toString());
                names.add(exampleName);
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id == null) continue;
            var item = BuiltInRegistries.ITEM.get(id);
            if (item != null) names.add(new ItemStack(item).getHoverName().getString());
        }
        return names.stream().distinct().toList();
    }
}
