package com.netparty.data;


import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.SocialNetAccount;



public class SocialNetAccountRec implements SocialNetAccount {

    private SocialNetwork netWork;
    private String id = "";
    String userName = "";



    public SocialNetAccountRec(SocialNetwork netWork, String id){
        this.netWork = netWork;
        this.id = id;


    }

    public SocialNetAccountRec(SocialNetwork netWork, String id, String userName){
        this.netWork = netWork;
        this.id = id;
        this.userName = userName;


    }



    @Override
    public String getId() {
        return id;
    }

    @Override
    public SocialNetwork getNet() {
        return netWork;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
