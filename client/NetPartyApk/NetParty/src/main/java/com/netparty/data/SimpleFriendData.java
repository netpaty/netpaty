package com.netparty.data;


import android.graphics.Bitmap;

import com.netparty.enums.SocialNetwork;

public class SimpleFriendData {

    private String id;
    private String name;
    private String photoUrl;
    private SocialNetwork network;

    private Bitmap photo = null;

    public SimpleFriendData(SocialNetwork net, String id, String name, String photoUrl){
        this.name = name;
        this.id = id;
        this.photoUrl = photoUrl;
        this.network = net;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public SocialNetwork getNet(){
        return network;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
}
