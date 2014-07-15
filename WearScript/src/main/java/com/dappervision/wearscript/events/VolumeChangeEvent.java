package com.dappervision.wearscript.events;

public class VolumeChangeEvent {
    private final double newVolume;

    public VolumeChangeEvent(double volume) {
        newVolume = volume;
    }

    public double getNewVolume() {
        return newVolume;
    }
}
