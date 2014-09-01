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
import com.netparty.R;
import com.netparty.interfaces.MetaContact;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.dialogs.VKCaptchaDialog;

public class NPService extends Service {

    public static final String GOOGLE_EVENT = "google_event";
    public static final String GOOGLE_CONNECT = "google_connect";
    public static final String GOOGLE_DISCONNECT = "google_disconnect";
    public static final String GOOGLE_CONNECTION_FAILED = "google_failed";

    public static final String VK_EVENT = "vk_event";
    public static final String VK_TOKEN_EXPIRED = "vk_token_expired";
    public static final String VK_ACCESS_DENIED = "vk_access_denied";
    public static final String VK_RECEIVED_TOKEN = "vk_received_token";
    public static final String VK_ACCEPT_TOKEN = "vk_accept_token";


    public VKAccessToken getVkToken() {
        return vkToken;
    }

    public void setVkToken(VKAccessToken vkToken) {
        this.vkToken = vkToken;
    }

    private VKAccessToken vkToken;

    public VKError getVkError() {
        return vkError;
    }

    public void setVkError(VKError vkError) {
        this.vkError = vkError;
    }

    private VKError vkError;


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
        plusClient = new PlusClient.Builder(this, googleCallbacks, googleConFailedListener)
                .setActions("http://schemas.google.com/AddActivity",
                        "http://schemas.google.com/BuyActivity")
                .setScopes(Scopes.PLUS_LOGIN, Scopes.PLUS_ME ,"https://www.googleapis.com/auth/userinfo.email")
                .build();

        VKSdk.initialize(sdkListener, getString(R.string.vk_app_id));
    }

    private VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.e("tag", "onVKTokenExpired");
            setVkToken(expiredToken);
            sendEvent(VK_TOKEN_EXPIRED, VK_EVENT);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.e("tag", "onVKAccessDenied");
            setVkError(authorizationError);
            sendEvent(VK_ACCESS_DENIED, VK_EVENT);
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.e("tag", "onVKReceiveNewToken");
            setVkToken(newToken);
            sendEvent(VK_RECEIVED_TOKEN, VK_EVENT);


        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            setVkToken(token);
            Log.e("tag", "onVKAcceptUserToken");
            sendEvent(VK_ACCEPT_TOKEN, VK_EVENT);
        }
    };

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
            sendEvent(GOOGLE_CONNECT, GOOGLE_EVENT);
        }

        @Override
        public void onDisconnected() {

            sendEvent(GOOGLE_DISCONNECT, GOOGLE_EVENT);
        }
    };

    private GoogleApiClient.OnConnectionFailedListener googleConFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            googleConnectionResult = connectionResult;
            sendEvent(GOOGLE_CONNECTION_FAILED, GOOGLE_EVENT);
        }
    };

    private ConnectionResult googleConnectionResult = null;

    private void sendEvent(String eventName, String eventType){
        Intent intent = new Intent(eventType).putExtra(eventType, eventName);
        sendBroadcast(intent);
    }

}
