package com.example.dell.fireapp;

/**
 * Created by DELL on 11/13/2017.
 */

public class Chats {
    String last_meaasge;
    

    public Chats(){}

    public Chats(String last_messsage) {
        this.last_meaasge = last_messsage;
    }

    public String getLast_meaasge() {
        return last_meaasge;
    }

    public void setLast_meaasge(String last_meaasge) {
        this.last_meaasge = last_meaasge;
    }
}
