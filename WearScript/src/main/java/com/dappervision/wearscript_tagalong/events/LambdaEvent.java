package com.dappervision.wearscript_tagalong.events;

public class LambdaEvent {
    private String command;

    public LambdaEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
