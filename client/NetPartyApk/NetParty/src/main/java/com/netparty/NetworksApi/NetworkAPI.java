package com.netparty.NetworksApi;


import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetworks;
import com.netparty.exceptions.LoginException;
import com.netparty.interfaces.SocialNetAccount;

import java.util.ArrayList;

public class NetworkAPI {
    private static ArrayList<SocialNetAccount> accounts;
    private static NetworkAPI singleton;

    private NetworkAPI(){
        accounts = new ArrayList<SocialNetAccount>();
        accounts.add(new SocialNetAccountRec(SocialNetworks.FACEBOOK, "Hellen", "2000", "1561"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.FACEBOOK, "Bobby", "2000", "5918"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.FACEBOOK, "Anna", "2000", "4458"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.FACEBOOK, "Anna2", "2000", "1358"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.FACEBOOK, "Sam", "2000", "8614"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.GOOGLE, "Hellen", "2000", "447"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.GOOGLE, "Evil", "2000", "668"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.GOOGLE, "Legolas", "2000", "478"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.GOOGLE, "Joan", "2000", "364"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.TWITTER, "Sam", "2000", "78954"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.TWITTER, "Hellen", "2000", "54789"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.TWITTER, "Anna", "2000", "33589"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.TWITTER, "Bob", "2000", "68974"));
        accounts.add(new SocialNetAccountRec(SocialNetworks.TWITTER, "John", "2000", "66889"));
    }

    public static String login(SocialNetAccount account) throws LoginException {
        if(singleton == null) singleton = new NetworkAPI();
        for(SocialNetAccount a: accounts){
            if(a.getNet().equals(account.getNet()) &&
                    a.getLogin().equals(account.getLogin())&&
                    a.getPassword().equals(account.getPassword())) return a.getId();
        }
        throw new LoginException();
    }


}
