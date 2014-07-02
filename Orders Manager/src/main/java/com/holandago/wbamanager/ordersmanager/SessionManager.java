package com.holandago.wbamanager.ordersmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

/**
 * Created by maestro on 01/07/14.
 */
public class SessionManager {
    SharedPreferences pref;

    Editor editor;

    Context context;

    int PRIVATE_MODE = 0;
    public static final String PREF_NAME = "LoginPREF";
    public static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USER = "user";
    public static final String KEY_ID = "user_id";


    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String userID){
        editor.putBoolean(IS_LOGIN,true);
        editor.putString(KEY_USER,username);
        editor.putString(KEY_ID,userID);
        editor.commit();
    }

    public HashMap<String,String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String, String>();
        user.put(KEY_USER,pref.getString(KEY_USER,null));
        user.put(KEY_ID,pref.getString(KEY_ID,null));
        return user;
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        //TODO: Add logout here
    }
}
