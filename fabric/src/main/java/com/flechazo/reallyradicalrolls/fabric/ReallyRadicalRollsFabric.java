package com.flechazo.reallyradicalrolls.fabric;

import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import net.fabricmc.api.ModInitializer;

public class ReallyRadicalRollsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReallyRadicalRolls.init();
    }
}
