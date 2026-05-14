package com.flechazo.reallyradicalrolls.neoforge;


import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ReallyRadicalRolls.MOD_ID)
public class ReallyRadicalRollsNeoForge {
    public ReallyRadicalRollsNeoForge(IEventBus eventBus) {
        ReallyRadicalRolls.init();
    }
}
