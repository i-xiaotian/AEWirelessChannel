package com.xiaotian.ae.wirelesscable.integration.jade;

import com.xiaotian.ae.wirelesscable.block.WirelessBusBlock;
import com.xiaotian.ae.wirelesscable.tile.WirelessBusBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class JadeIntegration implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(JadeInfoProvider.INSTANCE, WirelessBusBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadeInfoProvider.INSTANCE, WirelessBusBlock.class);
    }
}
