package com.netparty.viewers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.netparty.data.MetaContactRec;
import com.netparty.fragments.ContactsListFragment;
import com.netparty.interfaces.MenuPanelShowListener;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;
import com.netparty.utils.web.WebUtils;
import com.netparty.views.MenuPanel;
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
        unregisterGoogleReceiver();

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

        if(currentFragment instanceof ContactsListFragment) {
            if (getService().getPlusClient().isConnected()) ((ContactsListFragment)currentFragment).loadGoogleFriends();
            ((ContactsListFragment)currentFragment).loadFacebookFriends();
        }

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
            return 4;
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
            switch (position){
                case 0:
                    text.setText("Photo");
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                    break;
                case 1:
                    text.setText("Chat");
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.chat));
                    break;
                case 2:
                    text.setText("Edit");
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.edit));
                    break;
                case 3:
                    text.setText("Media");
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.media));
                    break;
            }
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
