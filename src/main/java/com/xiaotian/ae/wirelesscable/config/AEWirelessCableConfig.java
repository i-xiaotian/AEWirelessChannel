package com.xiaotian.ae.wirelesscable.config;

import com.xiaotian.ae.wirelesscable.AEWirelessChannel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.oredict.OreDictionary;

@Config(modid = AEWirelessChannel.MOD_ID, name = "AEWirelessChannel")
public class AEWirelessCableConfig {

    @Config.Comment({
            "Chunk loading configuration.",
            "保持区块加载的相关配置。"
    })
    public static final ChunkLoad GENERAL_CONFIG = new ChunkLoad();

    @Config.Comment({
            "If true, connection information will only be shown while sneaking.",
            "If false, it will always be shown.",
            "",
            "若为 true，连接信息仅在玩家蹲下时显示。",
            "若为 false，则始终显示连接信息。"
    })
    public static boolean topConnectionInfoShowType = false;

    @Config.Comment({
            "The maximum number of connection info entries to display in The One Probe.",
            "This limits how many connections will be shown at once.",
            "",
            "在 The One Probe 中显示的最大连接信息数量。",
            "用于限制同时展示的连接数量，防止界面信息过多。"
    })
    public static int topConnectionInfoShowCount = 10;

    @Config.Comment({
            "Whether a configured tool is required to safely dismantle wireless buses.",
            "潜行右键安全拆除无线总线时是否必须使用下方列表中的工具。"
    })
    public static boolean requireDismantlingTool = true;

    @Config.Comment({
            "Allowed dismantling tools. Entries may be item registry IDs (modid:item) or Ore Dictionary names.",
            "拆除工具列表。可填写物品注册名（modid:item）或矿物词典名。默认 itemQuartzWrench。"
    })
    public static String[] dismantlingTools = {"itemQuartzWrench"};

    public static boolean isDismantlingTool(final ItemStack stack) {
        if (stack.isEmpty()) return false;

        final String registryName = String.valueOf(stack.getItem().getRegistryName());
        for (String configuredTool : dismantlingTools) {
            if (configuredTool == null || configuredTool.trim().isEmpty()) continue;
            final String tool = configuredTool.trim();
            if (tool.startsWith("ore:")) {
                if (hasOreName(stack, tool.substring("ore:".length()))) return true;
            } else if (tool.equals(registryName) || hasOreName(stack, tool)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOreName(final ItemStack stack, final String name) {
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (name.equals(OreDictionary.getOreName(oreId))) return true;
        }
        return false;
    }


    public static class ChunkLoad {

        @Config.Comment({
                "Whether the wireless bus can keep the chunk loaded.",
                "All wireless buses in a world share one Forge ticket owned by this mod.",
                "Forge's maximumChunksPerTicket setting limits the total chunks loaded by the mod in that world.",
                "",
                "无线频道总线是否可以保持区块加载。",
                "同一个世界内的无线总线共同使用由本模组持有的一张 Forge ticket。",
                "forgeChunkLoading.cfg 中的 maximumChunksPerTicket 限制本模组在该世界可加载的区块总数。"
        })
        public boolean forceChunk = false;

        @Config.Comment({
                "The chunk loading radius around the wireless bus.",
                "1 = 3x3 chunks, 2 = 5x5 chunks, and so on.",
                "",
                "无线总线保持区块加载时的加载半径。",
                "1 表示加载 3x3 区块，2 表示 5x5，以此类推。"
        })
        @Config.RangeInt(min = 0, max = 10)
        public int loadRadius = 1;
    }
}
