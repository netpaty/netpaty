package com.netparty.interfaces;


import com.netparty.enums.SocialNetwork;

public interface SocialNetAccount {
    String getId();
    SocialNetwork getNet();
    String getUserName();
    void setId(String id);

}
