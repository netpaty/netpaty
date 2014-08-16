package com.netparty.data;


import android.graphics.Bitmap;

import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;


public class AccountRec implements Account {

    private SocialNetwork netWork;
    private String id = "";
    private String userName = "";
    private String photoUrl = "";
    private Bitmap photo = null;



    public AccountRec(SocialNetwork netWork, String id){
        this.netWork = netWork;
        this.id = id;


    }

    public AccountRec(SocialNetwork netWork, String id, String userName){
        this.netWork = netWork;
        this.id = id;
        this.userName = userName;
    }

    public AccountRec(SocialNetwork netWork, String id, String userName, String photoUrl){
        this.netWork = netWork;
        this.id = id;
        this.userName = userName;
        this.photoUrl = photoUrl;
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

    @Override
    public Bitmap getPhoto() {
        return photo;
    }

    @Override
    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    @Override
    public String getPhotoUrl() {
        return photoUrl;
    }

}
