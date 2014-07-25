package com.netparty.services;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.netparty.interfaces.MetaContact;

public class NPService extends Service {

    private final IBinder binder = new LocalBinder();
    private MetaContact metaContact = null;

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

}
