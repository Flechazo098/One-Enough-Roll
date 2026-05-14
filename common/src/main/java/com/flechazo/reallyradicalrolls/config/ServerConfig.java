package com.flechazo.reallyradicalrolls.config;

import cc.sighs.oelib.config.ConfigManager;
import cc.sighs.oelib.config.ConfigSchema;
import cc.sighs.oelib.config.ConfigUnit;
import cc.sighs.oelib.config.field.ConfigField;
import com.flechazo.reallyradicalrolls.ReallyRadicalRolls;
import net.minecraft.resources.Identifier;
public record ServerConfig(
        boolean allowThrusting,
        boolean forceEnabled,
        KineticDamage kineticDamage
) {
    public static final ConfigUnit<ServerConfig> CONFIG_UNIT;

    static {
        var def = ConfigSchema.defineServer(
                Identifier.fromNamespaceAndPath(ReallyRadicalRolls.MOD_ID, "server"),
                ServerConfig.class,
                meta -> meta.fileName("really_radical_rolls_server"),
                instance -> instance.group(
                        ConfigField.bool("allowThrusting").defaultValue(false).tooltip().forGetter(ServerConfig::allowThrusting),
                        ConfigField.bool("forceEnabled").defaultValue(false).tooltip().forGetter(ServerConfig::forceEnabled),
                        ConfigField.enumValue("kineticDamage", KineticDamage.class).defaultValue(KineticDamage.VANILLA).tooltip().forGetter(ServerConfig::kineticDamage)
                ).apply(instance, ServerConfig::new)
        );
        CONFIG_UNIT = def.unit();
    }

    public static void register() {
        ConfigManager.registerServer(CONFIG_UNIT, player -> player.level().getServer().getPlayerList().isOp(player.nameAndId()));
    }
}
