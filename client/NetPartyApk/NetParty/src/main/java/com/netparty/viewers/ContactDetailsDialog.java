package com.netparty.viewers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netparty.R;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;
import com.netparty.interfaces.RemoveItemListener;
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
    private FullScreenTransparentDialog dialog;
    private ArrayList<Account> list;
    private Account dragItem;

    public ContactDetailsDialog(final Context context, MetaContact mc) {
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
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listView.onTouchEvent(event);
                if(dialog != null) dialog.moveItem(event);
                return true;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dragItem = list.get(position);
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
                Rect listRect = new Rect(listView.getLeft(), listView.getTop(),
                        listView.getRight(), listView.getBottom());
                dialog = new FullScreenTransparentDialog(context, bitmap, listRect);
                dialog.setListener(new RemoveItemListener() {
                    @Override
                    public void removeItem(Object item) {
                        list.remove(dragItem);
                        if(list.size() < 2) cancel();
                        ((MainActivity)context).addContact(dragItem);
                        adapter.notifyDataSetChanged();

                    }
                });
                dialog.show();
                return true;
            }
        });
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
            icon.setVisibility(View.VISIBLE);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.facebook_circle));
            }
            else {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.google_circle));
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
