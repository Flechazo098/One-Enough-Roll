package com.flechazo.reallyradicalrolls.flight;

import cc.sighs.oelib.event.EventBus;
import com.flechazo.reallyradicalrolls.event.RollActiveGroupEvent;
import com.flechazo.reallyradicalrolls.event.RollModifyEvent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RollProcessGroup {
    private static final Map<Identifier, RollProcessGroup> BY_ID = new HashMap<>();

    private final Identifier id;
    private final List<ConditionEntry> conditions = new ArrayList<>();
    private final List<Consumer<RollContext>> beforeModifiers = new ArrayList<>();
    private final List<Consumer<RollContext>> afterModifiers = new ArrayList<>();

    private RollProcessGroup(Identifier id) {
        this.id = id;
    }

    public static RollProcessGroup get(Identifier id) {
        return BY_ID.computeIfAbsent(id, RollProcessGroup::new);
    }

    public static RollProcessGroup getActive() {
        RollActiveGroupEvent event = new RollActiveGroupEvent();
        EventBus.post(event);
        return event.getActiveGroup();
    }

    public Identifier getId() {
        return this.id;
    }

    /**
     * Register a simple enable predicate (default priority 0).
     * Equivalent to {@code trueIf(condition, 0)}.
     */
    public void registerEnablePredicate(BooleanSupplier predicate) {
        trueIf(predicate, 0);
    }

    /**
     * If this condition is true, the group is enabled.
     */
    public void trueIf(BooleanSupplier condition, int priority) {
        conditions.add(new ConditionEntry(priority, () -> condition.getAsBoolean() ? TriState.TRUE : TriState.PASS));
        conditions.sort(null);
    }

    public void trueIf(BooleanSupplier condition) {
        trueIf(condition, 0);
    }

    /**
     * If this condition is false, the group is disabled (no further checks).
     * Otherwise passes to the next condition.
     */
    public void falseUnless(BooleanSupplier condition, int priority) {
        conditions.add(new ConditionEntry(priority, () -> condition.getAsBoolean() ? TriState.PASS : TriState.FALSE));
        conditions.sort(null);
    }

    public void falseUnless(BooleanSupplier condition) {
        falseUnless(condition, 0);
    }

    public void registerBeforeModifier(Consumer<RollContext> modifier) {
        this.beforeModifiers.add(modifier);
    }

    public void registerAfterModifier(Consumer<RollContext> modifier) {
        this.afterModifiers.add(modifier);
    }

    public boolean enabled() {
        for (ConditionEntry entry : conditions) {
            var result = entry.condition.get();
            if (result != TriState.PASS) {
                return result == TriState.TRUE;
            }
        }
        return false;
    }

    public void processBeforeModifier(RollContext context) {
        for (Consumer<RollContext> modifier : beforeModifiers)
            modifier.accept(context);
        EventBus.post(new RollModifyEvent.Pre(this.id, context));
    }

    public void processAfterModifier(RollContext context) {
        for (Consumer<RollContext> modifier : afterModifiers)
            modifier.accept(context);
        EventBus.post(new RollModifyEvent.Post(this.id, context));
    }

    private record ConditionEntry(int priority, Supplier<TriState> condition) implements Comparable<ConditionEntry> {
        @Override
        public int compareTo(ConditionEntry o) {
            return Integer.compare(priority, o.priority);
        }
    }
}
