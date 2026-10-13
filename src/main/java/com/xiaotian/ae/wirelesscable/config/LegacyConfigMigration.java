package com.xiaotian.ae.wirelesscable.config;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyConfigMigration {
    private static final Pattern VALUE = Pattern.compile("(?im)^\\s*[BIS]:([A-Za-z0-9_.-]+)\\s*=\\s*(.*?)\\s*$");
    private static final Pattern TOOL_LIST = Pattern.compile("(?is)S:dismantlingTools\\s*<([^>]*)>");

    private LegacyConfigMigration() {
    }

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != ModConfig.SPEC) return;
        Path oldFile = FMLPaths.CONFIGDIR.get().resolve("AEWirelessChannel.cfg");
        if (!Files.isRegularFile(oldFile)) return;
        try {
            String contents = Files.readString(oldFile);
            List<String[]> values = findValues(contents);
            applyBoolean(values, "forceChunk", ModConfig.FORCE_CHUNK_LOADING::set);
            applyInteger(values, "loadRadius", ModConfig.CHUNK_LOADING_RADIUS::set);
            applyBoolean(values, "requireDismantlingTool", ModConfig.REQUIRE_DISMANTLING_TOOL::set);
            applyBoolean(values, "topConnectionInfoShowType", ModConfig.TOP_CONNECTION_INFO_SHOW_TYPE::set);
            applyInteger(values, "topConnectionInfoShowCount", ModConfig.TOP_CONNECTION_INFO_SHOW_COUNT::set);
            List<String> tools = findToolList(contents);
            if (!tools.isEmpty()) {
                for (int index = 0; index < tools.size(); index++) {
                    tools.set(index, migrateToolEntry(tools.get(index)));
                }
                ModConfig.DISMANTLING_TOOLS.set(tools);
            }
            ModConfig.SPEC.save();
            archiveOldFile(oldFile);
            AEWirelessChannel.LOGGER.info("Migrated settings from {} to the NeoForge config", oldFile.getFileName());
        } catch (IOException exception) {
            AEWirelessChannel.LOGGER.error("Could not migrate legacy config {}", oldFile, exception);
        }
    }

    private static List<String[]> findValues(String contents) {
        List<String[]> entries = new ArrayList<>();
        Matcher matcher = VALUE.matcher(contents);
        while (matcher.find()) entries.add(new String[]{matcher.group(1), matcher.group(2).trim()});
        return entries;
    }

    private static List<String> findToolList(String contents) {
        Matcher matcher = TOOL_LIST.matcher(contents);
        if (!matcher.find()) return List.of();
        List<String> tools = new ArrayList<>();
        for (String line : matcher.group(1).split("\\R")) {
            String tool = line.trim();
            if (!tool.isEmpty()) tools.add(tool);
        }
        return tools;
    }

    private static String migrateToolEntry(String entry) {
        if (entry.equalsIgnoreCase("itemQuartzWrench")) return "ae2:certus_quartz_wrench";
        String legacyName = entry.regionMatches(true, 0, "ore:", 0, 4) ? entry.substring(4) : entry;
        if (legacyName.contains(":")) return entry;
        String normalized = legacyName.toLowerCase(Locale.ROOT);
        String tag = switch (normalized) {
            case "toolwrench", "toolquartzwrench", "toolcertusquartzwrench" -> "tools/wrenches";
            case "toolpickaxe" -> "tools/pickaxes";
            case "toolaxe" -> "tools/axes";
            case "toolshovel" -> "tools/shovels";
            case "toolhoe" -> "tools/hoes";
            case "toolhammer" -> "tools/hammers";
            default -> normalized;
        };
        return "#forge:" + tag;
    }

    private static void applyBoolean(List<String[]> values, String key, java.util.function.Consumer<Boolean> setter) {
        for (String[] value : values) {
            if (!value[0].equalsIgnoreCase(key)) continue;
            if (value[1].equalsIgnoreCase("true") || value[1].equalsIgnoreCase("false")) {
                setter.accept(Boolean.parseBoolean(value[1]));
            }
            return;
        }
    }

    private static void applyInteger(List<String[]> values, String key, java.util.function.Consumer<Integer> setter) {
        for (String[] value : values) {
            if (!value[0].equalsIgnoreCase(key)) continue;
            try {
                setter.accept(Integer.parseInt(value[1]));
            } catch (NumberFormatException ignored) {
                // Keep the new config default when the old value is invalid.
            }
            return;
        }
    }

    private static void archiveOldFile(Path oldFile) throws IOException {
        Path archive = oldFile.resolveSibling(oldFile.getFileName() + ".migrated");
        int suffix = 1;
        while (Files.exists(archive)) {
            archive = oldFile.resolveSibling(oldFile.getFileName() + ".migrated." + suffix++);
        }
        Files.move(oldFile, archive, StandardCopyOption.ATOMIC_MOVE);
    }
}
