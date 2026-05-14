package com.flechazo.reallyradicalrolls.compat.debug;

import com.flechazo.reallyradicalrolls.api.RollableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public class RollDebugEntry implements DebugScreenEntry {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("one_enough_roll", "roll");

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        if (Minecraft.getInstance().player instanceof RollableEntity rollable && rollable.rollable$isRolling()) {
            displayer.addLine(String.format(Locale.ROOT, "Roll: %.1f°", rollable.rollable$getRoll(1.0f)));
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}
