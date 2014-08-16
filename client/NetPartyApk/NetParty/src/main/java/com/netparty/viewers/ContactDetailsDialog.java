package com.netparty.viewers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netparty.R;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;
import com.netparty.utils.web.WebUtils;

import java.util.ArrayList;

/**
 * Created by Valentin on 15.08.2014.
 */
public class ContactDetailsDialog extends Dialog {

    private Context context;
    private View contentView;
    private ListView listView;
    private FriendsAdapter adapter;
    private ArrayList<Account> list;

    public ContactDetailsDialog(Context context, MetaContact mc) {
        super(context);
        this.context = context;
        list = mc.getAccounts();
        LayoutInflater inflater = getLayoutInflater();
        contentView = inflater.inflate(R.layout.layout_contact_details, null);
        setContentView(contentView);
        setTitle(R.string.contact_tittle);
        listView = (ListView)contentView.findViewById(R.id.list);
        adapter = new FriendsAdapter();
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private class FriendsAdapter extends BaseAdapter{



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

            View view = getLayoutInflater().inflate(R.layout.simple_friend_item, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            final ImageView photo = (ImageView)view.findViewById(R.id.photo);
            name.setText(list.get(position).getUserName());
            final ImageView icon = (ImageView)view.findViewById(R.id.net_icon);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.facebook_ico));
            }
            else {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.google_plus_ico));
            }
            if(list.get(position).getPhoto() != null) {
                photo.setImageBitmap(list.get(position).getPhoto());
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = WebUtils.loadPhoto(list.get(position).getPhotoUrl());
                        ((AbstractActivity)context).runOnUiThread(new Runnable() {
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
}
