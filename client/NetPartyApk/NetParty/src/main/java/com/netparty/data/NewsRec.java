package com.netparty.data;

import com.netparty.interfaces.News;

/**
 * Created by Valentin on 26.08.2014.
 */
public class NewsRec implements News {

    private String name;
    private String type;
    private String message;

    public NewsRec(String name, String type, String message){
        this.name = name;
        this.type = type;
        this.message = message;
    }


    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return name;
    }
}
