package com.netparty.viewers;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

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
import com.netparty.data.AccountRec;
import com.netparty.data.MetaContactRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;
import com.netparty.utils.db.DataBaseAdapter;

import com.facebook.*;
import com.facebook.model.*;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.util.Arrays;


public class LoginActivity extends AbstractActivity implements View.OnClickListener {

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private SignInButton plusButton;

    private Button vkBtn;

    private boolean firstSignIn = true, processLogin = true;

    public static final int REQUEST_CODE_RESOLVE_ERR = 9000;


    private static String[] sMyScope = new String[]{VKScope.FRIENDS, VKScope.WALL, VKScope.PHOTOS};

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
        super.onServiceConnected();
        mPlusClient = getService().getPlusClient();
        if (VKSdk.isLoggedIn()) {
            vkBtn.setText(getString(R.string.logout));
        } else {
            vkBtn.setText(getString(R.string.login_with_vk));
        }
        if (VKSdk.wakeUpSession()) {
            vkBtn.setText(getString(R.string.logout));
        }

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
        faceBookLoginBtn.setReadPermissions(Arrays.asList("user_friends", "friends_online_presence",
                "friends_online_presence"));

        vkBtn = (Button)findViewById(R.id.vk_button);
        vkBtn.setOnClickListener(this);



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
        VKUIHelper.onResume(this);

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
        unregisterReceivers();
        VKUIHelper.onDestroy(this);

    }

    @Override
    protected void onGoogleConnected() {
        mConnectionProgressDialog.dismiss();
        Log.e("tag", "Connected " + mPlusClient.getAccountName() + " " + mPlusClient.getCurrentPerson() + "  " + mPlusClient.isConnected());

        onSocialNetworkLogin(new AccountRec(SocialNetwork.GOOGLE, mPlusClient.getAccountName(),
                mPlusClient.getCurrentPerson().getDisplayName()));
        plusButton.setVisibility(View.GONE);
        googleSignOutBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onGoogleDisconnected() {

    }

    @Override
    protected void onGoogleConnectionFailed() {
        mConnectionProgressDialog.dismiss();
        mConnectionResult = getService().getGoogleConnectionResult();
        if(mConnectionResult != null) {
            Log.e("tag", "FAILED " + mConnectionResult.toString());
            try {
                Log.e("tag", "LoginActivity startResolutionForResult");
                mConnectionResult.startResolutionForResult(LoginActivity.this, REQUEST_CODE_RESOLVE_ERR);
                mConnectionProgressDialog.show();
            } catch (IntentSender.SendIntentException e) {
                mConnectionProgressDialog.show();
                mPlusClient.connect();
            }
        }
    }

    @Override
    protected void onVKTokenExpired(VKAccessToken expiredToken) {
        VKSdk.authorize(sMyScope);
    }

    @Override
    protected void onVKAccessDenied(VKError authorizationError) {
        vkBtn.setText(getString(R.string.login_with_vk));
    }

    @Override
    protected void onVKReceiveNewToken(VKAccessToken newToken) {
        vkConnected(newToken);
    }

    @Override
    protected void onVKAcceptUserToken(VKAccessToken token) {
        vkConnected(token);
    }

    private void vkConnected(VKAccessToken token){
        vkBtn.setText(getString(R.string.logout));

        VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKList<VKApiUser> users = (VKList<VKApiUser>)response.parsedModel;
                if(users != null && users.size()>0) {
                    VKApiUser user = users.get(0);
                    onSocialNetworkLogin(new AccountRec(SocialNetwork.VK, String.valueOf(user.getId()),
                            user.first_name + " " + user.last_name));


                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }



    private void onSocialNetworkLogin(Account account){
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

    private void processMetaContact(MetaContact mc, Account acc){
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




    private void showAddAccDialog(final Account account){
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
            unregisterReceivers();
            dbAdapter.updateOrAddMetaContact(mc);
            editor.putString(LAST_MC_ID, mc.getId());
            editor.commit();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

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
                            onSocialNetworkLogin(new AccountRec(SocialNetwork.FACEBOOK, user.getId(), user.getName()));
                        }
                    }
                }).executeAsync();

            }
        }
    };



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
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
            if (mPlusClient.isConnected()) {
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
        if(v.getId() == R.id.vk_button){
            if (VKSdk.isLoggedIn()) {
                VKSdk.logout();
                vkBtn.setText(getString(R.string.login_with_vk));
            } else {
                VKSdk.authorize(sMyScope);
            }
        }
    }
}
