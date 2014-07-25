package com.netparty.enums;


public enum SocialNetworks {
    FACEBOOK("FaceBook"),
    GOOGLE("Google+"),
    TWITTER("Twitter");

    private String name;

    private SocialNetworks(String name){
        this.name = name;
    }

    public static SocialNetworks fromString(String name) {
        if (name != null) {
            for (SocialNetworks type : SocialNetworks.values()) {
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
        String[] names = new String[SocialNetworks.values().length];
        for(int i = 0; i < names.length; i++){
            names[i] = SocialNetworks.values()[i].getName();
        }
        return names;
    }


}
