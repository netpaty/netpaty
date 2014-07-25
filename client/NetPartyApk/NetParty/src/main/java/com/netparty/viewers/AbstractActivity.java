package com.netparty.viewers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;


import com.netparty.services.NPService;


public abstract class AbstractActivity extends Activity {

    NPService service = null;

    private boolean isActive = false;

    public boolean isActive(){
        return isActive;
    }

    public NPService getService(){
        return service;
    }

    protected abstract void onServiceConnected();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, NPService.class), connection, Context.BIND_AUTO_CREATE);
        


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
    }
}
