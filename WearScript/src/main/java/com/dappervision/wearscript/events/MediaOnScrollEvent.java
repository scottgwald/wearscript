package com.dappervision.wearscript.events;

public class MediaOnScrollEvent {
    private float displacement, delta, velocity;

    public MediaOnScrollEvent(float v1, float v2, float v3) {
        this.displacement = v1;
        this.delta = v2;
        this.velocity = v3;
    }

    public float getDisplacement() {
        return displacement;
    }

    public float getDelta() {
        return delta;
    }

    public float getVelocity() {
        return velocity;
    }

}
