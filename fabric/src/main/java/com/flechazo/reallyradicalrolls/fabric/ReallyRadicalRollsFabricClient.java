package com.flechazo.reallyradicalrolls.fabric;

import com.flechazo.reallyradicalrolls.ReallyRadicalRollsClient;
import net.fabricmc.api.ClientModInitializer;

public class ReallyRadicalRollsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReallyRadicalRollsClient.init();
    }
}
