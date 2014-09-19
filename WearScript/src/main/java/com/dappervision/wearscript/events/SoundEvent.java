package com.dappervision.wearscript.events;

public class SoundEvent {
    private String type;
    private int id;

    public SoundEvent(String type,int id) {
        this.type = type;
        this.id = id;
    }


    public SoundEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getId(){
        return id;
    }
}
