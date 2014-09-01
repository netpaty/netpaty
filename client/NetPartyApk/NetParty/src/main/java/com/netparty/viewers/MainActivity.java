package com.netparty.viewers;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.netparty.R;
import com.netparty.data.AccountRec;
import com.netparty.enums.MenuItem;
import com.netparty.fragments.AbstractFragment;
import com.netparty.fragments.ContactsListFragment;
import com.netparty.fragments.NewsFragment;
import com.netparty.interfaces.MenuPanelShowListener;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.utils.web.WebUtils;
import com.netparty.views.MenuPanel;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;
import android.support.v4.app.FragmentManager;


public class MainActivity extends AbstractActivity implements View.OnClickListener {



    private ImageView fbIcon, googleIcon, ava;
    private Button signOut;
    private TextView name;
    private Account firstAcc;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment currentFragment;
    private RelativeLayout fragmentContainer;
    private View menuDriver, focusCatcher;
    private MenuPanel menuPanel;
    private ListView menu;
    private MenuAdapter adapter;


    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        menuPanel = (MenuPanel)findViewById(R.id.menu);
        menu = (ListView)menuPanel.findViewById(R.id.menu_list);
        adapter = new MenuAdapter();
        menu.setAdapter(adapter);

        adapter.notifyDataSetChanged();



