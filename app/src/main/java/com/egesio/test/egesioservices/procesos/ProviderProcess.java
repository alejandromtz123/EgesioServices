package com.egesio.test.egesioservices.procesos;

import android.content.Context;

import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.InformacionUsuarioModel;
import com.egesio.test.egesioservices.model.LecturasResponse;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.egesio.test.egesioservices.utils.LogUtil;
import com.egesio.test.egesioservices.utils.Sharedpreferences;
import com.egesio.test.egesioservices.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProviderProcess {

    private final static String TAG = App.class.getSimpleName();
    private List<LecturasResponse> arrayListLecturas;
    private InformacionUsuarioModel informacionUsuarioModel;

    public List<LecturasResponse> getUltimasLecturasSuenio(Context _ctx){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        arrayListLecturas = new ArrayList<>();
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.TOKEN_SEND, ""))
                    .header("idioma",Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url("http://201.156.230.48:7001/usuarios/informacionUsuario/InformacionUltimasLecturas?informacion_usuario_id=136&numero_lecturas=1&dispositivo_parametro_id=5")
                    .get()
                    .build();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    countDownLatch.countDown();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        if(response.code() == 200){
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ENTRE 200 getUltimasLecturasSuenio");
                            String r = response.body().string();
                            JSONObject json = new JSONObject(r);
                            String responseJson = json.getString("response");
                            Gson gson = new Gson();
                            LecturasResponse[] lecturasResponseArray = gson.fromJson(responseJson, LecturasResponse[].class);
                            arrayListLecturas =  Arrays.asList(lecturasResponseArray);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    } catch (Exception e){
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    } finally {
                        countDownLatch.countDown();
                    }

                }
            });
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return arrayListLecturas;
    }

    public InformacionUsuarioModel getInformacionUsuario(Context _ctx){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        informacionUsuarioModel = new InformacionUsuarioModel();
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.TOKEN_SEND, ""))
                    .header("idioma",Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url("http://201.156.230.48:7001/usuarios/InformacionUsuario/get?informacion_id=1412")
                    .get()
                    .build();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    countDownLatch.countDown();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        if(response.code() == 200){
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ENTRE 200 getInformacionUsuario");
                            String r = response.body().string();
                            JSONObject json = new JSONObject(r);
                            String responseJson = json.getString("response");
                            Gson gson = new Gson();
                            informacionUsuarioModel = (gson.fromJson(responseJson, InformacionUsuarioModel[].class))[0];
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    } catch (Exception e){
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    } finally {
                        countDownLatch.countDown();
                    }

                }
            });
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return informacionUsuarioModel;
    }
}
