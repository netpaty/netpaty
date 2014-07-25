package com.netparty.interfaces;


import java.util.ArrayList;

public interface MetaContact {
    ArrayList<SocialNetAccount> getAccounts();
    String getId();
    void addAccount(SocialNetAccount account);
    boolean getNotifyFlag();
    void setId(String id);
    void setNotifyFlag(boolean flag);
}
