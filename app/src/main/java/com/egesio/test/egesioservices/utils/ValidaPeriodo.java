package com.egesio.test.egesioservices.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.egesio.test.egesioservices.constants.Constans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ValidaPeriodo extends AsyncTask<String, Void, String> {

    public Context context;

    public ValidaPeriodo(Context contex){
        this.context = contex;
    }

    protected String doInBackground(String... json) {

        try {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(context).obtenValorString(Constans.TOKEN_SEND,""))
                    .header("idioma",Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url(Sharedpreferences.getInstance(context).obtenValorString(Constans.TIEMPO_LECTURAS, ""))
                    .get()
                    .build();

            //Response response = client.newCall(request).execute();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new SendDataFirebase(context).execute("{\"action\": \"ERROR TOKEN: " +  e.getMessage() +  Utils.getHora() + "\"}");
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        new SendDataFirebase(context).execute("{\"action\": \"ERROR RESPONNSE TOKEN: " + response.message() +  Utils.getHora() + "\"}");
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            String r = response.body().string();
                            JSONObject json = new JSONObject(r);
                            String responseJson = json.getString("response");
                            JSONArray jsonIfo = new JSONArray(responseJson); //JSONObject(responseJson);
                            String infoJson = jsonIfo.getString(0);
                            JSONObject jsonValores = new JSONObject(infoJson);
                            String _minutos_lectura_global = jsonValores.getString("minutos_lectura_global");
                            Integer resultado = Integer.valueOf(_minutos_lectura_global) * 60;
                            Sharedpreferences.getInstance(context).escribeValorString(Constans.PERIODO, String.valueOf(resultado));
                            //Sharedpreferences.getInstance(context).escribeValorString(Constans.TOKEN_KEY, token);
                            Log.d("EGESIO", _minutos_lectura_global);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";

    }

    protected void onPostExecute(String feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }



}
