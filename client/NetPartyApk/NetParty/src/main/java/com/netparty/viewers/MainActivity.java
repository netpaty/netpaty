package com.netparty.viewers;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.netparty.R;
import com.netparty.data.SimpleFriendData;
import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.SocialNetAccount;
import com.netparty.services.NPService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AbstractActivity implements View.OnClickListener {


    ListView friends;
    ImageView fbIcon, googleIcon;
    Button signOut;

    FriendsAdapter adapter;

    ArrayList<SimpleFriendData> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);



        findViewById(R.id.accounts).setOnClickListener(this);

        friends = (ListView)findViewById(R.id.friends);

        fbIcon = (ImageView)findViewById(R.id.fb_icon);
        googleIcon = (ImageView)findViewById(R.id.google_icon);

        signOut = (Button)findViewById(R.id.out);

        signOut.setOnClickListener(this);

        friendsList = new ArrayList<SimpleFriendData>();
        adapter = new FriendsAdapter(this, friendsList);
        friends.setAdapter(adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(googleEventReceiver);
    }

    private BroadcastReceiver googleEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(NPService.GOOGLE_EVENT);
            if(event.equals(NPService.GOOGLE_CONNECT)){
                Log.e("tag", "connect? " + getService().getPlusClient().isConnected());
                if(getService().getMetaContact().hasGoogleAccount()){
                    googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_plus_ico));
                    loadGoogleFriends();
                }
            }
            else
            if (event.equals(NPService.GOOGLE_DISCONNECT)){

            }
            else
            if (event.equals(NPService.GOOGLE_CONNECTION_FAILED)){
                ConnectionResult mConnectionResult = null;
                if(getService()!=null) mConnectionResult = getService().getGoogleConnectionResult();
                if(mConnectionResult != null) {
                    Log.e("tag", "FAILED " + mConnectionResult.toString());
                    try {
                        Log.e("tag", "startResolutionForResult");
                        mConnectionResult.startResolutionForResult(MainActivity.this, LoginActivity.REQUEST_CODE_RESOLVE_ERR);

                    } catch (IntentSender.SendIntentException e) {

                        if(getService() != null) getService().getPlusClient().connect();
                    }
                }
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        registerReceiver(googleEventReceiver, new IntentFilter(NPService.GOOGLE_EVENT));

        if(getService().getMetaContact().hasGoogleAccount() && getService().getPlusClient() != null){
            if(!getService().getPlusClient().isConnected()) getService().getPlusClient().connect();

        }

        ArrayList<SocialNetAccount> accounts = getService().getMetaContact().getAccounts();
        for(SocialNetAccount account: accounts){
            if(account.getNet().equals(SocialNetwork.FACEBOOK)){
                if(Session.getActiveSession() != null && Session.getActiveSession().isOpened()){
                    fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_ico));
                }
                else fbIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_gray));
            }
            if(account.getNet().equals(SocialNetwork.GOOGLE)){

                if(getService().getPlusClient().isConnected()){
                    googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_plus_ico));
                }
                else googleIcon.setImageDrawable(getResources().getDrawable(R.drawable.google_plus_gray));
            }
        }


        if(getService().getPlusClient().isConnected()) loadGoogleFriends();
        loadFacebookFriends();



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
                            friendsList.add(new SimpleFriendData(SocialNetwork.GOOGLE, p.getId(), p.getDisplayName(), p.getImage().getUrl()));
                        }
                    } finally {
                        persons.close();
                        adapter.notifyDataSetChanged();
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
                       // Log.e("tag", response.toString());

                        try {
                            GraphObject go  = response.getGraphObject();
                            JSONObject jso = go.getInnerJSONObject();
                            JSONArray arr = jso.getJSONArray( "data" );
                            Log.e("tag", "arrLength = " + arr.length());
                            for ( int i = 0; i < arr.length() ; i++ )
                            {
                                JSONObject json_obj = arr.getJSONObject( i );
                                final String id = json_obj.getString("id");
                                final String name = json_obj.getString("name");
                                final String url = json_obj.getJSONObject("picture").getJSONObject("data").getString("url");
                                friendsList.add(new SimpleFriendData(SocialNetwork.FACEBOOK, id, name, url));
                            }
                        }
                        catch ( Throwable t )
                        {
                            t.printStackTrace();
                            Log.e("tag", "catch" + t.getMessage());
                        }
                        adapter.notifyDataSetChanged();

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
        ArrayList<SimpleFriendData> list;
        LayoutInflater inflater;
        View view;

        public FriendsAdapter(Context context, ArrayList<SimpleFriendData> list){
            this.context = context;
            this.list = list;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return list.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            view = inflater.inflate(R.layout.simple_friend_item, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            final ImageView photo = (ImageView)view.findViewById(R.id.photo);
            name.setText(list.get(position).getName());
            final ImageView icon = (ImageView)view.findViewById(R.id.net_icon);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_ico));
            }
            else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.google_plus_ico));
            }
            if(list.get(position).getPhoto() != null) {
                photo.setImageBitmap(list.get(position).getPhoto());
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = BitmapFactory.decodeStream(HttpRequest(list.get(position).getPhotoUrl()));
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


    public static InputStream HttpRequest(String strUrl) {

        HttpResponse response = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(strUrl));
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            return entity.getContent();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
