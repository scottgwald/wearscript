package com.dappervision.wearscript_tagalong.events;

/**
 * Created by christianvazquez on 2/10/15.
 */
public class GETEvent {

    private final String cardId;
    private final String address;
    private final String callback;

    public GETEvent(String cardId, String address, String callback){
        this.cardId =cardId;
        this.address = address;
        this.callback = callback;
    }

    public String getCardId(){
        return cardId;
    }

    public String getAddress(){
        return address;
    }

    public String getCallback(){
        return callback;
    }

}
