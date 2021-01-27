package com.egesio.test.egesioservices.utils;

import android.content.Context;
import android.util.Log;

import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.procesos.HistorialLecturasProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SendDataEgesio{

    private Context context;
    private boolean _r = false;

    public SendDataEgesio(Context context){
        this.context = context;
    }

    public void enviaDatosEgesioDB(){

        LecturasRequest          temperatureParam = new LecturasRequest();
        LecturasRequest            heartRateParam = new LecturasRequest();
        LecturasRequest          oxygenationParam = new LecturasRequest();
        LecturasRequest bloodPresionArterialParam = new LecturasRequest();
        ArrayList<LecturasRequest> arrayLecturasRequest = new ArrayList<LecturasRequest>();

        int idPulsera = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.IDPULSERA, "0"));
        String _idioma = Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es");
        String sendSameDate = Utils.getHora();

        temperatureParam.setDispositivo_id(idPulsera);
        temperatureParam.setValor(Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255"));
        temperatureParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_temperatura);
        temperatureParam.setFecha(sendSameDate);
        temperatureParam.setBnd_store_foward(false);
        temperatureParam.setIdioma(_idioma);

        heartRateParam.setDispositivo_id(idPulsera);
        heartRateParam.setValor(Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "255"));
        heartRateParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_ritmo_cardiaco);
        heartRateParam.setFecha(sendSameDate);
        heartRateParam.setBnd_store_foward(false);
        heartRateParam.setIdioma(_idioma);

        oxygenationParam.setDispositivo_id(idPulsera);
        oxygenationParam.setValor(Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255"));
        oxygenationParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_oxigenacion_sangre);
        oxygenationParam.setFecha(sendSameDate);
        oxygenationParam.setBnd_store_foward(false);
        oxygenationParam.setIdioma(_idioma);

        bloodPresionArterialParam.setDispositivo_id(idPulsera);
        bloodPresionArterialParam.setValor(Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255"));
        bloodPresionArterialParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_presion_arterial);
        bloodPresionArterialParam.setFecha(sendSameDate);
        bloodPresionArterialParam.setBnd_store_foward(false);
        bloodPresionArterialParam.setIdioma(_idioma);

        arrayLecturasRequest.add(temperatureParam);
        arrayLecturasRequest.add(heartRateParam);
        arrayLecturasRequest.add(oxygenationParam);
        arrayLecturasRequest.add(bloodPresionArterialParam);

        //Funcionalidad storeAndFowardSaverAsync
        if(InternetConnection.getInstance().validaConexion(context)){
            HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(context);
            historialLecturasProcess.iniciaProceso();
            String json = temperatureParam.toJSON() + "," + heartRateParam.toJSON() + "," + oxygenationParam.toJSON() + "," + bloodPresionArterialParam.toJSON() ;
            //new SendDataEgesio(context).execute("[" + json+  "]");
            if(ejecutaLlamadaAsync("[" + json+  "]", arrayLecturasRequest)) {
                Log.d("EGESIO", "Los datos se enviaron correctamente a BD");
            }else{
                Log.d("EGESIO", "Error al enviar datos a BD");
            }
        }else{
            Utils.storeAndFowardSaverAsync(context, arrayLecturasRequest);
        }

    }

    public boolean ejecutaLlamadaAsync(String jsonString, ArrayList<LecturasRequest> arrayLecturasRequest) {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonString, JSON);
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(context).obtenValorString(Constans.TOKEN_SEND, "0"))
                    .header("idioma",Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url(Sharedpreferences.getInstance(context).obtenValorString(Constans.URL_SERVICE_EGESIO, "0"))
                    .post(body)
                    .build();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new SendDataFirebase(context).execute("{\"action\": \"ERROR: " +  e.getMessage() +  Utils.getHora() + "\"}");
                    e.printStackTrace();
                    countDownLatch.countDown();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        new SendDataFirebase(context).execute("{\"action\": \"ERROR ejecutaLlamadaAsync: " + jsonString + " -- " + response.message() +  Utils.getHora() + "\"}");
                        Utils.storeAndFowardSaverAsync(context, arrayLecturasRequest);
                        countDownLatch.countDown();
                    } else {
                        try{
                            Log.d("Response", response.body().toString());
                            new SendDataFirebase(context).execute("{\"action\": \"ESCRIBIO EN BD ejecutaLlamadaAsync - " + Utils.getHora() + "\"}");
                            _r = true;
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            countDownLatch.countDown();
                        }
                    }
                }
            });
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }
}