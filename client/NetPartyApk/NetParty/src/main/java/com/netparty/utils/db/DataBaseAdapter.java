package com.netparty.utils.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.netparty.data.AccountRec;
import com.netparty.data.MetaContactRec;
import com.netparty.data.Names;
import com.netparty.enums.SocialNetwork;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.MetaContact;

public class DataBaseAdapter {
    DBHelper helper;
    Context context;

    SQLiteDatabase db;

    public static final String ACCOUNTS_DB = "accounts_db";
    public static final String ACCOUNTS_TABLE = "acc_table";
    public static final String USERS_TABLE = "users_table";


    public DataBaseAdapter(Context context){
        this.context = context;
        helper = new DBHelper(context, ACCOUNTS_DB, null, 1);
        db = helper.getWritableDatabase();
    }

    public MetaContact getMetaContact(String id){
        String selection = Names.DB_FIELD_USER_ID + "='" + id + "'";
        Cursor cursor1 = db.query(USERS_TABLE, new String[]{Names.DB_FIELD_NOTIFICATION_FLAG},selection, null, null, null, null);
        Boolean notificationFlag = false;
        if(cursor1.moveToFirst()){
            int flagColIndex = cursor1.getColumnIndex(Names.DB_FIELD_NOTIFICATION_FLAG);
            if(cursor1.getInt(flagColIndex) == 1) notificationFlag = true;
        }
        else return null;

        MetaContact mc = new MetaContactRec(id, notificationFlag);

        Cursor cursor2 = db.query(ACCOUNTS_TABLE, null, selection, null, null, null, null);
        if(cursor2.moveToFirst()){
            int networkIdColIndex = cursor2.getColumnIndex(Names.DB_FIELD_NETWORK_ID);
            int netTypeColIndex = cursor2.getColumnIndex(Names.DB_FIELD_NETWORK_TYPE);
            int userNameColIndex = cursor2.getColumnIndex(Names.DB_FIELD_USER_NAME);
            do{
                mc.addAccount(new AccountRec(
                        SocialNetwork.fromString(cursor2.getString(netTypeColIndex)),
                        cursor2.getString(networkIdColIndex), cursor2.getString(userNameColIndex)));
            }
            while (cursor2.moveToNext());
        }
        return mc;
    }

    public MetaContact getMetaContact(Account account){

        String[] columns = {Names.DB_FIELD_NETWORK_ID, Names.DB_FIELD_NETWORK_TYPE, Names.DB_FIELD_USER_ID, Names.DB_FIELD_USER_NAME};
        String selection = Names.DB_FIELD_NETWORK_ID + "='" + account.getId() + "'" +
                " AND " + Names.DB_FIELD_NETWORK_TYPE  + "='" + account.getNet().getName() + "'";

        Cursor cursor = db.query(ACCOUNTS_TABLE, columns, selection, null, null, null, null);

        String user_id = null;

        if(cursor.moveToFirst()){
            int userIdColIndex = cursor.getColumnIndex(Names.DB_FIELD_USER_ID);
            user_id = cursor.getString(userIdColIndex);
            cursor.close();
            selection = Names.DB_FIELD_USER_ID + "=" + user_id;
            Cursor cursor1 = db.query(ACCOUNTS_TABLE, null, selection, null, null, null, null);
            Cursor cursor2 = db.query(USERS_TABLE, null, selection, null, null, null, null);
            if(cursor1.moveToFirst() && cursor2.moveToFirst()){
                int notifyColIndex = cursor2.getColumnIndex(Names.DB_FIELD_NOTIFICATION_FLAG);
                boolean flag = true;
                if(cursor2.getInt(notifyColIndex) == 0) flag = false;
                MetaContactRec metaContact = new MetaContactRec(user_id, flag);
                int networkIdColIndex = cursor1.getColumnIndex(Names.DB_FIELD_NETWORK_ID);
                int netTypeColIndex = cursor1.getColumnIndex(Names.DB_FIELD_NETWORK_TYPE);
                int userNameColIndex = cursor1.getColumnIndex(Names.DB_FIELD_USER_NAME);
                do{
                    metaContact.addAccount(new AccountRec(
                            SocialNetwork.fromString(cursor1.getString(netTypeColIndex)),
                            cursor1.getString(networkIdColIndex), cursor1.getString(userNameColIndex)));
                }
                while (cursor1.moveToNext());
                return metaContact;
            }
        }
        return null;
    }

