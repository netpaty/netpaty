package com.netparty.data;


import android.graphics.Bitmap;

import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;

import java.util.ArrayList;

public class MetaContactRec implements MetaContact, Account {

    private ArrayList<Account> accounts = new ArrayList<Account>();
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
    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SocialNetwork getNet() {
        if(accounts.size()>0) return accounts.get(0).getNet();
        return null;
    }

    @Override
    public String getUserName() {
        if(accounts.size()>0) return accounts.get(0).getUserName();
        return null;
    }

    @Override
    public void addAccount(Account account) {
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
    public Bitmap getPhoto() {
        if(accounts.size()>0) return accounts.get(0).getPhoto();
        return null;
    }

    @Override
    public void setPhoto(Bitmap photo) {
        if(accounts.size()>0) accounts.get(0).setPhoto(photo);
    }

    @Override
    public String getPhotoUrl() {
        if(accounts.size()>0) return accounts.get(0).getPhotoUrl();
        return null;
    }

    @Override
    public void setNotifyFlag(boolean flag) {
        this.notifyFlag = flag;
    }

    @Override
    public boolean containAccount(SocialNetwork network) {
        for(Account account: accounts){
            if(account.getNet().equals(network)) return true;
        }
        return false;
    }

    @Override
    public boolean containAccount(Account account) {
        for(Account acc: accounts){
            if(acc.getId().equals(account.getId())) return true;
        }
        return false;
    }
}
