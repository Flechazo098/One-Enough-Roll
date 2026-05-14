package com.flechazo.reallyradicalrolls.event;

import cc.sighs.oelib.event.Event;
import com.flechazo.reallyradicalrolls.flight.RollContext;
import net.minecraft.resources.Identifier;

public abstract class RollModifyEvent implements Event {
    private final Identifier groupId;
    private final RollContext context;

    private RollModifyEvent(Identifier groupId, RollContext context) {
        this.groupId = groupId;
        this.context = context;
    }

    public Identifier getGroupId() {
        return groupId;
    }

    public RollContext getContext() {
        return context;
    }

    public static class Pre extends RollModifyEvent {
        public Pre(Identifier groupId, RollContext context) {
            super(groupId, context);
        }
    }

    public static class Post extends RollModifyEvent {
        public Post(Identifier groupId, RollContext context) {
            super(groupId, context);
        }
    }
}
