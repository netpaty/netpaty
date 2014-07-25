package com.netparty.viewers;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import com.netparty.NetworksApi.NetworkAPI;
import com.netparty.R;
import com.netparty.data.MetaContactRec;
import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetworks;
import com.netparty.exceptions.LoginException;
import com.netparty.interfaces.MetaContact;
import com.netparty.interfaces.SocialNetAccount;
import com.netparty.utils.db.DataBaseAdapter;

import com.facebook.*;
import com.facebook.model.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class LoginActivity extends AbstractActivity implements View.OnClickListener {

    Spinner networks;
    EditText editLogin, editPassword;
    Button signInBtn;

    DataBaseAdapter dbAdapter;

    @Override
    protected void onServiceConnected() {

    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_login);
        //Log.e("tag", "service " + getService());
        dbAdapter = new DataBaseAdapter(this);
        //just for testing
       // dbAdapter.showDB();

        editLogin = (EditText)findViewById(R.id.login);
        editPassword = (EditText)findViewById(R.id.pass);
        signInBtn = (Button)findViewById(R.id.sign_in);
        signInBtn.setOnClickListener(this);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, SocialNetworks.getAllNames());

        networks = (Spinner)findViewById(R.id.networks);
        networks.setAdapter(adapter);



        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.e("tag", "Hello " + session + "!");
                if(exception!= null)Log.e("tag", "E " + exception.getMessage());
                if (session.isOpened()) {
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {

                            if (user != null) {

                                Log.e("tag", "Hello " + user.getName() + "!");
                            }
                        }
                    }).executeAsync();
                }
            }
        });

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case (R.id.sign_in):
                String login = editLogin.getText().toString();
                String password = editPassword.getText().toString();
                SocialNetworks net = SocialNetworks.
                        values()[networks.getSelectedItemPosition()];

                SocialNetAccountRec account = new SocialNetAccountRec(net, login, password);

                if (loginWithSocialNet(account)){
                    MetaContact mc = dbAdapter.getMetaContact(account);
                    if(mc == null){
                        showAddAccDialog(account);
                    }
                    else {
                        if(mc.getNotifyFlag()){
                            getService().setMetaContact(mc);
                            showAddAccDialog(account);
                        }
                        else loginNetParty(mc);
                    }
                }

                break;
        }
    }

    public boolean loginWithSocialNet(SocialNetAccount account){
        try {
            String id = NetworkAPI.login(account);
            account.setId(id);
            return true;



        } catch (LoginException e) {
            new AlertDialog.Builder(this).setTitle(R.string.error_title).
                    setMessage(R.string.login_error_msg).
                    setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
            return false;
        }
    }


    private void showAddAccDialog(final SocialNetAccount account){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_acc_dialog_tittle);
        String welcomeMsg = String.format(getResources().getString(R.string.add_acc_dialog_msg),
                account.getLogin());
        builder.setMessage(welcomeMsg);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginSocialDialog d = new LoginSocialDialog(LoginActivity.this, account);
                d.show();
            }
        });
        builder.setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MetaContact mc = new MetaContactRec(true);
                mc.addAccount(account);
                dbAdapter.addMetaContact(mc);
                loginNetParty(mc);
            }
        });
        builder.setNegativeButton(R.string.never, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MetaContact mc = new MetaContactRec(false);
                mc.addAccount(account);
                dbAdapter.addMetaContact(mc);
                loginNetParty(mc);
            }
        });
        builder.show();

    }

    public void loginNetParty(MetaContact metaContact){
        getService().setMetaContact(metaContact);
        Intent intent = new Intent().setClass(this, FirstActivity.class);
        startActivity(intent);

    }



}
