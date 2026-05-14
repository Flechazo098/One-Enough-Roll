package com.flechazo.reallyradicalrolls.api;

public interface RollRenderState {
    boolean rollable$isRolling();

    void rollable$setRolling(boolean rolling);

    float rollable$getRoll();

    void rollable$setRoll(float roll);
}
