package com.xiaotian.ae.wirelesscable.integration.top;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

public final class TopIntegration {
    private TopIntegration() {
    }

    public static void enqueue() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () ->
                (Function<ITheOneProbe, Void>) probe -> {
                    probe.registerProvider(new TopInfoProvider());
                    return null;
                });
    }
}
