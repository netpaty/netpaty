package com.netparty.interfaces;


import com.netparty.enums.SocialNetwork;

import java.util.ArrayList;

public interface MetaContact {
    ArrayList<Account> getAccounts();
    String getId();
    void addAccount(Account account);
    boolean getNotifyFlag();
    void setId(String id);
    void setNotifyFlag(boolean flag);
    boolean containAccount(Account account);
    boolean containAccount(SocialNetwork network);
}
