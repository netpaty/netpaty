package com.netparty.viewers;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.netparty.R;
import com.netparty.interfaces.SocialNetAccount;
import com.netparty.services.NPService;

import java.util.ArrayList;

public class FirstActivity extends AbstractActivity {

    TextView tv;

    @Override
    protected void onServiceConnected() {

        ArrayList<SocialNetAccount> accounts = getService().getMetaContact().getAccounts();
        String res = "";
        for(SocialNetAccount account: accounts){
            res += "Net: ";
            res += account.getNet().getName() + "; ";
            res += "Id: ";
            res += account.getId() + "; ";
            res += "login: ";
            res += account.getLogin() + "; ";
            res += "\n";
        }
        tv.setText(res);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        tv = (TextView)findViewById(R.id.acc);

    }
}
