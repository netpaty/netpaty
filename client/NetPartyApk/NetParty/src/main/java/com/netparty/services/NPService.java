package com.netparty.services;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusClient;
import com.netparty.interfaces.MetaContact;

public class NPService extends Service {

    public static final String GOOGLE_EVENT = "google_event";
    public static final String GOOGLE_CONNECT = "google_connect";
    public static final String GOOGLE_DISCONNECT = "google_disconnect";
    public static final String GOOGLE_CONNECTION_FAILED = "google_failed";

    private final IBinder binder = new LocalBinder();
    private MetaContact metaContact = null;

    public PlusClient getPlusClient() {
        return plusClient;
    }

    public GoogleApiClient.OnConnectionFailedListener getGoogleConFailedListener(){
        return googleConFailedListener;
    }

    public GooglePlayServicesClient.ConnectionCallbacks getGoogleCallbacks(){
        return googleCallbacks;
    }

    public ConnectionResult getGoogleConnectionResult(){
        return googleConnectionResult;
    }

    public void setPlusClient(PlusClient plusClient) {
        this.plusClient = plusClient;
    }

    private PlusClient plusClient = null;

    public void setMetaContact(MetaContact metaContact){
        this.metaContact = metaContact;
    }

    public MetaContact getMetaContact(){
        return metaContact;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate(){
        Log.e("tag", "Service create");
        plusClient = new PlusClient.Builder(this, googleCallbacks, googleConFailedListener)
                .setActions("http://schemas.google.com/AddActivity",
                        "http://schemas.google.com/BuyActivity")
                .setScopes(Scopes.PLUS_LOGIN, Scopes.PLUS_ME ,"https://www.googleapis.com/auth/userinfo.email")

                .build();
    }

    @Override
    public void onDestroy(){
        Log.e("tag", "Service Destroid");
    }

    public class LocalBinder extends Binder {
        public NPService getService() {
            return NPService.this;
        }
    }

    private GooglePlayServicesClient.ConnectionCallbacks googleCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            sendEvent(GOOGLE_CONNECT);
        }

        @Override
        public void onDisconnected() {

            sendEvent(GOOGLE_DISCONNECT);
        }
    };

    private GoogleApiClient.OnConnectionFailedListener googleConFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            googleConnectionResult = connectionResult;
            sendEvent(GOOGLE_CONNECTION_FAILED);
        }
    };

    private ConnectionResult googleConnectionResult = null;

    private void sendEvent(String eventName){
        Intent intent = new Intent(GOOGLE_EVENT).putExtra(GOOGLE_EVENT, eventName);
        sendBroadcast(intent);
    }

}
