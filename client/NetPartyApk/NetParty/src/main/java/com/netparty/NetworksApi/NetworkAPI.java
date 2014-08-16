package com.netparty.NetworksApi;


import com.netparty.data.AccountRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.exceptions.LoginException;
import com.netparty.interfaces.Account;

import java.util.ArrayList;

public class NetworkAPI {
    private static ArrayList<Account> accounts;
    private static NetworkAPI singleton;

    private NetworkAPI(){
        accounts = new ArrayList<Account>();
        accounts.add(new AccountRec(SocialNetwork.FACEBOOK,  "1561"));
        accounts.add(new AccountRec(SocialNetwork.FACEBOOK,  "5918"));
        accounts.add(new AccountRec(SocialNetwork.FACEBOOK,  "4458"));
        accounts.add(new AccountRec(SocialNetwork.FACEBOOK,  "1358"));
        accounts.add(new AccountRec(SocialNetwork.FACEBOOK,  "8614"));
        accounts.add(new AccountRec(SocialNetwork.GOOGLE,  "447"));
        accounts.add(new AccountRec(SocialNetwork.GOOGLE,  "668"));
        accounts.add(new AccountRec(SocialNetwork.GOOGLE,  "478"));
        accounts.add(new AccountRec(SocialNetwork.GOOGLE,  "364"));
        accounts.add(new AccountRec(SocialNetwork.TWITTER,  "78954"));
        accounts.add(new AccountRec(SocialNetwork.TWITTER,  "54789"));
        accounts.add(new AccountRec(SocialNetwork.TWITTER,  "33589"));
        accounts.add(new AccountRec(SocialNetwork.TWITTER,  "68974"));
        accounts.add(new AccountRec(SocialNetwork.TWITTER,  "66889"));
    }

    public static String login(Account account) throws LoginException {
        if(singleton == null) singleton = new NetworkAPI();
        for(Account a: accounts){
            if(a.getNet().equals(account.getNet())) return a.getId();
        }
        throw new LoginException();
    }


}
