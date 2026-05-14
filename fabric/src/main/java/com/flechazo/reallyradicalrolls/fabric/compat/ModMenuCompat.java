package com.flechazo.reallyradicalrolls.fabric.compat;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent, ReallyRadicalRolls.MOD_ID);
    }
}
