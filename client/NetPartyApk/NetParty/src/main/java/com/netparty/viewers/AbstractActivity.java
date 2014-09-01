package com.netparty.viewers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;


import com.facebook.Session;
import com.netparty.services.NPService;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;


public abstract class AbstractActivity extends FragmentActivity {

    NPService service = null;

    private boolean isActive = false;

    public boolean isActive(){
        return isActive;
    }

    public NPService getService(){
        return service;
    }

    protected void onServiceConnected(){
        registerReceiver(googleEventReceiver, new IntentFilter(NPService.GOOGLE_EVENT));
        registerReceiver(vkEventReceiver, new IntentFilter(NPService.VK_EVENT));
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, NPService.class), connection, Context.BIND_AUTO_CREATE);
        VKUIHelper.onCreate(this);
    }

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            AbstractActivity.this.service = ((NPService.LocalBinder)service).getService();
            AbstractActivity.this.onServiceConnected();
        }

        public void onServiceDisconnected(final ComponentName className) {
            finish();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        VKUIHelper.onActivityResult(requestCode, resultCode, data);

        if(Session.getActiveSession() != null) Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == LoginActivity.REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
            if(getService() != null && getService().getPlusClient() != null) getService().getPlusClient().connect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onPause(){
        isActive = false;
        super.onPause();

    }

    @Override
    protected void onStop(){
        isActive = false;
        super.onStop();

    }

    @Override
    protected void onDestroy(){
        isActive = false;
        super.onDestroy();
        unregisterReceivers();
        unbindService(connection);
    }

    private BroadcastReceiver googleEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(NPService.GOOGLE_EVENT);
            if(event.equals(NPService.GOOGLE_CONNECT)){
                onGoogleConnected();
            }
            else
            if (event.equals(NPService.GOOGLE_DISCONNECT)){
                onGoogleDisconnected();
            }
            else
            if (event.equals(NPService.GOOGLE_CONNECTION_FAILED)){
                onGoogleConnectionFailed();
            }
        }
    };

    private BroadcastReceiver vkEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(NPService.VK_EVENT);
            if(event.equals(NPService.VK_TOKEN_EXPIRED)){
                onVKTokenExpired(getService().getVkToken());
            }
            else
            if (event.equals(NPService.VK_ACCESS_DENIED)){
                onVKAccessDenied(getService().getVkError());
            }
            else
            if (event.equals(NPService.VK_ACCEPT_TOKEN)){
                onVKAcceptUserToken(getService().getVkToken());
            }
            else
            if (event.equals(NPService.VK_RECEIVED_TOKEN)){
                onVKReceiveNewToken(getService().getVkToken());
            }
        }
    };

    protected abstract void onGoogleConnected();

    protected abstract void onGoogleDisconnected();

    protected abstract void onGoogleConnectionFailed();

    protected abstract void onVKTokenExpired(VKAccessToken expiredToken);

    protected abstract void onVKAccessDenied(VKError authorizationError);

    protected abstract void onVKReceiveNewToken(VKAccessToken newToken);

    protected abstract void onVKAcceptUserToken(VKAccessToken token);

    protected void unregisterReceivers(){
        try{
            unregisterReceiver(googleEventReceiver);
            unregisterReceiver(vkEventReceiver);
        }
        catch (IllegalArgumentException e){

        }
    }


}
