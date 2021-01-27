package com.egesio.test.egesioservices.procesos;

import android.content.Context;
import android.util.Log;

import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.model.LecturasResponse;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.egesio.test.egesioservices.utils.InternetConnection;
import com.egesio.test.egesioservices.utils.SendDataEgesio;
import com.egesio.test.egesioservices.utils.Sharedpreferences;
import com.egesio.test.egesioservices.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistorialLecturasProcess {
    private Context _ctx;
    private boolean _rUltimasLecturas  = false;
    private boolean _rProximaLecturas  = false;
    private CommandManager manager;

    public HistorialLecturasProcess(Context context){
        _ctx = context;
        manager = CommandManager.getInstance(context);
    }

    public void iniciaProceso(){
        try{
            publicaStoreAndForward();
            if(obtenerUltimasLecturasUsuarioAsync()){
                if(obtenerProximaLecturaAsync()){
                    if(validaFechasProximaLecturaVSAhora()){
                        establecerHoraReloj();
                        prendeTodosLosSensores();
                        prendeMedicionesXHora();
                        obtenHistorialLecturasReloj();
                        Log.d("EGESIO", "ENTRE");
                    }else{
                        Log.d("EGESIO", "Servicio de registro de toma de lecturas online");
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void obtenHistorialLecturasReloj(){
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        try {
                            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String fechaUltimaLectura = Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, ""); //"2021-01-22 01:22:39";//
                            if(!fechaUltimaLectura.equals("")) {
                                Date dateUltimaLectura = sdfDateFormat.parse(fechaUltimaLectura);
                                Long timeInSecsUltimaLectura = dateUltimaLectura.getTime();
                                Utils.lecturasHistoricas.clear();
                                manager.getPullDownSinchronizationData(1, timeInSecsUltimaLectura);
                                Utils.timerLecturasHistoricas = 5;
                                validaArregloLecturasHistoricas();
                            }
                            mTimer.cancel();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void validaArregloLecturasHistoricas(){
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Log.d("EGESIO", "contanto hacuia atras : " + Utils.timerLecturasHistoricas);
                        if(--Utils.timerLecturasHistoricas <= 0){
                            Log.d("EGESIO", "Legue a cero, arreglo listo");
                            procesoSepararLecturasTemperaturas();
                            mTimer.cancel();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, 100, 1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void procesoSepararLecturasTemperaturas(){
        try{
            ArrayList<LecturasRequest> arrHistoricos = (ArrayList<LecturasRequest>)Utils.lecturasHistoricas.clone();
            ArrayList<LecturasRequest> arrHistoricosTemperaturas = new ArrayList<LecturasRequest>();
            ArrayList<LecturasRequest> arrHistoricosOtros = new ArrayList<LecturasRequest>();
            ArrayList<LecturasRequest> arrHistoricosAMandar = new ArrayList<LecturasRequest>();

            for(int i = 0; i < arrHistoricos.size(); i++){
                if(arrHistoricos.get(i).getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_temperatura){
                    if(arrHistoricosTemperaturas.size() == 0) {
                        String fechaTemp = arrHistoricos.get(i).getFecha();
                        String fechaTempInHours = Utils.quitaMinutosAFecha(fechaTemp);
                        LecturasRequest _o = arrHistoricos.get(i);
                        _o.setFecha(fechaTempInHours);
                        arrHistoricosTemperaturas.add(_o);
                    }else{
                        String fechaTemp1 = arrHistoricos.get(i).getFecha();
                        String fechaTempInHours1 = Utils.quitaMinutosAFecha(fechaTemp1);
                        boolean existeRegistro = false;
                        for(int j = 0; j < arrHistoricosTemperaturas.size(); j++){
                            String fechaTemp2 = arrHistoricosTemperaturas.get(j).getFecha();
                            if(fechaTempInHours1.equals(fechaTemp2)){
                                LecturasRequest _o = arrHistoricos.get(i);
                                if(!_o.getValor().equals("35.0")){
                                    arrHistoricosTemperaturas.get(j).setValor(_o.getValor());
                                }
                                existeRegistro = true;
                            }
                        }
                        if(!existeRegistro){
                            LecturasRequest _o = arrHistoricos.get(i);
                            _o.setFecha(fechaTempInHours1);
                            arrHistoricosTemperaturas.add(_o);
                        }
                    }
                }
            }
            for(int i = 0; i < arrHistoricos.size(); i++){
                if(arrHistoricos.get(i).getDispositivo_parametro_id() != Constans.modelo_dispositivo_parametros_temperatura){
                    String fechaOtros = arrHistoricos.get(i).getFecha();
                    for(int j = 0; j < arrHistoricosTemperaturas.size(); j++){
                        String fechaTemp1 = arrHistoricosTemperaturas.get(j).getFecha();
                        String valor = arrHistoricosTemperaturas.get(j).getValor();
                        if(fechaOtros.equals(fechaTemp1) && !valor.equals("35.0")){
                            arrHistoricosOtros.add(arrHistoricos.get(i));
                        }
                    }
                }
            }

            for(int h = 0; h < arrHistoricosOtros.size(); h++){
                LecturasRequest _o = arrHistoricosOtros.get(h);
                if(_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_ritmo_cardiaco){
                    Log.d("EGESIO", "FECHAS A GUARDAR : " + _o.getFecha());
                    Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, _o.getFecha());
                }
                int horasPorMinutos = Utils.delimitarHorasPorMinutosHistorico((Integer.valueOf(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.PERIODO, "60"))/60));
                Long horasPorMinutosInMilliseconnds = Long.valueOf(horasPorMinutos * 60 * 60 * 1000);
                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date2 = new Date(Utils.getFechaInMillSeconds(_o.getFecha()) + horasPorMinutosInMilliseconnds);
                String fechaAVencer = sdfDateFormat.format(date2);
                Long fechaAVencerInMilliseconds = Utils.getFechaInMillSeconds(fechaAVencer);
                Long fechaLecturaHistorico = Utils.getFechaInMillSeconds(_o.getFecha());
                if(fechaLecturaHistorico < fechaAVencerInMilliseconds){
                    arrHistoricosAMandar.add(_o);
                    for(int j = 0; j < arrHistoricosTemperaturas.size(); j++){
                        if(_o.getFecha().equals(arrHistoricosTemperaturas.get(j).getFecha())){
                            if(_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_ritmo_cardiaco) {
                                arrHistoricosAMandar.add(arrHistoricosTemperaturas.get(j));
                                break;
                            }
                        }
                    }
                }
            }
            if(arrHistoricosAMandar.size() > 0) {
                if (InternetConnection.getInstance().validaConexion(_ctx)) {
                    String jsonSend = "";
                    for (int i = 0; i < arrHistoricosAMandar.size(); i++) {
                        jsonSend += arrHistoricosAMandar.get(i).toJSON();
                        String sepComa = ",";
                        if ((i + 1) == arrHistoricosAMandar.size()) {
                            sepComa = "";
                        }
                        jsonSend += sepComa;
                    }
                    //new SendDataEgesio(_ctx).execute("[" + jsonSend+  "]");
                    SendDataEgesio sendDataEgesio = new SendDataEgesio(_ctx);
                    sendDataEgesio.ejecutaLlamadaAsync("[" + jsonSend + "]", arrHistoricosAMandar);
                } else {
                    Utils.storeAndFowardSaverAsync(_ctx, arrHistoricosAMandar);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void prendeMedicionesXHora(){
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.turnOnHourlyMeasurement(1);
                        mTimer.cancel();
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void prendeTodosLosSensores(){
        try{
            Timer mTimerHeart = new Timer(true);
            mTimerHeart.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.heartRateSensor(1);
                        mTimerHeart.cancel();
                    }
                }
            }, 1000, 100);

            Timer mTimerOxygen = new Timer(true);
            mTimerOxygen.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.bloodOxygenSensor(1);
                        mTimerOxygen.cancel();
                    }
                }
            }, 2000, 100);

            Timer mTimerPressure = new Timer(true);
            mTimerPressure.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.bloodPressureSensor(1);
                        mTimerPressure.cancel();
                    }
                }
            }, 3000, 100);


            Timer mTimerTemperature = new Timer(true);
            mTimerTemperature.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.temperatureSensor(1);
                        mTimerTemperature.cancel();
                    }
                }
            }, 4000, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void establecerHoraReloj(){
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager.getDateSetCommand(1);
                        mTimer.cancel();
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void test1(){
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean validaFechasProximaLecturaVSAhora(){
        boolean _r = false;
        try {
            Long timeInSecsProximaLectura = Utils.getFechaInMillSeconds(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_PROXIMA_LECTURA, ""));
            Long timeInSecsNow = Utils.getFechaInMillSeconds(Utils.getHora());
            if(timeInSecsProximaLectura <= timeInSecsNow){
                _r = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }

    public void publicaStoreAndForward(){
        try{
            String _dataStore = Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.REGISTROS_LECTURA, "");
            if(!_dataStore.equals("")){
                if(InternetConnection.getInstance().validaConexion(_ctx)){
                    Utils.storeAndFowardPublisherAsync(_ctx);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean obtenerUltimasLecturasUsuarioAsync(){
         try {
                _rUltimasLecturas = false;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .header("Content-Type","")
                        .header("responseType","")
                        .header("Access-Control-Allow-Methods","")
                        .header("Access-Control-Allow-Origin","")
                        .header("Access-Control-Allow-Credentials","")
                        .header("Authorization", "Bearer " + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.TOKEN_SEND, ""))
                        .header("idioma",Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.IDIOMA_SEND, "es"))
                        .url(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.GATEWAY_URL, "") +
                                "usuarios/informacionUsuario/InformacionUltimasLecturasMovil?" +
                                "informacion_usuario_id=" + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.INFORMACION_USUARIO_ID, "") +
                                "&numero_lecturas=" + 4)
                        .get()
                        .build();

                CountDownLatch countDownLatch = new CountDownLatch(1);
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            countDownLatch.countDown();
                        } else {
                            try {
                                if(response.code() == 200){
                                    Log.d("EGESIO", "ENTRE 200 otenerUltimasLecturasUsuarioAsync");
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
                                    Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);
                                    Log.d("EGESIO", "Fecha ultima lectura = " + fechaUltimaLectura);
                                    _rUltimasLecturas = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e){
                                e.printStackTrace();
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    }
                });
                countDownLatch.await();
            }catch (Exception e){
                e.printStackTrace();
            }
        return _rUltimasLecturas;
    }

    public boolean obtenerProximaLecturaAsync(){
        try{
            _rProximaLecturas = false;
            OkHttpClient client2 = new OkHttpClient();
            Request request2 = new Request.Builder()
                    .header("Content-Type","")
                    .header("responseType","")
                    .header("Access-Control-Allow-Methods","")
                    .header("Access-Control-Allow-Origin","")
                    .header("Access-Control-Allow-Credentials","")
                    .header("Authorization", "Bearer " + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.TOKEN_SEND, ""))
                    .header("idioma",Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.IDIOMA_SEND, "es"))
                    .url(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.TIEMPO_LECTURAS, ""))
                    .get()
                    .build();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            client2.newCall(request2).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    countDownLatch.countDown();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        countDownLatch.countDown();
                    } else {
                        try {
                            if(response.code() == 200) {
                                Log.d("EGESIO", "ENTRE 200 obtenerProximaLecturaAsync");
                                String r = response.body().string();
                                JSONObject json = new JSONObject(r);
                                String responseJson = json.getString("response");
                                JSONArray jsonIfo = new JSONArray(responseJson); //JSONObject(responseJson);
                                String infoJson = jsonIfo.getString(0);
                                JSONObject jsonValores = new JSONObject(infoJson);
                                String _minutos_lectura_global = jsonValores.getString("minutos_lectura_global");
                                Long resultado = Long.valueOf(_minutos_lectura_global) * 60;
                                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PERIODO, String.valueOf(resultado));
                                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date1 = sdfDateFormat.parse(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, ""));
                                long timeInSecs = date1.getTime();
                                Date date2 = new Date(timeInSecs + (resultado * 1000));
                                String fechaProximaLectura = sdfDateFormat.format(date2);
                                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_PROXIMA_LECTURA, fechaProximaLectura);
                                Log.d("EGESIO", "Fecha proxima lectura = " + fechaProximaLectura);
                                Log.d("EGESIO", _minutos_lectura_global);
                                _rProximaLecturas = true;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e){
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
        return _rProximaLecturas;
    }







}
