package com.flechazo.reallyradicalrolls.event;

import cc.sighs.oelib.event.Event;
import com.flechazo.reallyradicalrolls.flight.RollProcessGroup;
import org.jetbrains.annotations.Nullable;

public class RollActiveGroupEvent implements Event {
    private @Nullable RollProcessGroup activeGroup;

    public @Nullable RollProcessGroup getActiveGroup() {
        return activeGroup;
    }

    public void setActiveGroup(@Nullable RollProcessGroup activeGroup) {
        this.activeGroup = activeGroup;
    }
}
