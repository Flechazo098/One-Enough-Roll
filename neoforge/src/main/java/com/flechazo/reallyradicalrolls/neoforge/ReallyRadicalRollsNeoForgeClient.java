package com.flechazo.reallyradicalrolls.neoforge;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.flechazo.reallyradicalrolls.ReallyRadicalRollsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ReallyRadicalRolls.MOD_ID, dist = Dist.CLIENT)
public class ReallyRadicalRollsNeoForgeClient {
    public ReallyRadicalRollsNeoForgeClient(IEventBus bus) {
        ReallyRadicalRollsClient.init();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (_, parent) -> new ConfigScreen(parent, ReallyRadicalRolls.MOD_ID));
    }
}
