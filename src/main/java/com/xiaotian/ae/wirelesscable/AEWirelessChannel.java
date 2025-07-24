package com.xiaotian.ae.wirelesscable;

import com.xiaotian.ae.wirelesscable.config.AEWirelessChannelConfig;
import com.xiaotian.ae.wirelesscable.proxy.ClientProxy;
import com.xiaotian.ae.wirelesscable.proxy.CommonProxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AEWirelessChannel.MOD_ID)
public class AEWirelessChannel {

    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE-Wireless-Channel";

    public static AEWirelessChannel instance;
    public static Logger log = LogManager.getLogger();

    public static CommonProxy proxy;

    public AEWirelessChannel() {
        instance = this;

        // 注册配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEWirelessChannelConfig.CLIENT_SPEC);

        // 根据不同物理环境加载代理
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> proxy = new CommonProxy());

        // 注册事件监听器
        FMLJavaModLoadingContext.get().getModEventBus().addListener(proxy::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    // 通用初始化，类似旧版init
    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        log.info(MOD_NAME + " common setup start.");
        proxy.init();
    }

}
