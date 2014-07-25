package com.netparty.data;


import android.support.v7.appcompat.R;

import com.netparty.enums.SocialNetworks;



public class SocialNetAccountRec implements com.netparty.interfaces.SocialNetAccount {

    private SocialNetworks network;
    private String id = "";
    private String login = "";
    private String password = "";


    public SocialNetAccountRec(SocialNetworks network, String login, String password){
        this.network = network;
        this.login = login;
        this.password = password;

    }

    public SocialNetAccountRec(SocialNetworks network, String login,
                               String password, String id){
        this.network = network;
        this.login = login;
        this.password = password;
        this.id = id;

    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public SocialNetworks getNet() {
        return network;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
