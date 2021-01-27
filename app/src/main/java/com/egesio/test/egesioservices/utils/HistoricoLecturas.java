package com.egesio.test.egesioservices.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasResponse;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoricoLecturas extends AsyncTask<String, Void, String> {

    public Context context;

    public HistoricoLecturas(Context contex){
        this.context = contex;
    }

    protected String doInBackground(String... numLecturas) {

        try {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(context).obtenValorString(Constans.TOKEN_SEND, ""))
                    .header("idioma",Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url(Sharedpreferences.getInstance(context).obtenValorString(Constans.GATEWAY_URL, "") +
                            "usuarios/informacionUsuario/InformacionUltimasLecturasMovil?" +
                            "informacion_usuario_id=945" + //Sharedpreferences.getInstance(context).obtenValorString(Constans.INFORMACION_USUARIO_ID, "") +
                            "&numero_lecturas=" + numLecturas[0])
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new SendDataFirebase(context).execute("{\"action\": \"ERROR HistoricoLecturas: " +  e.getMessage() +  Utils.getHora() + "\"}");
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        new SendDataFirebase(context).execute("{\"action\": \"ERROR RESPONNSE HistoricoLecturas: " + response.message() +  Utils.getHora() + "\"}");
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            if(response.code() == 200){
                                Log.d("EGESIO", "ENTRE");
                                String r = response.body().string();
                                JSONObject json = new JSONObject(r);
                                String responseJson = json.getString("response");
                                Gson gson = new Gson();
                                LecturasResponse[] lecturasResponseArray = gson.fromJson(responseJson, LecturasResponse[].class);
                                List<LecturasResponse> lecturasResponseList =  Arrays.asList(lecturasResponseArray);
                                LecturasResponse lecturaFechaMaxima =  Collections.max(lecturasResponseList, Comparator.comparing(l -> l.getLectura_fecha().replace("T", " ")));
                                List<LecturasResponse> arrLecturasMismaFecha = lecturasResponseList.stream().filter(l -> l.getLectura_fecha().equals(lecturaFechaMaxima.getLectura_fecha())).collect(Collectors.toList());

                                LecturasResponse rTemp = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_temperatura && !l.getLectura_valor().equals("255")).findAny().orElse(null);
                                LecturasResponse rHeart = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_ritmo_cardiaco && !l.getLectura_valor().equals("255")).findAny().orElse(null);
                                LecturasResponse rBloodO = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_oxigenacion_sangre && !l.getLectura_valor().equals("255")).findAny().orElse(null);
                                LecturasResponse rBloodP = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_presion_arterial && !l.getLectura_valor().equals("255")).findAny().orElse(null);

                                MeasurementModel measurementModel = new MeasurementModel();
                                measurementModel.setTemperature((rTemp != null) ? Double.valueOf(rTemp.getLectura_valor()) : null);
                                measurementModel.setTemperature((rHeart != null) ? Double.valueOf(rTemp.getLectura_valor()) : null);
                                measurementModel.setTemperature((rBloodO != null) ? Double.valueOf(rTemp.getLectura_valor()) : null);
                                measurementModel.setTemperature((rBloodP != null) ? Double.valueOf(rTemp.getLectura_valor()) : null);
                                String fechaUltimaLectura = lecturaFechaMaxima.getLectura_fecha().replace("T", " ");
                                measurementModel.setFecha(fechaUltimaLectura);

                                Sharedpreferences.getInstance(context).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);

                                Log.d("EGESIO", "Fecha ultima lectura = " + fechaUltimaLectura);


                                try {

                                    OkHttpClient client2 = new OkHttpClient();
                                    Request request2 = new Request.Builder()
                                            .header("Content-Type","")
                                            .header("responseType","")
                                            .header("Access-Control-Allow-Methods","")
                                            .header("Access-Control-Allow-Origin","")
                                            .header("Access-Control-Allow-Credentials","")
                                            .header("Authorization", "Bearer " + Sharedpreferences.getInstance(context).obtenValorString(Constans.TOKEN_SEND, ""))
                                            .header("idioma",Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es"))
                                            .url(Sharedpreferences.getInstance(context).obtenValorString(Constans.TIEMPO_LECTURAS, ""))
                                            .get()
                                            .build();

                                    //Response response = client.newCall(request).execute();

                                    client2.newCall(request2).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            new SendDataFirebase(context).execute("{\"action\": \"ERROR Historico Periodo: " +  e.getMessage() +  Utils.getHora() + "\"}");
                                            e.printStackTrace();
                                        }
                                        @Override
                                        public void onResponse(Call call, final Response response) throws IOException {
                                            if (!response.isSuccessful()) {
                                                new SendDataFirebase(context).execute("{\"action\": \"ERROR  Historico Periodo: " + response.message() +  Utils.getHora() + "\"}");
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
                                                    Long resultado = Long.valueOf(_minutos_lectura_global) * 60;
                                                    Sharedpreferences.getInstance(context).escribeValorString(Constans.PERIODO, String.valueOf(resultado));


                                                    SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    Date date1 = sdfDateFormat.parse(Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, ""));
                                                    long timeInSecs = date1.getTime();
                                                    Date date2 = new Date(timeInSecs +  (resultado * 1000));
                                                    String fechaProximaLectura = sdfDateFormat.format(date2);

                                                    Sharedpreferences.getInstance(context).escribeValorString(Constans.FECHA_PROXIMA_LECTURA, fechaProximaLectura);

                                                    Log.d("EGESIO", "Fecha proxima lectura = " + fechaProximaLectura);

                                                    Log.d("EGESIO", _minutos_lectura_global);

                                                    //String _o = Utils.getCurrentMeasurement(context);
                                                    //Log.d("EGESIO", _o);

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                } catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }









                               // fechaDateUltimaLectura.





                            }else{
                                Log.d("EGESIO", "Sin valores");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e){
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
