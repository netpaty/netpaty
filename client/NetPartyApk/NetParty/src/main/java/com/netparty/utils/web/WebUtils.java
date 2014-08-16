package com.netparty.utils.web;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by Valentin on 13.08.2014.
 */
public class WebUtils {
    public static Bitmap loadPhoto(String strUrl) {

        HttpResponse response = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(strUrl));
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            return BitmapFactory.decodeStream(entity.getContent());
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

    public static Bitmap loadPhoto(final String strUrl, final Callable callback) {
        Loader loader = new Loader(strUrl, callback);
        Bitmap bm = null;
        ExecutorService ex= Executors.newCachedThreadPool();
        Future<Bitmap> s= ex.submit(new Loader(strUrl,callback));
        try {
            bm = s.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }


    private static class Loader implements Callable<Bitmap>{

        String url;
        Callable callback;

        public Loader(String url, Callable callback){
            this.url = url;
            this.callback = callback;
        }

        @Override
        public Bitmap call() throws Exception {
            Bitmap bm = loadPhoto(url);
            callback.call();
            return bm;
        }
    }

}
