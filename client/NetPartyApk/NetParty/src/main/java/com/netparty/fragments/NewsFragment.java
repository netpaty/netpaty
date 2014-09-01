package com.netparty.fragments;

import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.netparty.R;
import com.netparty.data.AccountRec;
import com.netparty.data.MetaContactRec;
import com.netparty.data.NewsRec;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.News;
import com.netparty.services.NPService;
import com.netparty.viewers.MainActivity;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Valentin on 26.08.2014.
 */
public class NewsFragment extends AbstractFragment {

    private View contentView;
    private ListView newsListView;
    private ArrayList<News> newsList;
    private NewsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_news, container, false);
        newsList = new ArrayList<News>();
        newsListView = (ListView)contentView.findViewById(R.id.news);
        adapter = new NewsAdapter();
        newsListView.setAdapter(adapter);
        NPService service = ((MainActivity)getActivity()).getService();
        if(service != null){
            if(service.getMetaContact().containAccount(SocialNetwork.FACEBOOK)) loadFaceBookNews();
            if(service.getMetaContact().containAccount(SocialNetwork.VK)) loadVKNews();
        }

        return contentView;
    }

    @Override
    public void onServiceConnected(Service service) {

    }

    private void loadFaceBookNews(){
        new Request(
                Session.getActiveSession(),
                "/me/home",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        Log.e("tag", "complete" + response.toString());
                        try {
                            GraphObject go  = response.getGraphObject();
                            if(go != null) {
                                JSONObject jso = go.getInnerJSONObject();
                                JSONArray arr = null;
                                arr = jso.getJSONArray("data");
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject news = arr.getJSONObject(i);
                                    String name = news.getJSONObject("from").getString("name");
                                    String message = "-";
                                    String story = "-";
                                    String type = "-";
                                    try {
                                        message = news.getString("message");
                                    } catch (JSONException e) {

                                    }
                                    try {
                                        story = news.getString("story");
                                    } catch (JSONException e) {

                                    }
                                    try {
                                        type = news.getString("type");
                                    } catch (JSONException e) {

                                    }
                                    String msg = "";
                                    if (message.equals("-")) msg = story;
                                    else msg = message;
                                    newsList.add(new NewsRec(name, type, msg));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
        ).executeAsync();
    }


    private void loadVKNews(){

        final String strUrl = "https://api.vk.com/method/newsfeed.get?fields=first_name&access_token=" + VKSdk.getAccessToken().accessToken;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI(strUrl));
                    HttpResponse response = httpClient.execute(request);
                    String JSONString = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONObject jsResponse = new JSONObject(JSONString).getJSONObject("response");
                    JSONArray newsItems = jsResponse.getJSONArray("items");
                    for(int i = 0; i < newsItems.length(); i++){
                        JSONObject news = newsItems.getJSONObject(i);
                        int srcId = 0;
                        String type = "";
                        String msg = "";
                        try {
                            srcId = news.getInt("source_id");
                        }
                        catch (JSONException e){

                        }
                        try {
                            type = news.getString("type");
                        }
                        catch (JSONException e){

                        }
                        try {
                            msg = news.getString("text");
                        }
                        catch (JSONException e){

                        }
                        newsList.add(new NewsRec(String.valueOf(srcId), type, msg));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }




                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


    private class NewsAdapter extends BaseAdapter{
        LayoutInflater inflater;

        public NewsAdapter(){
            inflater = (LayoutInflater)NewsFragment.this.getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return newsList.size();
        }

        @Override
        public Object getItem(int position) {
            return newsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.news_item, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            TextView type = (TextView)view.findViewById(R.id.type);
            TextView msg = (TextView)view.findViewById(R.id.message);
            name.setText(newsList.get(position).getName());
            type.setText(newsList.get(position).getType());
            msg.setText(newsList.get(position).getMessage());

            return view;
        }
    }
}
