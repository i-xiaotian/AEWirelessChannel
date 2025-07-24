package com.xiaotian.ae.wirelesscable.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AEWirelessChannelConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT = new ClientConfig(CLIENT_BUILDER);
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    public static class ClientConfig {

        public final ForgeConfigSpec.BooleanValue topConnectionInfoShowType;
        public final ForgeConfigSpec.IntValue topConnectionInfoShowCount;

        public final ChunkLoadConfig chunkLoad;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Chunk loading configuration.", "保持区块加载的相关配置。").push("chunkLoad");
            chunkLoad = new ChunkLoadConfig(builder);
            builder.pop();

            topConnectionInfoShowType = builder
                    .comment(
                            "If true, connection information will only be shown while sneaking.",
                            "If false, it will always be shown.",
                            "若为 true，连接信息仅在玩家蹲下时显示。",
                            "若为 false，则始终显示连接信息。"
                    )
                    .define("topConnectionInfoShowType", false);

            topConnectionInfoShowCount = builder
                    .comment(
                            "The maximum number of connection info entries to display in The One Probe.",
                            "This limits how many connections will be shown at once.",
                            "在 The One Probe 中显示的最大连接信息数量。",
                            "用于限制同时展示的连接数量，防止界面信息过多。"
                    )
                    .defineInRange("topConnectionInfoShowCount", 10, 1, Integer.MAX_VALUE);
        }
    }

    public static class ChunkLoadConfig {
        public final ForgeConfigSpec.BooleanValue forceChunk;
        public final ForgeConfigSpec.IntValue loadRadius;

        public ChunkLoadConfig(ForgeConfigSpec.Builder builder) {
            forceChunk = builder
                    .comment(
                            "Whether the wireless bus can keep the chunk loaded.",
                            "The 'maximumChunksPerTicket' setting in 'forgeChunkLoading.cfg' affects",
                            "how many buses can keep chunks loaded. The default limit is 25.",
                            "无线频道总线是否可以保持区块加载。",
                            "forgeChunkLoading.cfg 文件中的 maximumChunksPerTicket 配置项",
                            "影响可加载区块的总线数量，默认限制为 25 个。"
                    )
                    .define("forceChunk", false);

            loadRadius = builder
                    .comment(
                            "The chunk loading radius around the wireless bus.",
                            "1 = 3x3 chunks, 2 = 5x5 chunks, and so on.",
                            "无线总线保持区块加载时的加载半径。",
                            "1 表示加载 3x3 区块，2 表示 5x5，以此类推。"
                    )
                    .defineInRange("loadRadius", 1, 0, 10);
        }
    }
}
