package com.egesio.test.egesioservices.procesos;

import android.content.Context;
import android.util.Log;

import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.model.LecturasResponse;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.egesio.test.egesioservices.utils.InternetConnection;
import com.egesio.test.egesioservices.utils.LogUtil;
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
    private final static String TAG = HistorialLecturasProcess.class.getSimpleName();
    private Context _ctx;
    private boolean _rUltimasLecturas  = false;
    private boolean _rProximaLecturas  = false;
    private CommandManager manager;

    public HistorialLecturasProcess(Context context){
        _ctx = context;
        manager = CommandManager.getInstance(context);
    }

    public void iniciaProceso(int _v){
        try{
            //publicaStoreAndForward();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "true");
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "0");
            if(isHoraLeerHistorico()){
                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "60");
                obtenHistorialLecturasReloj();
            }else{
                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "false");
                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "100");
                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "0");
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public boolean isHoraLeerHistorico(){
        boolean _r = false;
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            obtenerUltimasLecturasUsuarioAsync();
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "10");
            obtenerProximaLecturaAsync();
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "20");
            if(validaFechasProximaLecturaVSAhora()){
                _r = true;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public void obtenHistorialLecturasReloj(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //if(Utils.isDeviceConnect){
                try {
                    SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String fechaUltimaLectura = Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, ""); //"2021-01-22 01:22:39";//
                    if(!fechaUltimaLectura.equals("")) {
                        Date dateUltimaLectura = sdfDateFormat.parse(fechaUltimaLectura);
                        Long timeInSecsUltimaLectura = dateUltimaLectura.getTime();
                        Utils.lecturasHistoricas.clear();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 7 - ");
                        manager.getPullDownSinchronizationData(1, timeInSecsUltimaLectura);
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 8 - ");
                        Utils.timerLecturasHistoricas = 25;
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 9 - ");
                        validaArregloLecturasHistoricas();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 10 - ");
                    }else{
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 10.1 - ");
                        Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "false");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                }
            //}
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "false");
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public void validaArregloLecturasHistoricas(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> 11 - ");
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "contanto hacia atras : " + Utils.timerLecturasHistoricas);
                        if(--Utils.timerLecturasHistoricas <= 0){
                            procesoSepararLecturasTemperaturas();
                            mTimer.cancel();
                        }
                    }catch (Exception e){
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    }
                }
            }, 100, 1000);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "false");
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }



    public void procesoSepararLecturasTemperaturas(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            ArrayList<LecturasRequest> arrHistoricos = (ArrayList<LecturasRequest>)Utils.lecturasHistoricas.clone();
            ArrayList<LecturasRequest> arrHistoricosTemperaturas = new ArrayList<LecturasRequest>();
            ArrayList<LecturasRequest> arrHistoricosOtros = new ArrayList<LecturasRequest>();
            ArrayList<LecturasRequest> arrHistoricosAMandar = new ArrayList<LecturasRequest>();
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "95");
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
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "96");
            for(int i = 0; i < arrHistoricos.size(); i++) {
                if (arrHistoricos.get(i).getDispositivo_parametro_id() != Constans.modelo_dispositivo_parametros_temperatura) {
                    arrHistoricosOtros.add(arrHistoricos.get(i));
                }
            }
            int idPulsera = Integer.valueOf(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.IDPULSERA, "0"));

            for(int i = 0; i < arrHistoricosTemperaturas.size() ; i++){
                LecturasRequest lecturaTemperatura = arrHistoricosTemperaturas.get(i);
                Double temperatureDouble;
                try{
                    temperatureDouble = Double.valueOf(lecturaTemperatura.getValor());
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    temperatureDouble = 0.0;
                }
                if(lecturaTemperatura.getValor() != null && temperatureDouble > 35.0){
                    arrHistoricosAMandar.add(lecturaTemperatura);
                    LecturasRequest lecturaR = Utils.existeFechaIgualEnLectutas(lecturaTemperatura, arrHistoricosOtros, 2);
                    if(lecturaR != null){
                        arrHistoricosAMandar.add(lecturaR);
                    }else{
                        LecturasRequest lecturaRitmoCardiaco = new LecturasRequest();
                        lecturaRitmoCardiaco.setDispositivo_id(idPulsera);
                        lecturaRitmoCardiaco.setValor("255");
                        lecturaRitmoCardiaco.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_ritmo_cardiaco);
                        lecturaRitmoCardiaco.setFecha(lecturaTemperatura.getFecha());
                        lecturaRitmoCardiaco.setBnd_store_foward(false);
                        arrHistoricosAMandar.add(lecturaRitmoCardiaco);
                    }

                    LecturasRequest lecturaO = Utils.existeFechaIgualEnLectutas(lecturaTemperatura, arrHistoricosOtros, 3);
                    if(lecturaO != null){
                        arrHistoricosAMandar.add(lecturaO);
                    }else{
                        LecturasRequest lecturaOxigenacion = new LecturasRequest();
                        lecturaOxigenacion.setDispositivo_id(idPulsera);
                        lecturaOxigenacion.setValor("255");
                        lecturaOxigenacion.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_oxigenacion_sangre);
                        lecturaOxigenacion.setFecha(lecturaTemperatura.getFecha());
                        lecturaOxigenacion.setBnd_store_foward(false);
                        arrHistoricosAMandar.add(lecturaOxigenacion);
                    }

                    LecturasRequest lecturaP = Utils.existeFechaIgualEnLectutas(lecturaTemperatura, arrHistoricosOtros, 4);
                    if(lecturaP != null){
                        arrHistoricosAMandar.add(lecturaP);
                    }else{
                        LecturasRequest lecturaPresion = new LecturasRequest();
                        lecturaPresion.setDispositivo_id(idPulsera);
                        lecturaPresion.setValor("255");
                        lecturaPresion.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_presion_arterial);
                        lecturaPresion.setFecha(lecturaTemperatura.getFecha());
                        lecturaPresion.setBnd_store_foward(false);
                        arrHistoricosAMandar.add(lecturaPresion);
                    }
                }

            }
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "98");

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
                    SendDataEgesio sendDataEgesio = new SendDataEgesio(_ctx);
                    sendDataEgesio.ejecutaLlamadaAsync("[" + jsonSend + "]", arrHistoricosAMandar);
                } else {
                    Utils.storeAndFowardSaverAsync(_ctx, arrHistoricosAMandar);
                }
            }
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "100");
            Utils.iniciaProcesoTramasSuenio(_ctx);

        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }finally {
            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try{
                            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "0");
                            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.ESTA_SINCRONIZANDO, "false");
                        }catch(Exception e){
                        }
                    }
                }, 3000);
            }catch (Exception e){
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            }
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }


    public boolean validaFechasProximaLecturaVSAhora(){
        boolean _r = false;
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "30");
            Long timeInSecsProximaLectura = Utils.getFechaInMillSeconds(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_PROXIMA_LECTURA, "")); //Utils.getFechaInMillSeconds("2020-02-17 00:00:00");//
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "40");
            Long timeInSecsNow = Utils.getFechaInMillSeconds(Utils.getHora());
            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "50");
            //Temporal para leer hourly de sueño
            //Long _2diasAtras = (2 * 24 * 60 * 60 * 1000L);
            //timeInSecsProximaLectura = timeInSecsProximaLectura - _2diasAtras;
            if(timeInSecsProximaLectura <= timeInSecsNow){
                _r = true;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public void publicaStoreAndForward(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            String _dataStore = Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.REGISTROS_LECTURA, "");
            if(!_dataStore.equals("")){
                if(InternetConnection.getInstance().validaConexion(_ctx)){
                    Utils.storeAndFowardPublisherAsync(_ctx);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public boolean obtenerUltimasLecturasUsuarioAsync(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
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
                                //"usuarios/informacionUsuario/InformacionUltimasLecturasMovil?" + //Corporativo
                                "usuarios/informacionUsuario/InformacionUltimasLecturas?" + //ParaTodos
                                "informacion_usuario_id=" + Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.INFORMACION_USUARIO_ID, "") +
                                "&numero_lecturas=" + 4)
                        .get()
                        .build();


                CountDownLatch countDownLatch = new CountDownLatch(1);
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
                        try{
                            String fechaUltimaLectura;
                            String now = Utils.getHora();
                            Long nowInMillisecionds = Utils.getFechaInMillSeconds(now);
                            Long _5diasAtras = (5 * 24 * 60 * 60 * 1000L);
                            Date date2 = new Date(nowInMillisecionds - _5diasAtras);
                            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            fechaUltimaLectura = sdfDateFormat.format(date2);
                            Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);
                        }catch (Exception e2){
                            e2.printStackTrace();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e2.getMessage());
                        }
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            try {
                                String fechaUltimaLectura;
                                String now = Utils.getHora();
                                Long nowInMillisecionds = Utils.getFechaInMillSeconds(now);
                                Long _5diasAtras = (5 * 24 * 60 * 60 * 1000L);
                                Date date2 = new Date(nowInMillisecionds - _5diasAtras);
                                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                fechaUltimaLectura = sdfDateFormat.format(date2);
                                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + response.body().toString());
                            }catch (Exception e){
                                e.printStackTrace();
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                            }finally {
                                countDownLatch.countDown();
                            }
                        } else {
                            try {
                                if(response.code() == 200){
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ENTRE 200 otenerUltimasLecturasUsuarioAsync");
                                    String r = response.body().string();
                                    JSONObject json = new JSONObject(r);
                                    String responseJson = json.getString("response");
                                    Gson gson = new Gson();
                                    LecturasResponse[] lecturasResponseArray = gson.fromJson(responseJson, LecturasResponse[].class);
                                    List<LecturasResponse> lecturasResponseList =  Arrays.asList(lecturasResponseArray);
                                    LecturasResponse lecturaFechaMaxima =  Collections.max(lecturasResponseList, Comparator.comparing(l -> l.getLectura_fecha().replace("T", " ")));
                                    List<LecturasResponse> arrLecturasMismaFecha = lecturasResponseList.stream().filter(l -> l.getLectura_fecha().equals(lecturaFechaMaxima.getLectura_fecha())).collect(Collectors.toList());

                                    LecturasResponse rTemp = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_temperatura /*&& !l.getLectura_valor().equals("255")*/).findAny().orElse(null);
                                    LecturasResponse rHeart = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_ritmo_cardiaco /*&& !l.getLectura_valor().equals("255")*/).findAny().orElse(null);
                                    LecturasResponse rBloodO = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_oxigenacion_sangre /*&& !l.getLectura_valor().equals("255")*/).findAny().orElse(null);
                                    LecturasResponse rBloodP = arrLecturasMismaFecha.stream().filter(l -> Integer.valueOf(l.getParametro_id()) == Constans.modelo_dispositivo_parametros_presion_arterial /*&& !l.getLectura_valor().equals("255")*/).findAny().orElse(null);

                                    MeasurementModel measurementModel = new MeasurementModel();
                                    measurementModel.setTemperature((rTemp != null) ? Double.valueOf(rTemp.getLectura_valor()) : null);
                                    measurementModel.setHeartRate((rHeart != null) ? Integer.valueOf(rHeart.getLectura_valor()) : null);
                                    measurementModel.setBloodOxygen((rBloodO != null) ? Integer.valueOf(rBloodO.getLectura_valor()) : null);
                                    measurementModel.setBloodPressure((rBloodP != null) ? rBloodP.getLectura_valor() : null);
                                    String fechaUltimaLectura = lecturaFechaMaxima.getLectura_fecha().replace("T", " ");
                                    measurementModel.setFecha(fechaUltimaLectura);
                                    Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> Fecha ultima lectura SERVICIO  = " + fechaUltimaLectura);
                                    _rUltimasLecturas = true;
                                }else{
                                    String fechaUltimaLectura;
                                    String now = Utils.getHora();
                                    Long nowInMillisecionds = Utils.getFechaInMillSeconds(now);
                                    Long _5diasAtras = (5 * 24 * 60 * 60 * 1000L);
                                    Date date2 = new Date(nowInMillisecionds - _5diasAtras);
                                    SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    fechaUltimaLectura = sdfDateFormat.format(date2);
                                    Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_ULTIMA_LECTURA, fechaUltimaLectura);
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> Fecha ultima lectura ELSE FORZADA = " + fechaUltimaLectura);
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
                    }
                });
                countDownLatch.await();
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _rUltimasLecturas;
    }

    public boolean obtenerProximaLecturaAsync(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
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
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    countDownLatch.countDown();
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ENTRE 401 obtenerProximaLecturaAsync");
                        countDownLatch.countDown();
                    } else {
                        try {
                            if(response.code() == 200) {
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ENTRE 200 obtenerProximaLecturaAsync");
                                String r = response.body().string();
                                JSONObject json = new JSONObject(r);
                                String responseJson = json.getString("response");
                                JSONArray jsonIfo = new JSONArray(responseJson); //JSONObject(responseJson);
                                String infoJson = jsonIfo.getString(0);
                                JSONObject jsonValores = new JSONObject(infoJson);
                                String _minutos_lectura_global = String.valueOf(jsonValores.getInt("minutos_lectura_global"));
                                try {
                                    String minutos_lectura_personalizada = jsonValores.getString("minutos_lectura_personalizada");
                                    if (minutos_lectura_personalizada != null && !minutos_lectura_personalizada.equals("null")) {
                                        int valor = Integer.valueOf(minutos_lectura_personalizada);
                                        if(valor > 0)
                                            _minutos_lectura_global = minutos_lectura_personalizada;
                                    }
                                }catch (Exception e){
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                                }

                                Long resultado = Long.valueOf(_minutos_lectura_global) * 60;
                                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.PERIODO, String.valueOf(resultado));
                                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date1 = sdfDateFormat.parse(Sharedpreferences.getInstance(_ctx).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, ""));
                                long timeInSecs = date1.getTime();
                                Date date2 = new Date(timeInSecs + (resultado * 1000));
                                String fechaProximaLectura = sdfDateFormat.format(date2);
                                Sharedpreferences.getInstance(_ctx).escribeValorString(Constans.FECHA_PROXIMA_LECTURA, fechaProximaLectura);
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Fecha proxima lectura = " + fechaProximaLectura);
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Minutos Globales = " + _minutos_lectura_global);
                                _rProximaLecturas = true;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                        } catch (Exception e){
                            e.printStackTrace();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                        }finally {
                            countDownLatch.countDown();
                        }
                    }
                }
            });
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _rProximaLecturas;
    }
}
