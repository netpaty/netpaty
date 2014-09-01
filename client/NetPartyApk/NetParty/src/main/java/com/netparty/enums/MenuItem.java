package com.netparty.enums;

import com.netparty.R;

/**
 * Created by Valentin on 26.08.2014.
 */
public enum MenuItem {
    CONTACTS("Contacts", R.drawable.photo),
    NEWS("News", R.drawable.edit);




    private String name;
    private int drawableId;

    MenuItem(String name, int drawableId){
        this.name = name;
        this.drawableId = drawableId;
    }

    public String getName(){
        return name;
    }

    public int getDrawableId(){
        return drawableId;
    }
}