    public void addMetaContact(MetaContact mc){
        ContentValues cv = new ContentValues();
        cv.put(Names.DB_FIELD_NOTIFICATION_FLAG, mc.getNotifyFlag()?1:0);
        long rowID = db.insert(USERS_TABLE, null, cv);
        mc.setId(String.valueOf(rowID));
        for (Account account: mc.getAccounts()){
            cv.clear();
            cv.put(Names.DB_FIELD_USER_ID, String.valueOf(rowID));
            cv.put(Names.DB_FIELD_NETWORK_TYPE, account.getNet().getName());
            cv.put(Names.DB_FIELD_USER_NAME, account.getUserName());
            cv.put(Names.DB_FIELD_NETWORK_ID, account.getId());
            db.insert(ACCOUNTS_TABLE, null, cv);
        }
        //just for testing
        showDB();
    }

    public void updateMetaContact(MetaContact mc){
        ContentValues cv = new ContentValues();
        cv.put(Names.DB_FIELD_NOTIFICATION_FLAG, (mc.getNotifyFlag())?1:0);
        String whereClause = Names.DB_FIELD_USER_ID + " = '" + mc.getId() + "'";
        db.update(USERS_TABLE, cv, whereClause, null);

        Log.e("tag", "size=" + mc.getAccounts().size());

        for (Account account: mc.getAccounts()){
            String[] columns = {Names.DB_FIELD_NETWORK_ID, Names.DB_FIELD_NETWORK_TYPE, Names.DB_FIELD_USER_ID};
            String selection = Names.DB_FIELD_NETWORK_ID + "='" + account.getId() + "'" +
                    " AND " + Names.DB_FIELD_NETWORK_TYPE  + "='" + account.getNet().getName() + "'" +
                    " AND " + Names.DB_FIELD_USER_ID  + "='" + mc.getId() + "'";

            Cursor cursor = db.query(ACCOUNTS_TABLE, columns, selection, null, null, null, null);

            if(!cursor.moveToFirst()){
                cv.clear();
                cv.put(Names.DB_FIELD_USER_ID, String.valueOf(mc.getId()));
                cv.put(Names.DB_FIELD_NETWORK_TYPE, account.getNet().getName());
                cv.put(Names.DB_FIELD_USER_NAME, account.getUserName());
                cv.put(Names.DB_FIELD_NETWORK_ID, account.getId());
                db.insert(ACCOUNTS_TABLE, null, cv);
            }

        }
        //just for testing
        showDB();
    }

    public void updateOrAddMetaContact(MetaContact mc){
        String whereClause = Names.DB_FIELD_USER_ID + " = '" + mc.getId() + "'";
        Cursor c = db.query(USERS_TABLE, null, whereClause, null, null, null, null);
        if(c.moveToFirst()) updateMetaContact(mc);
        else addMetaContact(mc);
    }

    //for testing
    public void showDB(){
        Cursor cursor = db.query(USERS_TABLE, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            Log.e("tag", "USERS_TABLE");
            int userIdColIndex = cursor.getColumnIndex(Names.DB_FIELD_USER_ID);
            int notifyColIndex = cursor.getColumnIndex(Names.DB_FIELD_NOTIFICATION_FLAG);
            do{
                Log.e("tag", "userId: " + cursor.getString(userIdColIndex) +
                " flag: " + cursor.getString(notifyColIndex));
            }
            while (cursor.moveToNext());
        }
        cursor = db.query(ACCOUNTS_TABLE, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            Log.e("tag", "ACCOUNTS_TABLE");
            int userIdColIndex = cursor.getColumnIndex(Names.DB_FIELD_USER_ID);
            int networkIdColIndex = cursor.getColumnIndex(Names.DB_FIELD_NETWORK_ID);
            int loginColIndex = cursor.getColumnIndex(Names.DB_FIELD_USER_NAME);
            int netTypeColIndex = cursor.getColumnIndex(Names.DB_FIELD_NETWORK_TYPE);
            do{
                Log.e("tag", "userId: " + cursor.getString(userIdColIndex) +
                        " NetID: " + cursor.getString(networkIdColIndex) +
                        " name: " + cursor.getString(loginColIndex) +
                        " netType: " + cursor.getString(netTypeColIndex));
            }
            while (cursor.moveToNext());
        }


    }
}
