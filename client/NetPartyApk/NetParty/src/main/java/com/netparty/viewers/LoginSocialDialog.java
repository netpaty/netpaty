package com.netparty.viewers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netparty.R;
import com.netparty.data.MetaContactRec;
import com.netparty.data.SocialNetAccountRec;
import com.netparty.enums.SocialNetworks;
import com.netparty.interfaces.MetaContact;
import com.netparty.interfaces.SocialNetAccount;
import com.netparty.utils.db.DataBaseAdapter;

import java.util.ArrayList;

public class LoginSocialDialog extends Dialog implements View.OnClickListener {

    final Context context;
    final View contentView;

    final Button faceBook, google, twitter, add, finish;
    final TextView network;
    final EditText eLogin, ePassword;

    SocialNetworks currentNet = SocialNetworks.FACEBOOK;

    SocialNetAccount firstAccount;

    MetaContact existingMC = null;

    ArrayList<SocialNetAccount> accounts;

    String login, password;

    SocialNetAccount account;

    DataBaseAdapter dbAdapter;

    public LoginSocialDialog(Context context, SocialNetAccount firstAccount) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.login_dialog_layout, null, false);
        setContentView(contentView);
        setTitle(R.string.login_dialog_title);

        existingMC = ((LoginActivity)context).getService().getMetaContact();
        this.firstAccount = firstAccount;

        accounts = new ArrayList<SocialNetAccount>();

        dbAdapter = new DataBaseAdapter(context);

        faceBook = (Button)contentView.findViewById(R.id.fb_btn);
        google = (Button)contentView.findViewById(R.id.google_btn);
        twitter = (Button)contentView.findViewById(R.id.tw_btn);
        add = (Button)contentView.findViewById(R.id.add);
        finish = (Button)contentView.findViewById(R.id.finish);

        faceBook.setOnClickListener(this);
        google.setOnClickListener(this);
        twitter.setOnClickListener(this);
        add.setOnClickListener(this);
        finish.setOnClickListener(this);

        faceBook.setSelected(true);

        eLogin = (EditText)contentView.findViewById(R.id.login);
        ePassword = (EditText)contentView.findViewById(R.id.pass);

        network = (TextView)contentView.findViewById(R.id.network);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fb_btn:
                faceBook.setSelected(true);
                google.setSelected(false);
                twitter.setSelected(false);
                network.setText(SocialNetworks.FACEBOOK.getName());
                currentNet = SocialNetworks.FACEBOOK;
                break;
            case R.id.google_btn:
                faceBook.setSelected(false);
                google.setSelected(true);
                twitter.setSelected(false);
                network.setText(SocialNetworks.GOOGLE.getName());
                currentNet = SocialNetworks.GOOGLE;
                break;
            case R.id.tw_btn:
                faceBook.setSelected(false);
                google.setSelected(false);
                twitter.setSelected(true);
                network.setText(SocialNetworks.TWITTER.getName());
                currentNet = SocialNetworks.TWITTER;
                break;
            case R.id.add:
                login = eLogin.getText().toString();
                password = ePassword.getText().toString();
                account = new SocialNetAccountRec(currentNet, login, password);
                if(((LoginActivity)context).loginWithSocialNet(account)){
                    if(findTheSame(account)) break;
                    if(existingMC != null){
                        existingMC.addAccount(account);
                    }
                    else {
                        MetaContact mc = dbAdapter.getMetaContact(account);
                        if(mc != null) {
                            existingMC = mc;
                            existingMC.addAccount(firstAccount);
                        }
                        else{
                           accounts.add(account);
                        }
                    }
                    String msg = String.format(context.getResources().getString(R.string.account_added),
                            currentNet.getName());
                    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
                eLogin.setText("");
                ePassword.setText("");
                break;
            case R.id.finish:
                login = eLogin.getText().toString();
                password = ePassword.getText().toString();
                if(!login.equals("") && !password.equals("")){
                    account = new SocialNetAccountRec(currentNet, login, password);
                    if(((LoginActivity)context).loginWithSocialNet(account)){
                        if(findTheSame(account)) break;
                        if(existingMC != null){
                            existingMC.addAccount(account);
                        }
                        else {
                            MetaContact mc = dbAdapter.getMetaContact(account);
                            if(mc != null) {
                                existingMC = mc;
                                existingMC.addAccount(firstAccount);
                            }
                            else{
                                accounts.add(account);
                            }
                        }
                    }
                    else{
                        eLogin.setText("");
                        ePassword.setText("");
                        break;
                    }
                }
                if(existingMC != null){
                    existingMC.setNotifyFlag(false);
                    dbAdapter.updateMetaContact(existingMC);
                    ((LoginActivity)context).loginNetParty(existingMC);
                    cancel();
                }
                else{
                    MetaContact newMetaContact = new MetaContactRec(false);
                    for(SocialNetAccount a: accounts){
                        newMetaContact.addAccount(a);
                    }
                    newMetaContact.addAccount(firstAccount);
                    dbAdapter.addMetaContact(newMetaContact);
                    ((LoginActivity)context).loginNetParty(newMetaContact);
                    cancel();
                }
                break;
        }

    }

    private boolean findTheSame(SocialNetAccount account){
        for(SocialNetAccount a: accounts) {
            if(a.getId().equals(account.getId())){
                showWarning();
                return true;
            }
        }
        if(existingMC != null){
            for(SocialNetAccount a: existingMC.getAccounts()) {
                if(a.getId().equals(account.getId())){
                    showWarning();
                    return true;
                }
            }
        }
        if(firstAccount.getId().equals(account.getId())){
            showWarning();
            return true;
        }
        return false;
    }

    private void showWarning(){
        new AlertDialog.Builder(context).setTitle(R.string.warning).
                setMessage(R.string.same_acc_added).
                setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        eLogin.setText("");
        ePassword.setText("");
    }
}
