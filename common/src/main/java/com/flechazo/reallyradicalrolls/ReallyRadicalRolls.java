package com.flechazo.reallyradicalrolls;

import cc.sighs.oelib.event.EventAutoRegistration;
import com.flechazo.reallyradicalrolls.config.ServerConfig;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReallyRadicalRolls {

    public static final String MOD_ID = "really_radical_rolls";
    public static final String MOD_NAME = "Really Radical Rolls";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        EventAutoRegistration.registerBasePackage("com.flechazo.reallyradicalrolls.event");
        ServerConfig.register();
    }

    public static Identifier id(String value) {
        return Identifier.fromNamespaceAndPath(MOD_ID, value);
    }
}