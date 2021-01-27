package com.egesio.test.egesioservices.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnection {

    private static InternetConnection instance = null;

    private InternetConnection(){

    }

    public static InternetConnection getInstance(){
        if(instance == null){
            instance = new InternetConnection();
        }
        return instance;
    }

    public boolean validaConexion(Context context){
        boolean b  = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            b = true;
        }
        return b;
    }

    public boolean validaConexionBackGround(Context context){
        boolean b  = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            b = true;
        }
        return b;
    }


}