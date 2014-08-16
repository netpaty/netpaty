package com.netparty.viewers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.netparty.R;

import com.netparty.views.DragDropListView;
import com.netparty.data.AccountRec;
import com.netparty.data.MetaContactRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.utils.web.WebUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AbstractActivity implements View.OnClickListener {


    private DragDropListView friendsLeft, friendsRight;
    private ImageView fbIcon, googleIcon, ava, dragItemView;
    private Button signOut;
    private TextView name;
    private Account firstAcc;
    private RelativeLayout.LayoutParams dragViewParams;
    private MetaContactRec dragItem = null;


    private UiLifecycleHelper uiHelper;

    FriendsAdapter adapterLeft, adapterRight;

    ArrayList<MetaContactRec> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        findViewById(R.id.accounts).setOnClickListener(this);

        dragItemView = (ImageView)findViewById(R.id.drag_image);

        dragViewParams = (RelativeLayout.LayoutParams) dragItemView.getLayoutParams();

        friendsLeft = (DragDropListView)findViewById(R.id.friends_left);
        friendsRight = (DragDropListView)findViewById(R.id.friends_right);

        friendsLeft.setDragListener(new DragListener(true));
        friendsRight.setDragListener(new DragListener(false));
        friendsLeft.setOutUpListener(new UpOutListener(true));
        friendsRight.setOutUpListener(new UpOutListener(false));

        fbIcon = (ImageView)findViewById(R.id.fb_icon);
        googleIcon = (ImageView)findViewById(R.id.google_icon);

        ava = (ImageView)findViewById(R.id.ava);

        friendsLeft.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                dragItem = (MetaContactRec)friendsLeft.getAdapter().getItem(position);
                Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                v.draw(canvas);
                dragItemView.setImageBitmap(bitmap);
                dragItemView.setVisibility(View.VISIBLE);
                dragItemView.bringToFront();
                return true;
            }
        });

        friendsRight.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                dragItem = (MetaContactRec)friendsRight.getAdapter().getItem(position);
                Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                v.draw(canvas);
                dragItemView.setImageBitmap(bitmap);
                dragItemView.setVisibility(View.VISIBLE);
                dragItemView.bringToFront();
                return true;
            }
        });

        friendsLeft.setOnItemClickListener(new friendsClickListener());
        friendsRight.setOnItemClickListener(new friendsClickListener());

        name = (TextView)findViewById(R.id.name);

        Typeface font = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf");
        signOut = (Button)findViewById(R.id.out);
        signOut.setTypeface(font);
        signOut.setOnClickListener(this);

        friendsList = new ArrayList<MetaContactRec>();
        adapterLeft = new FriendsAdapter(this, friendsList, true);
        adapterRight = new FriendsAdapter(this, friendsList, false);
        friendsLeft.setAdapter(adapterLeft);
        friendsRight.setAdapter(adapterRight);

    }

    private class friendsClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ContactDetailsDialog dialog = new ContactDetailsDialog(MainActivity.this, friendsList.get(position));
            dialog.show();
        }
    }


    private class DragListener implements View.OnTouchListener{

        boolean isLeft;

        public DragListener(boolean isLeft){
            this.isLeft = isLeft;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                dragViewParams.leftMargin = (int) event.getX();
                if(isLeft)dragViewParams.leftMargin -= dragItemView.getWidth()/2;
                else dragViewParams.leftMargin += dragItemView.getWidth()/2;
                dragViewParams.topMargin = (int) event.getY() - dragItemView.getHeight()/2;
            }
            if (event.getAction() == MotionEvent.ACTION_UP){
                dragItemView.setVisibility(View.GONE);
                if(friendsRight.equals(v))friendsRight.receiveItem((int)event.getX(), (int)event.getY(), dragItem);
                else if(friendsLeft.equals(v)) friendsLeft.receiveItem((int)event.getX(), (int)event.getY(), dragItem);
                if(dragItem != null) {
                    friendsList.remove(dragItem);
                    updateAdapters();
                    dragItem = null;
                }
            }

            dragItemView.setLayoutParams(dragViewParams);
            return true;
        }
    }

    private class UpOutListener implements View.OnTouchListener{

        boolean isLeft;

        public UpOutListener(boolean isLeft){
            this.isLeft = isLeft;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int)event.getX();
            if(isLeft) x -= v.getWidth();
            else x += v.getWidth();
            int y = (int)event.getY();
            if(friendsLeft.equals(v)) friendsRight.receiveItem(x, y, dragItem);
            else friendsLeft.receiveItem(x, y, dragItem);

            return true;
        }
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
                Log.e("tag", "load google ph");
            }
            googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle));
            loadGoogleFriends();
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

        name.setText(getService().getMetaContact().getAccounts().get(0).getUserName());

        if(getService().getMetaContact().getAccounts().size()>1){
            ava.setBackground(getResources().getDrawable(R.drawable.gold_frame));
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
            Log.e("tag", "load google ph");
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


        if(getService().getPlusClient().isConnected()) loadGoogleFriends();
        loadFacebookFriends();



    }

    private void updateAdapters(){
        adapterLeft.notifyDataSetChanged();
        adapterRight.notifyDataSetChanged();
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

    private void loadGoogleFriends(){
        getService().getPlusClient().loadVisiblePeople(new PlusClient.OnPeopleLoadedListener() {
            @Override
            public void onPeopleLoaded(ConnectionResult connectionResult, PersonBuffer persons, String s) {

                if (connectionResult.getErrorCode() == ConnectionResult.SUCCESS) {
                    try {
                        Iterator<Person> iterator = persons.iterator();
                        while (iterator.hasNext()) {
                            Person p = iterator.next();
                            MetaContactRec mc = new MetaContactRec(false);
                            mc.addAccount(new AccountRec(SocialNetwork.GOOGLE, p.getId(), p.getDisplayName(), p.getImage().getUrl()));
                            friendsList.add(mc);
                        }
                    } finally {
                        persons.close();
                        updateAdapters();
                    }
                } else {
                    Log.e("tag", "Error listing people: " + connectionResult.getErrorCode());
                }
            }
        }, null);
    }

    private void loadFacebookFriends(){
        new Request(
                Session.getActiveSession(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        try {
                            GraphObject go  = response.getGraphObject();
                            JSONObject jso = go.getInnerJSONObject();
                            JSONArray arr = jso.getJSONArray( "data" );
                            for ( int i = 0; i < arr.length() ; i++ )
                            {
                                JSONObject json_obj = arr.getJSONObject( i );
                                final String id = json_obj.getString("id");
                                final String name = json_obj.getString("name");
                                final String url = json_obj.getJSONObject("picture").getJSONObject("data").getString("url");
                                MetaContactRec mc = new MetaContactRec(false);
                                mc.addAccount(new AccountRec(SocialNetwork.FACEBOOK, id, name, url));
                                friendsList.add(mc);
                            }
                        }
                        catch ( Throwable t )
                        {
                            t.printStackTrace();
                            Log.e("tag", "catch" + t.getMessage());
                        }
                        updateAdapters();

                    }

                }
        ).executeAsync();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.out){
            Log.e("tag", "googleState " + getService().getPlusClient().isConnected());
            Log.e("tag", " " + getService().getPlusClient());
            SharedPreferences preferences = this.getSharedPreferences(LoginActivity.PREFERENCES,
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LoginActivity.LAST_MC_ID, "");
            editor.commit();
            if(Session.getActiveSession() != null && Session.getActiveSession().isOpened())
                Session.getActiveSession().closeAndClearTokenInformation();
            if(getService().getPlusClient() != null && getService().getPlusClient().isConnected()) {
                Log.e("tag", "googleRevokedCall");
                getService().getPlusClient().clearDefaultAccount();
                getService().getPlusClient().revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                    @Override
                    public void onAccessRevoked(ConnectionResult connectionResult) {
                        Log.e("tag", "accessRevoked");
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




    private class FriendsAdapter extends BaseAdapter{

        Context context;
        ArrayList<MetaContactRec> list;
        LayoutInflater inflater;
        View view;
        boolean left;

        public FriendsAdapter(Context context, ArrayList<MetaContactRec> list, boolean left){
            this.context = context;
            this.list = list;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.left = left;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            FrameLayout fake = new FrameLayout(context);
            fake.setVisibility(View.GONE);
            if(left ){
                if (position%2 == 0)return fake;
            }
            else {
                if (position%2 == 1)return fake;
            }
            view = inflater.inflate(R.layout.simple_friend_item, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            final ImageView photo = (ImageView)view.findViewById(R.id.photo);
            if(list.get(position).getAccounts().size()>1) {
                photo.setBackground(getResources().getDrawable(R.drawable.gold_frame));
            }
            name.setText(list.get(position).getUserName());
            final ImageView icon = (ImageView)view.findViewById(R.id.net_icon);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_circle));
            }
            else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle));
            }
            if(list.get(position).getPhoto() != null) {
                photo.setImageBitmap(list.get(position).getPhoto());
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = WebUtils.loadPhoto(list.get(position).getPhotoUrl());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photo.setImageBitmap(bitmap);
                                list.get(position).setPhoto(bitmap);
                            }
                        });
                    }
                }).start();

            }
            return view;
        }


    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("tag", "state=" + state);
            if(session.isOpened()){
                if(firstAcc.getNet().equals(SocialNetwork.FACEBOOK)
                        && Session.getActiveSession().isOpened()){
                    loadFacebookPhoto();
                }
                loadFacebookFriends();
                fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_ico));


            }
        }
    };


}
