package com.netparty.viewers;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.netparty.R;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;

import java.util.ArrayList;
import java.util.Arrays;

public class AccountActivity extends AbstractActivity {


    private UiLifecycleHelper uiHelper;
    private LoginButton faceBookLoginBtn;
    private AccountsAdapter adapter;

    @Override
    protected void onServiceConnected() {
        adapter = new AccountsAdapter(this, getService().getMetaContact().getAccounts());
        ((ListView)findViewById(R.id.accs)).setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_accounts);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(saveInstanceState);

        faceBookLoginBtn = (LoginButton)findViewById(R.id.authButton);
        faceBookLoginBtn.setReadPermissions(Arrays.asList("user_friends"));
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("tag", "state=" + state);

        }
    };

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
    }

    @Override
    protected void onGoogleConnected() {

    }

    @Override
    protected void onGoogleDisconnected() {

    }

    @Override
    protected void onGoogleConnectionFailed() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private class AccountsAdapter extends BaseAdapter {

        Context context;
        ArrayList<Account> list;
        LayoutInflater inflater;
        View view;

        public AccountsAdapter(Context context, ArrayList<Account> list){
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
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            long id = 0;
            try{
                id = Long.valueOf(list.get(position).getId());
            }
            catch (Exception e){
                Log.e("tag", e.getMessage());
            }
            return id;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            view = inflater.inflate(R.layout.simple_account_item, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(list.get(position).getUserName());
            final ImageView icon = (ImageView)view.findViewById(R.id.icon);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_ico));
            }
            else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.google_plus_ico));
            }

            return view;
        }
    }


}
