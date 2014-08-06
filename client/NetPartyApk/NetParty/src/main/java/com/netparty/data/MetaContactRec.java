package com.netparty.data;


import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.MetaContact;
import com.netparty.interfaces.SocialNetAccount;

import java.util.ArrayList;

public class MetaContactRec implements MetaContact {

    private ArrayList<SocialNetAccount> accounts = new ArrayList<SocialNetAccount>();
    private String id;
    private boolean notifyFlag;

    public MetaContactRec(String id, boolean flag){
        notifyFlag = flag;
        this.id = id;
    }

    public MetaContactRec(boolean flag){
        notifyFlag = flag;
    }

    @Override
    public ArrayList<SocialNetAccount> getAccounts() {
        return accounts;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addAccount(SocialNetAccount account) {
        accounts.add(account);
    }

    @Override
    public boolean getNotifyFlag() {
        return notifyFlag;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setNotifyFlag(boolean flag) {
        this.notifyFlag = flag;
    }

    @Override
    public boolean hasGoogleAccount() {
        for(SocialNetAccount account: accounts){
            if(account.getNet().equals(SocialNetwork.GOOGLE)) return true;
        }
        return false;
    }

    @Override
    public boolean containAccount(SocialNetAccount account) {
        for(SocialNetAccount acc: accounts){
            if(acc.getId().equals(account.getId())) return true;
        }
        return false;
    }
}
