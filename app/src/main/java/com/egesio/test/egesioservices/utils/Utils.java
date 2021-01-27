package com.egesio.test.egesioservices.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.egesio.test.egesioservices.procesos.HistorialLecturasProcess;
import com.egesio.test.egesioservices.service.RealTimeService;

import static android.content.Context.ACTIVITY_SERVICE;
import static java.lang.Double.isNaN;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Utils {

    public static boolean isServiceStart = false;
    public static boolean isServiceStop = false;
    public static boolean isDeviceConnect = false;
    public static boolean isDeviceDisconnect = false;
    public static boolean isDeviceConnecting = false;
    public static boolean isDeviceDisconnectManual = false;
    public static int timerLecturasHistoricas = 5;
    public static ArrayList<LecturasRequest> lecturasHistoricas = new ArrayList<LecturasRequest>();

    public static boolean isOpenLocationService(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context _ctx) {
        final ActivityManager activityManager = (ActivityManager)_ctx.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

    public static String getHora(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String fecha = dtf.format(now);
        return fecha;
    }

    public static void guardaDato(Context context, String key, String valor){
        try{
            //new SendDataFirebase(context).execute("{\"action\": \"" + key + " - " + valor + " - " + Utils.getHora() + "\"}");
            /*if(key.equals(Constans.TEMPERATURE_KEY) && valor != null){
                Double temperature = Double.valueOf(valor);
                if (temperature != null && !isNaN(temperature) && temperature > 35 && temperature < 43)
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.TEMPERATURE_KEY, String.valueOf(temperature));
            }else if(key.equals(Constans.HEART_KEY) && valor != null){
                Integer heartRate = Integer.valueOf(valor);
                if (heartRate != null && !isNaN(heartRate) && heartRate > 40 && heartRate < 226)
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.HEART_KEY, String.valueOf(heartRate));
            }else if(key.equals(Constans.BLOOD_OXYGEN_KEY) && valor != null){
                Integer bloodOxygen = Integer.valueOf(valor);
                if (bloodOxygen != null && !isNaN(bloodOxygen) && bloodOxygen > 70 && bloodOxygen <= 100)
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
            }else if(key.equals(Constans.BLOOD_PRESSURE_KEY) && valor != null){
                Integer bloodPressureHypertension = Integer.valueOf(valor.split("/")[0]);
                Integer bloodPressureHypotension = Integer.valueOf(valor.split("/")[1]);
                if (bloodPressureHypertension != null && !isNaN(bloodPressureHypertension) &&
                        bloodPressureHypertension > 70 && bloodPressureHypertension < 200 &&
                        !isNaN(bloodPressureHypotension) && bloodPressureHypotension > 40 && bloodPressureHypotension < 130)
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
            }*/

            if(key.equals(Constans.TEMPERATURE_KEY) && valor != null){
                Double temperature = Double.valueOf(valor);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.TEMPERATURE_KEY, String.valueOf(temperature));
            }else if(key.equals(Constans.HEART_KEY) && valor != null){
                Integer heartRate = Integer.valueOf(valor);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.HEART_KEY, String.valueOf(heartRate));
            }else if(key.equals(Constans.BLOOD_OXYGEN_KEY) && valor != null){
                Integer bloodOxygen = Integer.valueOf(valor);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
            }else if(key.equals(Constans.BLOOD_PRESSURE_KEY) && valor != null){
                Integer bloodPressureHypertension = Integer.valueOf(valor.split("/")[0]);
                Integer bloodPressureHypotension = Integer.valueOf(valor.split("/")[1]);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isLecturasCompletas(Context context){
        boolean r = false;
        try{
            int theCountGeneral = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.COUNT_GENERAL, "0"));
            if(theCountGeneral < Constans.LECTURAS){
                Sharedpreferences.getInstance(context).escribeValorString(Constans.COUNT_GENERAL, String.valueOf(theCountGeneral + 1));
            }else{
                r = true;
                Sharedpreferences.getInstance(context).escribeValorString(Constans.COUNT_GENERAL, "0");
            }
        }catch (Exception e){
            new SendDataFirebase(context).execute("{\"action\": \"ERROR  - " + e.getMessage() + " - " + Utils.getHora() + "\"}");
        }
        return r;
    }

    public static boolean isPeriodoCompleto(Context context){
        boolean r = false;
        Long theLastGeneral = Long.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.LAST_TIME_GENERAL, "0"));
        Long generalNow = System.currentTimeMillis();

        Long millseg = generalNow - theLastGeneral;
        //new SendDataFirebase(context).execute("{\"action\": \"TIME: -" + millseg  + "-" +  Utils.getHora() + "\"}");
        Long periodo = Long.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.PERIODO, "0")) * 1000; //30000L; //
        if(millseg > periodo)
            r = true;
        return r;
    }

    public static void enviaDatosEgesioDB(Context context){
        SendDataEgesio sendDataEgesio = new SendDataEgesio(context);
        sendDataEgesio.enviaDatosEgesioDB();
        Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
    }

    public static void validaLecturas(Context context, CommandManager manager){
        if(Utils.isLecturasCompletas(context)) {
            //validaDatos(data, "TEMPERATURE", context);
            new SendDataFirebase(context).execute("{\"action\": \"APAGO TODAS - " + Utils.getHora() + "\"}");
            manager.getOneClickMeasurementCommand(0);

        }else{
            if(Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.COUNT_GENERAL, "0")) == 3){
                new SendDataFirebase(context).execute("{\"action\": \"PRENDO TODAS - " + Utils.getHora() + "\"}");
                manager.getOneClickMeasurementCommand(1);
            }
        }

        if(Utils.isPeriodoCompleto(context)){
            Utils.enviaDatosEgesioDB(context);
            new ValidaPeriodo(context).execute("");
            HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(context);
            historialLecturasProcess.obtenerUltimasLecturasUsuarioAsync();
            historialLecturasProcess.obtenerProximaLecturaAsync();
        }
    }

    public static String obtenLecturaHeartJSON(Context context){
        String r = "";
        try{
            String _heart = Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "0");
            r += "{";
            r += "\"" + Constans.HEART_KEY + "\":" + "\"" + _heart + "\"";
            r += "}";
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
        return r;
    }

    public static String obtenLecturaOxygeJSON(Context context){
        String r = "";
        try{
            String _oxygen = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255");
            r += "{";
            r += "\"" + Constans.BLOOD_OXYGEN_KEY + "\":" + "\"" + _oxygen + "\"";
            r += "}";
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
        return r;
    }

    public static String obtenLecturaPressureJSON(Context context){
        String r = "";
        try{
            String _pressure = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255");
            r += "{";
            r += "\"" + Constans.BLOOD_PRESSURE_KEY + "\":" + "\"" + _pressure + "\"";
            r += "}";
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
        return r;
    }

    public static String obtenLecturaTemperatureJSON(Context context){
        String r = "";
        try{
            String _temperature = Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255");
            r += "{";
            r += "\"" + Constans.TEMPERATURE_KEY + "\":" + "\"" + _temperature + "\"";
            r += "}";
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
        return r;
    }

    public static String obtenTodosValoresJSON(Context context){
        String r = "";
        try{
            String _heart = Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "255");
            String _oxygen = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255");
            String _pressure = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255");
            String _temperature = Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255");
            r += "{";
            r += "\"" + Constans.HEART_KEY + "\":" + "\"" + _heart + "\",";
            r += "\"" + Constans.BLOOD_OXYGEN_KEY + "\":" + "\"" + _oxygen + "\",";
            r += "\"" + Constans.BLOOD_PRESSURE_KEY + "\":" + "\"" + _pressure + "\",";
            r += "\"" + Constans.TEMPERATURE_KEY + "\":" + "\"" + _temperature + "\"";
            r += "}";
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
        return r;
    }

    public static void detenProcesoLectura(Context context){
        try{
            //validaDeviceDisconnect(context, 60000);
            if(Utils.isDeviceConnect){
                Intent intent = new Intent(RealTimeService.ACTION_GATT_DISCONNECTED_ALL);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }else{
                Log.d("Egesio", "Pulsera NO conectada");
            }

        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void iniciaProcesoLectura(Context context){
        try{
            //validaDeviceConnect(context, 60000);
            if(Utils.isServiceStart){
                Intent intent = new Intent(RealTimeService.ACTION_REALTIME_BROADCAST);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }else{
                Log.d("Egesio", "Servicio NO iniciado");
            }

        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void detenServicio(Context context){
        try{
            //validaStopServicio(context, 10000);
            if (Utils.isMyServiceRunning(RealTimeService.class, context)) {
                Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                context.stopService(intentRealTimeMonitoring);
                Utils.isServiceStop = true;
            }
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void iniciaServicio(Context context){
        try{
            //validaIniciaServicio(context, 10000);
            if (!Utils.isMyServiceRunning(RealTimeService.class, context)) {
                Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                context.startService(intentRealTimeMonitoring);
                Utils.isServiceStart = true;
            }
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }


    public static void muestraValoresEgesio(Context context){
        Log.d("EGESIOAV", "token_jwt : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("token_jwt", "N/P"));
        Log.d("EGESIOAV", "pms_gateway_url : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("pms_gateway_url", "N/P"));
        Log.d("EGESIOAV", "post_lecturas_url : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("post_lecturas_url", "N/P"));
        Log.d("EGESIOAV", "instancia_id : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("instancia_id", "N/P"));
        Log.d("EGESIOAV", "dispositivo_id : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("dispositivo_id", "N/P"));
        Log.d("EGESIOAV", "get_tiempo_lecturas_url : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("get_tiempo_lecturas_url", "N/P"));
        Log.d("EGESIOAV", "uuid : " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("uuid", "N/P"));
    }


    public static void guardaValores(Context context){

        String _tokenJWT             = Sharedpreferences.getInstance(context).obtenValorStringEgesio("token_jwt", "");
        String _gatewayURL           = Sharedpreferences.getInstance(context).obtenValorStringEgesio("pms_gateway_url", "");
        String _lecturasURL          = Sharedpreferences.getInstance(context).obtenValorStringEgesio("post_lecturas_url", "");
        String _instanciaID          = Sharedpreferences.getInstance(context).obtenValorStringEgesio("instancia_id", "");
        String _dispositivoID        = Sharedpreferences.getInstance(context).obtenValorStringEgesio("dispositivo_id", "");
        String _tiempoLecturas       = Sharedpreferences.getInstance(context).obtenValorStringEgesio("get_tiempo_lecturas_url", "");
        String _uuidMac              = Sharedpreferences.getInstance(context).obtenValorStringEgesio("uuid", "");
        String _informacionUsuarioId = Sharedpreferences.getInstance(context).obtenValorStringEgesio("usuario_id", "");
        String _idiomaId             = Sharedpreferences.getInstance(context).obtenValorStringEgesio("idioma_id", "");


        Sharedpreferences.getInstance(context).escribeValorString(Constans.MACADDRESS, _uuidMac.replace("\"", ""));
        Sharedpreferences.getInstance(context).escribeValorString(Constans.IDPULSERA, _dispositivoID.replace("\"", ""));
        Sharedpreferences.getInstance(context).escribeValorString(Constans.TIEMPO_LECTURAS, _tiempoLecturas.replace("\"", ""));
        Sharedpreferences.getInstance(context).escribeValorString(Constans.USER_KEY, "adminDev");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.PASSWORD_KEY, "admin123456");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.INFORMACION_USUARIO_ID, _informacionUsuarioId.replace("\"", ""));

        Sharedpreferences.getInstance(context).escribeValorString(Constans.GATEWAY_URL, _gatewayURL.replace("\"", ""));

        Sharedpreferences.getInstance(context).escribeValorString(Constans.TEMPERATURE_KEY, "255");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.HEART_KEY, "255");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_OXYGEN_KEY, "255");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_PRESSURE_KEY, "255");

        Sharedpreferences.getInstance(context).escribeValorString(Constans.COUNT_GENERAL, "0");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, "0");

        Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, "0");
        Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, "0");

        Sharedpreferences.getInstance(context).escribeValorString(Constans.IDIOMA_SEND, _idiomaId.replace("\"", ""));
        Sharedpreferences.getInstance(context).escribeValorString(Constans.TOKEN_SEND, _tokenJWT.replace("\"", ""));
        Sharedpreferences.getInstance(context).escribeValorString(Constans.REFRESH_TOKEN_SEND, "0");

        Sharedpreferences.getInstance(context).escribeValorString(Constans.URL_SERVICE_EGESIO, _lecturasURL.replace("\"", ""));

        //Sharedpreferences.getInstance(context).escribeValorString(Constans.PERIODO, "60");
        new ValidaPeriodo(context).execute("");
    }

    //storeAndFowardSaverAsync
    public static void storeAndFowardSaverAsync(Context context, ArrayList<LecturasRequest> arrayLecturasRequest){
        String _sep = ",";
        String jSONStore = "";
        try {
            for (int i = 0; i < arrayLecturasRequest.size(); i++) {
                if (i == 0 || i % 4 == 0)
                    jSONStore += "{";
                LecturasRequest _o = arrayLecturasRequest.get(i);
                if (_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_temperatura) {
                    jSONStore += _o.getValor() + ",";
                }
                if (_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_ritmo_cardiaco) {
                    jSONStore += _o.getValor() + ",";
                }
                if (_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_oxigenacion_sangre) {
                    jSONStore += _o.getValor() + ",";
                }
                if (_o.getDispositivo_parametro_id() == Constans.modelo_dispositivo_parametros_presion_arterial) {
                    jSONStore += _o.getValor() + ",";
                    jSONStore += _o.getFecha();
                }
                if (i != 0 && i % 3 == 0)
                    jSONStore += "}";
                if ((i + 1) == arrayLecturasRequest.size()) {
                    _sep = "";
                    jSONStore += _sep;
                }
            }
            _sep = "^";
            String stringLectura = Sharedpreferences.getInstance(context).obtenValorString(Constans.REGISTROS_LECTURA, "");
            if (stringLectura == null || stringLectura.equals(""))
                _sep = "";
            stringLectura = stringLectura + _sep + jSONStore;
            Sharedpreferences.getInstance(context).escribeValorString(Constans.REGISTROS_LECTURA, stringLectura);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //storeAndFowardPublisherAsync
    public static void storeAndFowardPublisherAsync(Context context){
        String json = Sharedpreferences.getInstance(context).obtenValorString(Constans.REGISTROS_LECTURA, "");
        String jsonSend = "";
        try {
            if (json != null && !json.equals("")) {
                Log.d("EGESIO", "Valores en storeAndFowardPublisherAsync " + json);
                String[] arrayJson = json.split("^");
                ArrayList<LecturasRequest> arrayLecturasRequest = new ArrayList<LecturasRequest>();
                for (int i = 0; i < arrayJson.length; i++) {
                    String[] jsonValue = arrayJson[i].replace("{", "").replace("}", "").split(",");

                    LecturasRequest temperatureParam = new LecturasRequest();
                    LecturasRequest heartRateParam = new LecturasRequest();
                    LecturasRequest oxygenationParam = new LecturasRequest();
                    LecturasRequest bloodPresionArterialParam = new LecturasRequest();

                    int idPulsera = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.IDPULSERA, "0"));
                    String _idioma = Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es");
                    String sendSameDate = jsonValue[4];

                    temperatureParam.setDispositivo_id(idPulsera);
                    temperatureParam.setValor(jsonValue[0]);
                    temperatureParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_temperatura);
                    temperatureParam.setFecha(sendSameDate);
                    temperatureParam.setBnd_store_foward(true);
                    temperatureParam.setIdioma(_idioma);

                    heartRateParam.setDispositivo_id(idPulsera);
                    heartRateParam.setValor(jsonValue[1]);
                    heartRateParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_ritmo_cardiaco);
                    heartRateParam.setFecha(sendSameDate);
                    heartRateParam.setBnd_store_foward(true);
                    heartRateParam.setIdioma(_idioma);

                    oxygenationParam.setDispositivo_id(idPulsera);
                    oxygenationParam.setValor(jsonValue[2]);
                    oxygenationParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_oxigenacion_sangre);
                    oxygenationParam.setFecha(sendSameDate);
                    oxygenationParam.setBnd_store_foward(true);
                    oxygenationParam.setIdioma(_idioma);

                    bloodPresionArterialParam.setDispositivo_id(idPulsera);
                    bloodPresionArterialParam.setValor(jsonValue[3]);
                    bloodPresionArterialParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_presion_arterial);
                    bloodPresionArterialParam.setFecha(sendSameDate);
                    bloodPresionArterialParam.setBnd_store_foward(true);
                    bloodPresionArterialParam.setIdioma(_idioma);

                    arrayLecturasRequest.add(temperatureParam);
                    arrayLecturasRequest.add(heartRateParam);
                    arrayLecturasRequest.add(oxygenationParam);
                    arrayLecturasRequest.add(bloodPresionArterialParam);

                    jsonSend += temperatureParam.toJSON() + "," + heartRateParam.toJSON() + "," + oxygenationParam.toJSON() + "," + bloodPresionArterialParam.toJSON();
                    String sepComa = ",";
                    if ((i + 1) == arrayJson.length) {
                        sepComa = "";
                    }
                    jsonSend += sepComa;
                }
                //new SendDataEgesio(context).execute("[" + json + "]");
                SendDataEgesio sendDataEgesio = new SendDataEgesio(context);
                sendDataEgesio.ejecutaLlamadaAsync("[" + json + "]", arrayLecturasRequest);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.REGISTROS_LECTURA, "");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getCurrentMeasurement(Context context) {
        MeasurementModel _o = new MeasurementModel();
        try {
            String _tmpBloodOxygen = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255");
            String _tmpHeartRate = Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "255");
            String _tmpTemperature = Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255");
            String _tmpBloodPressure = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255");
            String _tmpNextMeasurementDate = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_PROXIMA_LECTURA, "");
            String _tmpFecha = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, "");

            Integer bloodOxygen = _tmpBloodOxygen.equals("255") ? null : Integer.valueOf(_tmpBloodOxygen);
            Integer heartRate = _tmpHeartRate.equals("255") ? null : Integer.valueOf(_tmpHeartRate);
            Double temperature = _tmpTemperature.equals("255") ? null : Double.valueOf(_tmpTemperature);
            String bloodPressure = _tmpBloodPressure.equals("255") ? null : _tmpBloodPressure;
            String nextMeasurementDate = _tmpNextMeasurementDate.equals("") ? null : _tmpNextMeasurementDate;
            String fecha = _tmpFecha.equals("") ? null : _tmpFecha;

            _o.setBloodOxygen(bloodOxygen);
            _o.setHeartRate(heartRate);
            _o.setTemperature(temperature);
            _o.setBloodPressure(bloodPressure);
            _o.setNextMeasurementDate(nextMeasurementDate);
            _o.setFecha(fecha);
        }catch (Exception e){
            e.printStackTrace();
        }
        return _o.toJSON();
    }

    public static String rellenaConCeros(String _n, int _c) {
        String _r = "";
        try {
            if (_n == null) {
                for (int i = 0; i < _c; i++) {
                    _r += "0";
                }
            } else {
                for (int i = 0; i < (_c - _n.length()); i++) {
                    _r += "0";
                }
                _r = _r + _n;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _r;
    }

    public static Long getFechaInMillSeconds(String fechaAConvertir){
        Long _r = 0L;
        try {
            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (fechaAConvertir != null && !fechaAConvertir.equals("") && fechaAConvertir.length() == 19) {
                Date dateUltimaLectura = sdfDateFormat.parse(fechaAConvertir);
                _r = dateUltimaLectura.getTime();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }

    public static String quitaMinutosAFecha(String fechaAConvertir){
        String _r = "0000-00-00 00:00:00";
        try {
            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (fechaAConvertir != null && !fechaAConvertir.equals("") && fechaAConvertir.length() == 19) {
                String fechaT1 = fechaAConvertir.substring(0, 14);
                String fechaT2 = fechaAConvertir.substring(16, 19);
                _r =  fechaT1 + "00" + fechaT2;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }

    public static int delimitarHorasPorMinutosHistorico(int minutos) {
        int _r = 8;
        try {
            if (minutos < 120) {
                _r = 1;
            } else if (minutos < 180) {
                _r =  2;
            } else if (minutos < 240) {
                _r =  3;
            } else if (minutos < 300) {
                _r =  4;
            } else if (minutos < 360) {
                _r =  5;
            } else if (minutos < 420) {
                _r =  6;
            } else if (minutos < 480) {
                _r =  7;
            } else {
                _r =  8;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }
}