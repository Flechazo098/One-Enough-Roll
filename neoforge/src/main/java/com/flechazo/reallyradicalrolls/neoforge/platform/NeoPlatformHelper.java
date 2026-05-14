package com.flechazo.reallyradicalrolls.neoforge.platform;

import com.flechazo.reallyradicalrolls.platform.PlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class NeoPlatformHelper implements PlatformHelper {
    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist() == Dist.CLIENT;
    }
}
