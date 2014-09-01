package com.netparty.fragments;

import android.app.Service;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.netparty.data.AccountRec;
import com.netparty.data.MetaContactRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.RemoveItemListener;
import com.netparty.services.NPService;
import com.netparty.utils.web.WebUtils;
import com.netparty.viewers.ContactDetailsDialog;
import com.netparty.viewers.MainActivity;
import com.netparty.views.DragDropListView;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Valentin on 17.08.2014.
 */
public class ContactsListFragment extends AbstractFragment {

    private static final int SCROLL_DURATION = 70;
    private static final int SCROLL_DISTANCE = 30;

    private View contentView;

    private DragDropListView friendsLeft, friendsRight;
    private ImageView dragItemView;
    private MetaContactRec dragItem = null;
    private FriendsAdapter adapterLeft, adapterRight;
    private ArrayList<MetaContactRec> friendsList;
    private RelativeLayout mContainer;
    private boolean isScrolling = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_contacts_list, container, false);

        dragItemView = (ImageView)contentView.findViewById(R.id.drag_image);

        mContainer = (RelativeLayout)contentView.findViewById(R.id.container);
        friendsLeft = (DragDropListView)contentView.findViewById(R.id.friends_left);
        friendsRight = (DragDropListView)contentView.findViewById(R.id.friends_right);

        friendsLeft.setDragListener(new DragListener(true));
        friendsRight.setDragListener(new DragListener(false));
        friendsLeft.setOutUpListener(new UpOutListener(true));
        friendsRight.setOutUpListener(new UpOutListener(false));

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

        ScrollStateListener scrollStateListener = new ScrollStateListener();

        friendsLeft.setOnScrollListener(scrollStateListener);
        friendsRight.setOnScrollListener(scrollStateListener);

        friendsLeft.setOnItemClickListener(new FriendsClickListener());
        friendsRight.setOnItemClickListener(new FriendsClickListener());
        friendsLeft.setRemoveItemListener(new RemoveListener());
        friendsRight.setRemoveItemListener(new RemoveListener());

        friendsList = new ArrayList<MetaContactRec>();
        adapterLeft = new FriendsAdapter(getActivity(), friendsList, true);
        adapterRight = new FriendsAdapter(getActivity(), friendsList, false);
        friendsLeft.setAdapter(adapterLeft);
        friendsRight.setAdapter(adapterRight);


        if(((MainActivity)getActivity()).getService() != null) {
            if (((MainActivity)getActivity()).getService().getPlusClient().isConnected())
                loadGoogleFriends();
            loadFacebookFriends();
            loadVKFriends();
        }

        return contentView;
    }

    @Override
    public void onServiceConnected(Service service) {
        if (((NPService)service).getPlusClient().isConnected())
            loadGoogleFriends();
        loadFacebookFriends();
        loadVKFriends();
    }

    private class ScrollStateListener implements AbsListView.OnScrollListener{

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState == SCROLL_STATE_IDLE) isScrolling = false;
            else isScrolling = true;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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
                if(isLeft){
                    dragItemView.setX(event.getX() - dragItemView.getWidth()/2);
                }
                else {
                    dragItemView.setX(event.getX() + dragItemView.getWidth()/2);
                }
                dragItemView.setY(event.getY() - dragItemView.getHeight()/2);

                if(!isScrolling) {
                    if (event.getY() >= 0 &&
                            event.getY() <= 0 + dragItemView.getHeight()) {
                        if (isLeft) {
                            if (event.getX() < mContainer.getWidth() / 2)
                                friendsLeft.smoothScrollBy(-SCROLL_DISTANCE, SCROLL_DURATION);
                            else friendsRight.smoothScrollBy(-SCROLL_DISTANCE, SCROLL_DURATION);
                        } else {
                            if (event.getX() < 0)
                                friendsLeft.smoothScrollBy(-SCROLL_DISTANCE, SCROLL_DURATION);
                            else friendsRight.smoothScrollBy(-SCROLL_DISTANCE, SCROLL_DURATION);
                        }

                    }
                    if (event.getY() <= mContainer.getBottom() &&
                            event.getY() >= mContainer.getBottom() - dragItemView.getHeight()) {
                        if (isLeft) {
                            if (event.getX() < mContainer.getWidth() / 2)
                                friendsLeft.smoothScrollBy(SCROLL_DISTANCE, SCROLL_DURATION);
                            else friendsRight.smoothScrollBy(SCROLL_DISTANCE, SCROLL_DURATION);
                        } else {
                            if (event.getX() < 0)
                                friendsLeft.smoothScrollBy(SCROLL_DISTANCE, SCROLL_DURATION);
                            else friendsRight.smoothScrollBy(SCROLL_DISTANCE, SCROLL_DURATION);
                        }
                    }
                }

            }
            if (event.getAction() == MotionEvent.ACTION_UP){
                dragItemView.setVisibility(View.GONE);
                if(friendsRight.equals(v))friendsRight.receiveItem((int)event.getX(), (int)event.getY(), dragItem);
                else if(friendsLeft.equals(v)) friendsLeft.receiveItem((int)event.getX(), (int)event.getY(), dragItem);
                if(dragItem != null) {
                    dragItem = null;
                }
            }
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

    private class FriendsClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(friendsList.get(position).getAccounts().size()>1) {
                ContactDetailsDialog dialog = new ContactDetailsDialog(getActivity(), friendsList.get(position));
                dialog.show();
            }
        }
    }




    private class RemoveListener implements RemoveItemListener {

        @Override
        public void removeItem(Object item) {
            friendsList.remove(item);
            updateAdapters();
        }
    }

    private void updateAdapters(){
        adapterLeft.notifyDataSetChanged();
        adapterRight.notifyDataSetChanged();
    }

    private class FriendsAdapter extends BaseAdapter {

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
            return 0;
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
                photo.setBackgroundResource(R.drawable.gold_frame);
            }
            name.setText(list.get(position).getUserName());
            final ImageView icon = (ImageView)view.findViewById(R.id.net_icon);
            if(list.get(position).getNet().equals(SocialNetwork.FACEBOOK)) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_circle));
            }
            else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.google_circle));
            }

            //ImageView online = (ImageView)view.findViewById(R.id.online);
            //online.setImageDrawable(getResources().getDrawable(R.drawable.on_line));

            if(list.get(position).getPhoto() != null) {
                photo.setImageBitmap(list.get(position).getPhoto());
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = WebUtils.loadPhoto(list.get(position).getPhotoUrl());
                        getActivity().runOnUiThread(new Runnable() {
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


    public void loadGoogleFriends(){

        ((MainActivity)getActivity()).getService().getPlusClient().loadVisiblePeople(new PlusClient.OnPeopleLoadedListener() {
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



    public void loadFacebookFriends(){
        new Request(
                Session.getActiveSession(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        Log.e("tag", response.toString());
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

    public void loadVKFriends(){
        String vkId = ((MainActivity)getActivity()).getService()
                .getMetaContact().getAccountId(SocialNetwork.VK);

        if(vkId != null) {
            final VKRequest request = VKApi.friends().get(
                    VKParameters.from(VKApiConst.USER_ID, vkId));
            request.addExtraParameter("fields", "first_name,photo_50");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {

                    try {
                        JSONArray friends = response.json.getJSONObject("response").getJSONArray("items");
                        for(int i = 0; i<friends.length(); i++){
                            MetaContactRec mc = new MetaContactRec(false);
                            mc.addAccount(new AccountRec(SocialNetwork.VK,
                                    friends.getJSONObject(i).getString("id"),
                                    friends.getJSONObject(i).getString("first_name") + " "
                                    + friends.getJSONObject(i).getString("last_name"),
                                    friends.getJSONObject(i).getString("photo_50")));
                            friendsList.add(mc);

                        }
                        Log.e("tag", "VK friends" + friends.length());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateAdapters();

                }
            });
        }
    }

    public void addContact(Account account){
        MetaContactRec mc = new MetaContactRec(false);
        mc.addAccount(account);
        friendsList.add(mc);
        updateAdapters();
    }

}
