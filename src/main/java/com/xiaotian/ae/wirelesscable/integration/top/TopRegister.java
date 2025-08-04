package com.xiaotian.ae.wirelesscable.integration.top;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;

public class TopRegister {

    public static void register() {
        final TheOneProbeImp top = TheOneProbe.theOneProbeImp;
        top.registerProvider(new TopInfoProvider());
    }

}
