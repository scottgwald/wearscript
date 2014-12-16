package com.dappervision.wearscript_tagalong.events;

public class BluetoothWriteEvent {
    private final String data;
    private String address;

    public BluetoothWriteEvent(String address, String data) {
        this.address = address;
        this.data = data;
    }

    public byte[] getBuffer() {
        return data.getBytes();
    }

    public String getAddress() {
        return address;
    }
}
