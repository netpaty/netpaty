package com.netparty.enums;


public enum SocialNetwork {
    FACEBOOK("FaceBook"),
    GOOGLE("Google+"),
    TWITTER("Twitter");

    private String name;

    private SocialNetwork(String name){
        this.name = name;
    }

    public static SocialNetwork fromString(String name) {
        if (name != null) {
            for (SocialNetwork type : SocialNetwork.values()) {
                if (name.equalsIgnoreCase(type.name)) {
                    return type;
                }
            }
        }
        return null;
    }

    public String getName(){
        return name;
    }

    public static String[] getAllNames() {
        String[] names = new String[SocialNetwork.values().length];
        for(int i = 0; i < names.length; i++){
            names[i] = SocialNetwork.values()[i].getName();
        }
        return names;
    }


}
