package com.egesio.test.egesioservices.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.service.RealTimeService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ObtenToken extends AsyncTask<String, Void, String> {

    public Context context;

    public ObtenToken(Context contex){
        this.context = contex;
    }

    private Exception exception;

    protected String doInBackground(String... json) {


        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json[0], JSON);
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("idioma","es")
                    .url(Constans.URL_AUTH)
                    .post(body)
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
                            String token = json.getString("token");
                            Sharedpreferences.getInstance(context).escribeValorString(Constans.TOKEN_KEY, token);
                            new SendDataFirebase(context).execute("{\"action\": \"TOKEN OK  - " + Utils.getHora() + "\"}");
                            if(!Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00").equals("00:00:00:00:00:00") && Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00").length() == 17){
                                if (!Utils.isMyServiceRunning(RealTimeService.class, context)) {
                                    Intent intentGeoRatioMonitoring = new Intent(context, RealTimeService.class);
                                    context.startService(intentGeoRatioMonitoring);
                                }
                            }else{
                                if (Utils.isMyServiceRunning(RealTimeService.class, context)) {
                                    Intent intentGeoRatioMonitoring = new Intent(context, RealTimeService.class);
                                    context.stopService(intentGeoRatioMonitoring);
                                }
                            }
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
