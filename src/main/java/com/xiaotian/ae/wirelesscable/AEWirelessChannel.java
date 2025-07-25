package com.xiaotian.ae.wirelesscable;

import com.xiaotian.ae.wirelesscable.config.AEWirelessChannelConfig;
import com.xiaotian.ae.wirelesscable.proxy.ModSetupComponent;
import com.xiaotian.ae.wirelesscable.registry.Blocks;
import com.xiaotian.ae.wirelesscable.registry.Items;
import com.xiaotian.ae.wirelesscable.registry.TileEntityTypes;
import com.xiaotian.ae.wirelesscable.tab.AEWirelessItemGroup;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AEWirelessChannel.MOD_ID)
public class AEWirelessChannel {

    public static final String MOD_ID = "aewirelesschannel";
    public static final String MOD_NAME = "AE-Wireless-Channel";

    public static AEWirelessChannel instance;
    public final static Logger log = LogManager.getLogger();

    public static ModSetupComponent proxy;

    public AEWirelessChannel() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEWirelessChannelConfig.CLIENT_SPEC);
        ModSetupComponent.wirelessItemGroup = new AEWirelessItemGroup(AEWirelessChannel.MOD_ID);
        Blocks.register();
        Items.register();
        TileEntityTypes.register();
    }

}