        focusCatcher = findViewById(R.id.focus_catcher);
        focusCatcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getX() < 0)
                    menuPanel.move(event.getX() + menuPanel.getWidth(), event.getAction());
                if(event.getAction() == MotionEvent.ACTION_UP && event.getX() >= 0){
                    focusCatcher.setVisibility(View.VISIBLE);
                    focusCatcher.requestFocus();
                    menuPanel.setX(0);
                }
                return true;
            }
        });






        menuPanel.setShowListener(new MenuPanelShowListener() {
            @Override
            public void menuShown() {
                focusCatcher.setVisibility(View.VISIBLE);
                focusCatcher.requestFocus();
            }

            @Override
            public void menuHide() {
                focusCatcher.setVisibility(View.INVISIBLE);
            }
        });

        fragmentContainer = (RelativeLayout)findViewById(R.id.fragment_container);




        menuDriver = findViewById(R.id.dragMenu);
        menuDriver.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    menuPanel.bringToFront();
                    fragmentContainer.setEnabled(false);
                }

                menuPanel.move(event.getX(), event.getAction());
                return true;
            }
        });


        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        currentFragment = new ContactsListFragment();
        transaction.add(R.id.fragment_container, currentFragment);
        transaction.commit();

        fragmentContainer = (RelativeLayout)findViewById(R.id.fragment_container);

        findViewById(R.id.accounts).setOnClickListener(this);



        fbIcon = (ImageView)findViewById(R.id.fb_icon);
        googleIcon = (ImageView)findViewById(R.id.google_icon);

        ava = (ImageView)findViewById(R.id.ava);



        name = (TextView)findViewById(R.id.name);

        //Typeface font = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf");
        signOut = (Button)findViewById(R.id.out);
       // signOut.setTypeface(font);
        signOut.setOnClickListener(this);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (MenuItem.values()[position]){
                    case CONTACTS:
                        if(!(currentFragment instanceof ContactsListFragment)){
                            transaction = fragmentManager.beginTransaction();
                            currentFragment = new ContactsListFragment();
                            transaction.replace(R.id.fragment_container, currentFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            focusCatcher.setVisibility(View.INVISIBLE);
                            menuPanel.setX(-menuPanel.getWidth());

                        }
                        break;
                    case NEWS:
                        if(!(currentFragment instanceof NewsFragment)){
                            transaction = fragmentManager.beginTransaction();
                            currentFragment = new NewsFragment();
                            transaction.replace(R.id.fragment_container, currentFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            focusCatcher.setVisibility(View.INVISIBLE);
                            menuPanel.setX(-menuPanel.getWidth());
                        }
                        break;
                }
            }
        });



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
        unregisterReceivers();

    }

    @Override
    protected void onGoogleConnected() {
        if(getService().getMetaContact().containAccount(SocialNetwork.GOOGLE)){
            if(firstAcc.getNet().equals(SocialNetwork.GOOGLE)
                    && getService().getPlusClient().isConnected()){
                loadGooglePhoto();

            }
            googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle));
            if(currentFragment instanceof ContactsListFragment)
                ((ContactsListFragment)currentFragment).loadGoogleFriends();
        }
    }

    @Override
    protected void onGoogleDisconnected() {

    }

    @Override
    protected void onGoogleConnectionFailed() {
        ConnectionResult mConnectionResult = null;
        if(getService()!=null) mConnectionResult = getService().getGoogleConnectionResult();
        if(mConnectionResult != null) {
            try {
                mConnectionResult.startResolutionForResult(MainActivity.this, LoginActivity.REQUEST_CODE_RESOLVE_ERR);

            } catch (IntentSender.SendIntentException e) {
                if(getService() != null) getService().getPlusClient().connect();
            }
        }
    }

    @Override
    protected void onVKTokenExpired(VKAccessToken expiredToken) {

    }

    @Override
    protected void onVKAccessDenied(VKError authorizationError) {

    }

    @Override
    protected void onVKReceiveNewToken(VKAccessToken newToken) {
        loadVKPhoto();
    }

    @Override
    protected void onVKAcceptUserToken(VKAccessToken token) {
        loadVKPhoto();
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();



        if(getService().getMetaContact().getAccounts().size()>1){
            ava.setBackgroundResource(R.drawable.gold_frame);
        }

        if(getService().getMetaContact().containAccount(SocialNetwork.GOOGLE) && getService().getPlusClient() != null){
            if(!getService().getPlusClient().isConnected()){
                getService().getPlusClient().connect();
            }
        }

        if(getService().getMetaContact().containAccount(SocialNetwork.FACEBOOK)){
            if(!Session.getActiveSession().isOpened()){
                Session.openActiveSession(this, true, callback);
            }
        }

        firstAcc = getService().getMetaContact().getAccounts().get(0);

        if(firstAcc.getNet().equals(SocialNetwork.FACEBOOK)
                && Session.getActiveSession().isOpened()){
            loadFacebookPhoto();
        }

        if(firstAcc.getNet().equals(SocialNetwork.GOOGLE)
                && getService().getPlusClient().isConnected()){
            loadGooglePhoto();

        }

        if(firstAcc.getNet().equals(SocialNetwork.VK)
                && VKSdk.isLoggedIn()){
            loadVKPhoto();
        }

        ArrayList<Account> accounts = getService().getMetaContact().getAccounts();
        for(Account account: accounts){
            if(account.getNet().equals(SocialNetwork.FACEBOOK)){
                if(Session.getActiveSession() != null && Session.getActiveSession().isOpened()){
                    fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_circle));
                }
                else fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_circle_gray));
            }
            if(account.getNet().equals(SocialNetwork.GOOGLE)){

                if(getService().getPlusClient().isConnected()){
                    googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle));
                }
                else googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle_gray));
            }
        }

        ((AbstractFragment)currentFragment).onServiceConnected(getService());

        name.setText(getService().getMetaContact().getAccounts().get(0).getUserName());

    }



    private void loadGooglePhoto(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                firstAcc.setPhoto(WebUtils.loadPhoto(getService().
                        getPlusClient().getCurrentPerson().
                        getImage().getUrl().split("\\?")[0] + "?sz=120"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        ava.setImageBitmap(firstAcc.getPhoto());
                    }
                });
            }
        }).start();
    }

    private void loadFacebookPhoto(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                firstAcc.setPhoto(WebUtils.loadPhoto("https://graph.facebook.com/" + firstAcc.getId()
                        + "/picture?height=120"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        ava.setImageBitmap(firstAcc.getPhoto());
                    }
                });
            }

        }).start();


    }



    private void loadVKPhoto(){
          VKRequest r = VKApi.users().get();
          r.addExtraParameter("user_id",firstAcc.getId());
          r.addExtraParameter("fields","photo_100");
          r.executeWithListener(new VKRequest.VKRequestListener() {
          @Override
          public void onComplete(VKResponse response) {
                VKList<VKApiUser> users = (VKList<VKApiUser>)response.parsedModel;
                    if(users != null && users.size()>0) {
                        final VKApiUser user = users.get(0);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(user != null) firstAcc.setPhoto(WebUtils.loadPhoto(user.photo_100));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ava.setImageBitmap(firstAcc.getPhoto());
                                    }
                                });
                            }
                        }).start();
                    }
                }
          });
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.out){

            SharedPreferences preferences = this.getSharedPreferences(LoginActivity.PREFERENCES,
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LoginActivity.LAST_MC_ID, "");
            editor.commit();
            if(Session.getActiveSession() != null && Session.getActiveSession().isOpened())
                Session.getActiveSession().closeAndClearTokenInformation();
            if(getService().getPlusClient() != null && getService().getPlusClient().isConnected()) {
                getService().getPlusClient().clearDefaultAccount();
                getService().getPlusClient().revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                    @Override
                    public void onAccessRevoked(ConnectionResult connectionResult) {

                    }
                });
            }
            if(VKSdk.isLoggedIn()){
                VKSdk.logout();
            }
            Intent intent = new Intent().setClass(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else
        if (v.getId() == R.id.accounts){
            startActivity(new Intent(this, AccountActivity.class));

        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {

            if(session.isOpened()){
                if(firstAcc.getNet().equals(SocialNetwork.FACEBOOK)
                        && Session.getActiveSession().isOpened()){
                    loadFacebookPhoto();
                }
                if(currentFragment instanceof ContactsListFragment)
                    ((ContactsListFragment)currentFragment).loadFacebookFriends();
                fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_circle));


            }
        }
    };

    private class MenuAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return MenuItem.values().length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.menu_item, null);
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            TextView text = (TextView)view.findViewById(R.id.text);
            text.setText(MenuItem.values()[position].getName());
            icon.setImageDrawable(
                    getResources().getDrawable(
                            MenuItem.values()[position].getDrawableId()));
            return view;
        }
    }

    public void addContact(Account account){
        if(currentFragment!=null){
            if(currentFragment instanceof ContactsListFragment){
                ((ContactsListFragment)currentFragment).addContact(account);
            }
        }
    }

}
