package com.netparty.viewers;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;


import com.netparty.R;
import com.netparty.data.MetaContactRec;
import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.MetaContact;
import com.netparty.interfaces.SocialNetAccount;
import com.netparty.services.NPService;
import com.netparty.utils.db.DataBaseAdapter;

import com.facebook.*;
import com.facebook.model.*;

import java.util.Arrays;


public class LoginActivity extends AbstractActivity implements View.OnClickListener {

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private SignInButton plusButton;

    private boolean firstSignIn = true, processLogin = true;

    public static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private DataBaseAdapter dbAdapter;

    private TextView hint;
    private Button loginBtn, googleSignOutBtn;
    private LoginButton faceBookLoginBtn;

    private UiLifecycleHelper uiHelper;

    public final static String PREFERENCES = "preferences";
    public final static String LAST_MC_ID = "last_id";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onServiceConnected() {

        mPlusClient = getService().getPlusClient();

        String lastMCId = preferences.getString(LAST_MC_ID, "");
        if(!"".equals(lastMCId)) {
            MetaContact mc = dbAdapter.getMetaContact(lastMCId);
            processMetaContact(mc, null);
        }


    }


    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_login);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(saveInstanceState);

        registerReceiver(googleEventReceiver, new IntentFilter(NPService.GOOGLE_EVENT));

        preferences = this.getSharedPreferences(PREFERENCES,
                Activity.MODE_PRIVATE);
        editor = preferences.edit();

        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");






        plusButton = (SignInButton)findViewById(R.id.sign_in_button);

        plusButton.setOnClickListener(this);

        googleSignOutBtn = (Button)findViewById(R.id.sign_out_button);
        googleSignOutBtn.setOnClickListener(this);

        faceBookLoginBtn = (LoginButton)findViewById(R.id.authButton);
        faceBookLoginBtn.setReadPermissions(Arrays.asList("user_friends"));

        hint = (TextView)findViewById(R.id.hint);
        loginBtn = (Button)findViewById(R.id.app_login);
        loginBtn.setOnClickListener(this);

        dbAdapter = new DataBaseAdapter(this);
        dbAdapter.showDB();

    }


    @Override
    protected void onStop() {
        super.onStop();


    }



    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

    }



    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        unregisterReceiver(googleEventReceiver);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }




    private void onSocialNetworkLogin(SocialNetAccount account){
        if(processLogin) {
            if (firstSignIn) {
                MetaContact mc = dbAdapter.getMetaContact(account);
                processMetaContact(mc, account);
            } else {
                //TODO try to get from db
                MetaContact mc = getService().getMetaContact();
                if (mc != null) mc.addAccount(account);
            }
        }
    }

    private void processMetaContact(MetaContact mc, SocialNetAccount acc){
        if(mc != null) {
            if(acc != null && !mc.containAccount(acc)) mc.addAccount(acc);
            getService().setMetaContact(mc);
        }
        else{
            mc = new MetaContactRec(true);
            if(acc != null) mc.addAccount(acc);
            getService().setMetaContact(mc);
        }
        mc = getService().getMetaContact();
        if(mc.getAccounts().size()>0 && mc.getNotifyFlag()) {
            showAddAccDialog(mc.getAccounts().get(0));
        }
        else {
            if(mc.getAccounts().size()>0){
                processLogin = false;
                loginNetParty();
            }
            else Log.e("tag", "AccountList is empty");
        }
    }




    private void showAddAccDialog(final SocialNetAccount account){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_acc_dialog_tittle);
        String welcomeMsg = String.format(getResources().getString(R.string.add_acc_dialog_msg),
                account.getUserName());
        builder.setMessage(welcomeMsg);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loginBtn.setVisibility(View.VISIBLE);
                hint.setText(getString(R.string.add_acc_hint));
                firstSignIn = false;
            }
        });
        builder.setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loginNetParty();
            }
        });
        builder.setNegativeButton(R.string.never, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MetaContact mc = getService().getMetaContact();
                if (mc != null) {
                    mc.setNotifyFlag(false);
                    loginNetParty();
                }
                else Log.e("tag", "MC is empty");
            }
        });
        builder.show();
    }



    private void loginNetParty(){
        MetaContact mc = getService().getMetaContact();
        if(mc != null && mc.getAccounts().size()>0){
            dbAdapter.updateOrAddMetaContact(mc);

            editor.putString(LAST_MC_ID, mc.getId());
            editor.commit();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else Log.e("tag", "No MC or Account!");
    }

    //Facebook

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("tag", "state=" + state);
            if(session.isOpened()){
                Request.newMeRequest(session, new Request.GraphUserCallback() {

                    // callback after Graph API response with user object
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            onSocialNetworkLogin(new SocialNetAccountRec(SocialNetwork.FACEBOOK, user.getId(), user.getName()));
                        }
                    }
                }).executeAsync();

            }
        }
    };


    //Google+

    private BroadcastReceiver googleEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(NPService.GOOGLE_EVENT);
            if(event.equals(NPService.GOOGLE_CONNECT)){
                mConnectionProgressDialog.dismiss();
                Log.e("tag", "Connected " + mPlusClient.getAccountName() + "  " + mPlusClient.isConnected());

                onSocialNetworkLogin(new SocialNetAccountRec(SocialNetwork.GOOGLE, mPlusClient.getAccountName(),
                        mPlusClient.getCurrentPerson().getDisplayName()));
                plusButton.setVisibility(View.GONE);
                googleSignOutBtn.setVisibility(View.VISIBLE);
            }
            else
            if (event.equals(NPService.GOOGLE_DISCONNECT)){

            }
            else
            if (event.equals(NPService.GOOGLE_CONNECTION_FAILED)){
                mConnectionProgressDialog.dismiss();
                mConnectionResult = getService().getGoogleConnectionResult();
                if(mConnectionResult != null) {
                    Log.e("tag", "FAILED " + mConnectionResult.toString());
                    try {
                        Log.e("tag", "startResolutionForResult");
                        mConnectionResult.startResolutionForResult(LoginActivity.this, REQUEST_CODE_RESOLVE_ERR);
                        mConnectionProgressDialog.show();
                    } catch (IntentSender.SendIntentException e) {
                        mConnectionProgressDialog.show();
                        mPlusClient.connect();
                    }
                }
            }
        }
    };



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
            Log.e("tag", "ConnectBtn clicked ");
            mConnectionProgressDialog.show();
            mPlusClient.connect();
        }
        if (v.getId() == R.id.app_login){
            MetaContact mc = getService().getMetaContact();
            if(mc != null){
                mc.setNotifyFlag(false);
                loginNetParty();
            }
            else Log.e("tag", "MC is empty");
        }
        if(v.getId() == R.id.sign_out_button){
            Log.e("tag", "sign out");
            if (mPlusClient.isConnected()) {
                Log.e("tag", "sign if");
                mPlusClient.clearDefaultAccount();
                mPlusClient.revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                    @Override
                    public void onAccessRevoked(ConnectionResult connectionResult) {
                        Log.e("tag", "accessRevoked");
                    }
                });
                plusButton.setVisibility(View.VISIBLE);
                googleSignOutBtn.setVisibility(View.GONE);
            }
        }
    }
}
