package com.netparty.interfaces;


import android.graphics.Bitmap;

import com.netparty.enums.SocialNetwork;

public interface Account {
    String getId();
    SocialNetwork getNet();
    String getUserName();
    void setId(String id);
    Bitmap getPhoto();
    void setPhoto(Bitmap photo);
    String getPhotoUrl();

}
