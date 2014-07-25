package com.netparty.interfaces;


import com.netparty.enums.SocialNetworks;

public interface SocialNetAccount {
    String getId();
    String getLogin();
    String getPassword();
    SocialNetworks getNet();
    void setId(String id);

}
