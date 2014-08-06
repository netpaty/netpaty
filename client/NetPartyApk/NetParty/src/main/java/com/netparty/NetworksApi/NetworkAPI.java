package com.netparty.NetworksApi;


import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.exceptions.LoginException;
import com.netparty.interfaces.SocialNetAccount;

import java.util.ArrayList;

public class NetworkAPI {
    private static ArrayList<SocialNetAccount> accounts;
    private static NetworkAPI singleton;

    private NetworkAPI(){
        accounts = new ArrayList<SocialNetAccount>();
        accounts.add(new SocialNetAccountRec(SocialNetwork.FACEBOOK,  "1561"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.FACEBOOK,  "5918"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.FACEBOOK,  "4458"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.FACEBOOK,  "1358"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.FACEBOOK,  "8614"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.GOOGLE,  "447"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.GOOGLE,  "668"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.GOOGLE,  "478"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.GOOGLE,  "364"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.TWITTER,  "78954"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.TWITTER,  "54789"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.TWITTER,  "33589"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.TWITTER,  "68974"));
        accounts.add(new SocialNetAccountRec(SocialNetwork.TWITTER,  "66889"));
    }

    public static String login(SocialNetAccount account) throws LoginException {
        if(singleton == null) singleton = new NetworkAPI();
        for(SocialNetAccount a: accounts){
            if(a.getNet().equals(account.getNet())) return a.getId();
        }
        throw new LoginException();
    }


}
