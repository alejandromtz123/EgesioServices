package com.egesio.test.egesioservices.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.egesio.test.egesioservices.adapter.DeviceListAdapter;
import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.bean.DeviceBean;
import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.InformacionUsuarioModel;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.model.LecturasResponse;
import com.egesio.test.egesioservices.model.MeasurementModel;
import com.egesio.test.egesioservices.model.SuenioModelRequest;
import com.egesio.test.egesioservices.model.SuenoModelResponse;
import com.egesio.test.egesioservices.procesos.HistorialLecturasProcess;
import com.egesio.test.egesioservices.procesos.ProviderProcess;
import com.egesio.test.egesioservices.service.BluetoothLeService;
import com.egesio.test.egesioservices.service.RealTimeService;

import static android.content.Context.ACTIVITY_SERVICE;
import static java.lang.Double.isNaN;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import okhttp3.internal.Util;


public class Utils {

    private final static String TAG = App.class.getSimpleName();
    public static boolean isServiceStart = false;
    public static boolean isServiceStop = false;
    public static boolean isDeviceConnect = false;
    public static boolean isDeviceDisconnect = false;
    public static boolean isDeviceConnecting = false;
    public static boolean isDeviceDisconnectManual = false;
    public static int timerLecturasHistoricas = 25;
    public static int timerLecturasSuenio = 25;
    public static int timerSensorInactive = 10;
    public static ArrayList<LecturasRequest> lecturasHistoricas = new ArrayList<>();
    public static ArrayList<Map<Long, String>> lecturasLogFirebase = new ArrayList<>();
    public static ArrayList<SuenoModelResponse> lecturasSuenio = new ArrayList<>();
    private static CommandManager manager;
    public static Context myContext;
    public static String ultimaLecturaSuenio;

    private static DeviceListAdapter mLeDeviceListAdapter;
    private static BluetoothAdapter bluetoothAdapter;
    private static LocationManager locationManager;

    private static ArrayList<DeviceBean> deviceBeens;
    private static Handler mHandler = new Handler();
    private static boolean mScanning;
    private static final long SCAN_PERIOD = 10000;
    private static Runnable runnable;



    public static boolean isOpenLocationService(Context context) {
        boolean r = false;
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps || network) {
                r = true;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context _ctx) {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            final ActivityManager activityManager = (ActivityManager) _ctx.getSystemService(ACTIVITY_SERVICE);
            final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

            for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
                if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                    return true;
                }
            }
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return false;
    }

    public static String getNombreMetodo(){
        //Retorna el nombre del metodo desde el cual se hace el llamado
        return new Exception().getStackTrace()[1].getMethodName();
    }

    public static String getHora(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        String fecha = "";
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            fecha = dtf.format(now);
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return fecha;
    }

    public static void guardaDato(Context context, String key, String valor){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //new SendDataFirebase(context).execute("{\"action\": \"" + key + " - " + valor + " - " + Utils.getHora() + "\"}");

            if(key.equals(Constans.TEMPERATURE_KEY) && valor != null){
                Double temperature = Double.valueOf(valor);
                if (temperature != null && !isNaN(temperature) && temperature > 35 && temperature < 43){
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.TEMPERATURE_KEY, String.valueOf(temperature));
                }else{
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.TEMPERATURE_KEY, "255");
                }
            }else if(key.equals(Constans.HEART_KEY) && valor != null){
                Integer heartRate = Integer.valueOf(valor);
                if (heartRate != null && !isNaN(heartRate) && heartRate > 40 && heartRate < 226){
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.HEART_KEY, String.valueOf(heartRate));
                }else{
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.HEART_KEY, "255");
                }
            }else if(key.equals(Constans.BLOOD_OXYGEN_KEY) && valor != null){
                Integer bloodOxygen = Integer.valueOf(valor);
                if (bloodOxygen != null && !isNaN(bloodOxygen) && bloodOxygen > 70 && bloodOxygen <= 100){
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
                }else{
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_OXYGEN_KEY, "255");
                }
            }else if(key.equals(Constans.BLOOD_PRESSURE_KEY) && valor != null){
                Integer bloodPressureHypertension = Integer.valueOf(valor.split("/")[0]);
                Integer bloodPressureHypotension = Integer.valueOf(valor.split("/")[1]);
                if (bloodPressureHypertension != null && !isNaN(bloodPressureHypertension) &&
                        bloodPressureHypertension > 70 && bloodPressureHypertension < 200 &&
                        !isNaN(bloodPressureHypotension) && bloodPressureHypotension > 40 && bloodPressureHypotension < 130){
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
                }else{
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.BLOOD_PRESSURE_KEY, "255");
                }
            }

            /*
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
            */

        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static boolean isLecturasCompletas(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean r = false;
        try{
            int theCountGeneral = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.COUNT_GENERAL, "0"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Contador de lecturas - " + theCountGeneral);
            if(theCountGeneral < Constans.LECTURAS){
                Sharedpreferences.getInstance(context).escribeValorString(Constans.COUNT_GENERAL, String.valueOf(theCountGeneral + 1));
            }else{
                r = true;
                Sharedpreferences.getInstance(context).escribeValorString(Constans.COUNT_GENERAL, "0");
            }
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ERROR - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static boolean isPeriodoCompleto(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean r = false;
        try{
            Long theLastGeneral = Long.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.LAST_TIME_GENERAL, "0"));
            Long generalNow = System.currentTimeMillis();

            Long millseg = generalNow - theLastGeneral;
            Long periodo = Long.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.PERIODO, "0")) * 1000; //30000L; //
            if(millseg > periodo)
                r = true;
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static void enviaDatosEgesioDB(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            SendDataEgesio sendDataEgesio = new SendDataEgesio(context);
            sendDataEgesio.enviaDatosEgesioDB();
            Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static void validaLecturas(Context context, CommandManager manager, int i){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{

            String primeraVez = Sharedpreferences.getInstance(context).obtenValorString(Constans.INICIA_PROCESO, "1");
            if(Utils.isLecturasCompletas(context) || primeraVez.equals("1")) {
                //validaDatos(data, "TEMPERATURE", context);
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "APAGO TODAS -");
                manager.getOneClickMeasurementCommand(0);






                HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(context);
                historialLecturasProcess.obtenerProximaLecturaAsync();
                historialLecturasProcess.obtenerUltimasLecturasUsuarioAsync();
                String fechaUltimaLectura = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, "");
                String fechaProximaLectura = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_PROXIMA_LECTURA, "");
                Long fechaProximaLecturaInMill = Utils.getFechaInMillSeconds(fechaProximaLectura);
                Long fechaUltimaLecturaInMil = Utils.getFechaInMillSeconds(fechaUltimaLectura);
                String periodio = Sharedpreferences.getInstance(context).obtenValorString(Constans.PERIODO, "3600");
                Long periodioInSec = Long.valueOf(periodio) * 1000;
                String now = Utils.getHora();
                Long nowInMill = Utils.getFechaInMillSeconds(now);
                if (Sharedpreferences.getInstance(context).obtenValorString(Constans.ESTA_SINCRONIZANDO, "false").equals("false")) {
                    //int intentos = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.INTENTOS_HIST, "0"));
                    if ((fechaUltimaLecturaInMil + periodioInSec + 3900000) < nowInMill /*&& intentos < 1*/) {
                        //Log.d(TAG, "-->> intentos : " + intentos);
                        Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "0");
                        //Sharedpreferences.getInstance(context).escribeValorString(Constans.INTENTOS_HIST, "" + (++intentos));
                        historialLecturasProcess.iniciaProceso(1);
                    } /*else {*/
                    //Sharedpreferences.getInstance(context).escribeValorString(Constans.INTENTOS_HIST, "0");
                    //Log.d(TAG, "-->> intentos ELSE: " + intentos);
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> FECHAS: fechaProximaLecturaInMill < nowInMill " + (fechaProximaLecturaInMill < nowInMill));
                    if (fechaProximaLecturaInMill < nowInMill) {
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> FECHAS: fechaProximaLecturaInMill < nowInMill Entre");
                        if (Sharedpreferences.getInstance(context).obtenValorString(Constans.ENVIADO_DATOS, "false").equals("false")) {
                            String candado = Sharedpreferences.getInstance(context).obtenValorString(Constans.CANDADO,String.valueOf(Utils.getFechaInMillSeconds(Utils.getHora())));
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> candado : " + candado);
                            Long candadoInMill = Long.valueOf(candado) + 60000;
                            //if(candadoInMill < Utils.getFechaInMillSeconds(Utils.getHora())) {
                            try {
                                Sharedpreferences.getInstance(context).escribeValorString(Constans.ENVIADO_DATOS, "true");
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> Entre candado : " + candado);
                                Utils.enviaDatosEgesioDB(context);
                                historialLecturasProcess.obtenerProximaLecturaAsync();
                                historialLecturasProcess.obtenerUltimasLecturasUsuarioAsync();
                                Sharedpreferences.getInstance(context).escribeValorString(Constans.CANDADO, String.valueOf(Utils.getFechaInMillSeconds(Utils.getHora())));
                            }catch (Exception e){
                                e.printStackTrace();
                            }finally {
                                Sharedpreferences.getInstance(context).escribeValorString(Constans.ENVIADO_DATOS, "false");
                            }
                            //}



                        }
                        /*}*/
                    }
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_PROCESO, "0");
                }





            }else{
                if(Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.COUNT_GENERAL, "0")) == 3){
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "PRENDO TODAS - ");
                    manager.getOneClickMeasurementCommand(1);
                }
            }
            /*if(Utils.isPeriodoCompleto(context)){
                Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
                HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(context);
                //if(Sharedpreferences.getInstance(context).obtenValorString(Constans.INICIA_SERVICIO_MANUAL, "0").equals("1")) {
                    Utils.enviaDatosEgesioDB(context);
                    if(historialLecturasProcess.isHoraLeerHistorico()){
                        Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "0");
                        historialLecturasProcess.iniciaProceso(1);
                    }
                    historialLecturasProcess.obtenerUltimasLecturasUsuarioAsync();
                    historialLecturasProcess.obtenerProximaLecturaAsync();
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, "0");
                //}
            }*/



        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static String obtenLecturaHeartJSON(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        String r = "";
        try{
            String _heart = Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "0");
            r += "{";
            r += "\"" + Constans.HEART_KEY + "\":" + "\"" + _heart + "\"";
            r += "}";
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static String obtenLecturaOxygeJSON(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        String r = "";
        try{
            String _oxygen = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255");
            r += "{";
            r += "\"" + Constans.BLOOD_OXYGEN_KEY + "\":" + "\"" + _oxygen + "\"";
            r += "}";
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static String obtenLecturaPressureJSON(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        String r = "";
        try{
            String _pressure = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255");
            r += "{";
            r += "\"" + Constans.BLOOD_PRESSURE_KEY + "\":" + "\"" + _pressure + "\"";
            r += "}";
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static String obtenLecturaTemperatureJSON(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        String r = "";
        try{
            String _temperature = Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255");
            r += "{";
            r += "\"" + Constans.TEMPERATURE_KEY + "\":" + "\"" + _temperature + "\"";
            r += "}";
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static String obtenTodosValoresJSON(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
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
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static void detenProcesoLectura(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //validaDeviceDisconnect(context, 60000);
            if(Utils.isDeviceConnect){
                Intent intent = new Intent(RealTimeService.ACTION_GATT_DISCONNECTED_ALL);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }else{
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Pulsera NO conectada");
            }

        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static void iniciaProcesoLectura(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //validaDeviceConnect(context, 60000);
            if(Utils.isServiceStart){
                Intent intent = new Intent(RealTimeService.ACTION_REALTIME_BROADCAST);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }else{
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Servicio NO iniciado");
            }

        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static void detenServicio(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Sharedpreferences.getInstance(context).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "1");
            if (Utils.isMyServiceRunning(RealTimeService.class, context)) {
                Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                context.stopService(intentRealTimeMonitoring);
                Utils.isServiceStop = true;
                if(Utils.isDeviceConnect){
                    Intent intent = new Intent(RealTimeService.ACTION_GATT_DISCONNECTED_ALL);
                    LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                }else{
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Pulsera NO conectada");
                }
            }
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }




    public static void iniciaServicio(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Utils.isDeviceDisconnect = true;
            Utils.isDeviceConnect = false;
            Utils.isDeviceConnecting = false;
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try{
                        if (Utils.isMyServiceRunning(RealTimeService.class, context) && Utils.isDeviceConnect) { //Se esta ejecutado, NO
                            mTimer.cancel();
                        }else{
                            if (!Utils.isMyServiceRunning(RealTimeService.class, context)) {
                                Sharedpreferences.getInstance(context).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
                                Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "1");
                                Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                                context.startService(intentRealTimeMonitoring);
                                Utils.isServiceStart = true;
                            }else{
                                detenServicio(context);
                                Timer mTimer = new Timer(true);
                                mTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        try{
                                            if (!Utils.isMyServiceRunning(RealTimeService.class, context)) {
                                                Sharedpreferences.getInstance(context).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
                                                Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "1");
                                                Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                                                context.startService(intentRealTimeMonitoring);
                                                Utils.isServiceStart = true;
                                            }
                                        }catch(Exception e){
                                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Error en el ciclo de inicio de iniciaServicio run() - 2");
                                            e.printStackTrace();
                                        }
                                    }
                                }, 1000);
                            }
                        }
                    }catch(Exception e){
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Error en el ciclo de inicio de iniciaServicio run() - 1");
                        e.printStackTrace();
                    }
                }
            }, 100, 40000);


            Utils.isServiceStart = true;

            try{
                Timer mTimerMediciones = new Timer(true);
                mTimerMediciones.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String _o = Utils.getCurrentMeasurement(context);
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Mediciones - " + _o);
                    }
                }, 100, 5000);
            }catch (Exception e){
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
            }


            try{
                Timer mTimerEstatus = new Timer(true);
                mTimerEstatus.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String respuesta = "disconnected";
                        try{
                            if(Utils.isDeviceDisconnect){
                                respuesta = "disconnected";
                            }else if(Utils.isDeviceConnect){
                                respuesta = "connected";
                            }else if(Utils.isDeviceConnecting){
                                respuesta = "connecting";
                            }
                        }catch(Exception e){
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
                        }
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Estatus Pulsera - " + respuesta);
                    }
                }, 100, 5000);
            }catch (Exception e){
                Log.d("Egesio", e.getMessage());
            }

            try{
                Timer mTimerPorcentaje = new Timer(true);
                mTimerPorcentaje.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try{
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Porcentaje - " + Utils.obtenerProgresoSincronizacion(context));
                        }catch(Exception e){
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
                        }
                    }
                }, 100, 5000);
            }catch (Exception e){
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
            }


        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Error en el ciclo de inicio de iniciaServicio");
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }


    public static void muestraValoresEgesio(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - token_jwt = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("token_jwt", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - pms_gateway_url = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("pms_gateway_url", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - post_lecturas_url = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("post_lecturas_url", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - instancia_id = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("instancia_id", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - dispositivo_id = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("dispositivo_id", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - get_tiempo_lecturas_url = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("get_tiempo_lecturas_url", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - uuid = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("uuid", "N/P"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - usuario_informacion_id = " + Sharedpreferences.getInstance(context).obtenValorStringEgesio("usuario_info_android_id", "N/P"));
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }


    public static void guardaValores(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            String _tokenJWT             = Sharedpreferences.getInstance(context).obtenValorStringEgesio("token_jwt", "");
            String _gatewayURL           = Sharedpreferences.getInstance(context).obtenValorStringEgesio("pms_gateway_url", "");
            String _lecturasURL          = Sharedpreferences.getInstance(context).obtenValorStringEgesio("post_lecturas_url", "");
            String _instanciaID          = Sharedpreferences.getInstance(context).obtenValorStringEgesio("instancia_id", "");
            String _dispositivoID        = Sharedpreferences.getInstance(context).obtenValorStringEgesio("dispositivo_id", "");
            String _tiempoLecturas       = Sharedpreferences.getInstance(context).obtenValorStringEgesio("get_tiempo_lecturas_url", "");
            String _uuidMac              = Sharedpreferences.getInstance(context).obtenValorStringEgesio("uuid", "");
            String _informacionUsuarioId = Sharedpreferences.getInstance(context).obtenValorStringEgesio("usuario_info_android_id", "");
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
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    //storeAndFowardSaverAsync
    public static void storeAndFowardSaverAsync(Context context, ArrayList<LecturasRequest> arrayLecturasRequest){
        String _sep = ",";
        String jSONStore = "";
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
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
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    //storeAndFowardPublisherAsync
    public static void storeAndFowardPublisherAsync(Context context){
        String json = Sharedpreferences.getInstance(context).obtenValorString(Constans.REGISTROS_LECTURA, "");
        String jsonSend = "";
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
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
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public static String getCurrentMeasurement(Context context) {
        MeasurementModel _o = new MeasurementModel();
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            String _tmpBloodOxygen = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_OXYGEN_KEY, "255");
            String _tmpHeartRate = Sharedpreferences.getInstance(context).obtenValorString(Constans.HEART_KEY, "255");
            String _tmpTemperature = Sharedpreferences.getInstance(context).obtenValorString(Constans.TEMPERATURE_KEY, "255");
            String _tmpBloodPressure = Sharedpreferences.getInstance(context).obtenValorString(Constans.BLOOD_PRESSURE_KEY, "255");
            String _tmpNextMeasurementDate = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_PROXIMA_LECTURA, "");
            String _tmpFecha = Sharedpreferences.getInstance(context).obtenValorString(Constans.FECHA_ULTIMA_LECTURA, "");
            String _tmpSincronizando = Sharedpreferences.getInstance(context).obtenValorString(Constans.ESTA_SINCRONIZANDO, "false");

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
            _o.setSincronizando(Boolean.valueOf(_tmpSincronizando));
            _o.setNextMeasurementDate(nextMeasurementDate);
            _o.setFecha(fecha);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
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

    public static String getFechaOfMillSeconds(Long fechaAConvertir){
        String _fecha = "";
        try {
            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(fechaAConvertir);
            _fecha = sdfDateFormat.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return _fecha;
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

    public static void establecerHoraReloj(Context context){
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Utils.isDeviceConnect){
                        manager = CommandManager.getInstance(context);
                        manager.getDateSetCommand(1);
                        mTimer.cancel();
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void prendeTodosLosSensores(Context context){
        try{
            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(Utils.isDeviceConnect){
                            mTimer.cancel();
                            manager = CommandManager.getInstance(context);
                            Utils.establecerHoraReloj(context);
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
                        }
                    }
                }, 100, 3000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void prendeMedicionesXHora(Context context){
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

    public static int obtenerProgresoSincronizacion(Context context){
        int _r = 0;
        try{
            _r = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.PROGRESO_SINCRONIZANDO, "0"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }

    public static LecturasRequest existeFechaIgualEnLectutas(LecturasRequest lecturaTemp, ArrayList<LecturasRequest> arrayLecturasRequest, int tipo){
        LecturasRequest _r = null;
        try{
            for(int i = 0; i < arrayLecturasRequest.size(); i++){
                if(arrayLecturasRequest.get(i).getDispositivo_parametro_id() == tipo){
                    if(lecturaTemp.getFecha().equals(arrayLecturasRequest.get(i).getFecha())){
                        _r = arrayLecturasRequest.get(i);
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return _r;
    }

    public static boolean iniciaProcesoTramasSuenio(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean _r = false;
        try{

            ProviderProcess providerProcess = new ProviderProcess();
            InformacionUsuarioModel informacionUsuarioModel = providerProcess.getInformacionUsuario(context);
            String horaInicioSuenio = informacionUsuarioModel.getInformacion_hora_inicio_suenio();
            String horaFinSuenio    = informacionUsuarioModel.getInformacion_hora_fin_suenio();

            if(horaInicioSuenio == null || horaFinSuenio == null){
                horaInicioSuenio = "21:00";
                horaFinSuenio    = "09:00";
            }else{
                if(horaInicioSuenio.equals("") || horaInicioSuenio.length() < 4){
                    horaInicioSuenio = "21:00";
                }
                if(horaFinSuenio.equals("") || horaFinSuenio.length() < 4){
                    horaFinSuenio    = "09:00";
                }
            }

            estableceHorarioSuenio(context, horaInicioSuenio, horaFinSuenio);
            List<LecturasResponse> arrayListLecturas = providerProcess.getUltimasLecturasSuenio(context);
            ultimaLecturaSuenio = "";
            if(arrayListLecturas != null && arrayListLecturas.size() > 0){
                ultimaLecturaSuenio = arrayListLecturas.get(0).getLectura_fecha();
            }else{
                String now = Utils.getHora();
                Long nowInMillisecionds = Utils.getFechaInMillSeconds(now);
                Long _5diasAtras = (5 * 24 * 60 * 60 * 1000L);
                Date date2 = new Date(nowInMillisecionds - _5diasAtras);
                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ultimaLecturaSuenio = sdfDateFormat.format(date2);
            }
            ultimaLecturaSuenio = ultimaLecturaSuenio.replace("T", " ");
            //ultimaLecturaSuenio = "2021-03-01 00:00:00"; //Temporal para pruebas
            Utils.timerLecturasSuenio = 15;
            Utils.lecturasSuenio.removeAll(Utils.lecturasSuenio);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Hora Inicio " + horaInicioSuenio);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Hora Fin " + horaFinSuenio);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Fecha " + ultimaLecturaSuenio);
            obtenHistorialDeSuenio(context, ultimaLecturaSuenio);
            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try{
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Contador de sueño " + Utils.timerLecturasSuenio);
                            if(--Utils.timerLecturasSuenio < 0){
                                mTimer.cancel();
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Terminamos lecturas con " + Utils.lecturasSuenio.size() + " registros");
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Iniciamos proceso de envio");
                                validaTramasSuenio(context);
                            }else{
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALJDRPLG - " + "Tamaño del arreglo de sueño = " + Utils.lecturasSuenio.size());
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
                        }
                    }
                }, 100, 1000);
            }catch (Exception e){
                Log.d("Egesio", e.getMessage());
            }

        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public static boolean validaTramasSuenio(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean _r = false;
        try{
            ArrayList<SuenoModelResponse> lecturasSuenioClone = ( ArrayList<SuenoModelResponse>)Utils.lecturasSuenio.clone();
            ArrayList<SuenoModelResponse> lecturasSuenioTemp = new ArrayList<>();
            ArrayList<SuenoModelResponse> lecturasSuenioFinal = new ArrayList<>();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Tamaño Original del Arreglo = " + lecturasSuenioClone.size() );
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Calcula horas de inicio y fin en milli");
            for(int i = 0; i < lecturasSuenioClone.size(); i++){
                SuenoModelResponse suenioModelResponse = lecturasSuenioClone.get(i);
                int year    = suenioModelResponse.getYear();
                int month   = suenioModelResponse.getMonth();
                int day     = suenioModelResponse.getDay();
                int hour    = suenioModelResponse.getHour();
                int minutes = suenioModelResponse.getMinutes();
                String _fechaInicio = "" + year + "-" + Utils.rellenaConCeros(String.valueOf(month), 2) + "-" + Utils.rellenaConCeros(String.valueOf(day), 2) + " " + Utils.rellenaConCeros(String.valueOf(hour), 2) + ":" + Utils.rellenaConCeros(String.valueOf(minutes), 2) + ":00";
                Long _fechaInicioInMill = Utils.getFechaInMillSeconds(_fechaInicio);
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Fecha Inicio = " + _fechaInicio);
                suenioModelResponse.setHoraInicio(_fechaInicioInMill);
                if(suenioModelResponse.getSleepId() == 2){
                    Long tiempoSuenio = ((suenioModelResponse.getTimeSlepping_1() * 16 * 16) + suenioModelResponse.getTimeSlepping_2()) * 60 * 1000L;
                    Long _fechaFinInMill = _fechaInicioInMill + tiempoSuenio;
                    suenioModelResponse.setHoraFin(_fechaFinInMill);
                    String horaInString = getHorasYMinutos(((suenioModelResponse.getTimeSlepping_1() * 16 * 16) + suenioModelResponse.getTimeSlepping_2()));
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Duración  = " + horaInString);
                    suenioModelResponse.setDuracion(tiempoSuenio);
                    suenioModelResponse.setDuracionInStr(horaInString);
                }else if(suenioModelResponse.getSleepId() == 1){
                    //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Calculamos despiertos y ligeros");
                    Long tiempoSuenio = ((suenioModelResponse.getTimeSlepping_1() * 16 * 16) + suenioModelResponse.getTimeSlepping_2()) * 60 * 1000L;
                    SuenoModelResponse suenioModelResponseDespierto = new SuenoModelResponse();
                    Long _fechaInicioDespiertoInMill = _fechaInicioInMill + tiempoSuenio;
                    String _fechaInicioDespierto =  Utils.getFechaOfMillSeconds(_fechaInicioDespiertoInMill);
                    if(_fechaInicioDespierto != null && _fechaInicioDespierto.length() == 19){
                        suenioModelResponseDespierto.setYear(Integer.valueOf(_fechaInicioDespierto.substring(0,4)));
                        suenioModelResponseDespierto.setMonth(Integer.valueOf(_fechaInicioDespierto.substring(5,7)));
                        suenioModelResponseDespierto.setDay(Integer.valueOf(_fechaInicioDespierto.substring(8,10)));
                        suenioModelResponseDespierto.setHour(Integer.valueOf(_fechaInicioDespierto.substring(11,13)));
                        suenioModelResponseDespierto.setMinutes(Integer.valueOf(_fechaInicioDespierto.substring(14,16)));
                        suenioModelResponseDespierto.setSleepId(3);
                        suenioModelResponseDespierto.setTimeSlepping_1(0);
                        suenioModelResponseDespierto.setTimeSlepping_2(0);
                        suenioModelResponseDespierto.setHoraFin(null);
                        suenioModelResponseDespierto.setDuracion(0L);
                        suenioModelResponseDespierto.setHoraInicio(_fechaInicioDespiertoInMill);
                        lecturasSuenioTemp.add(suenioModelResponseDespierto);
                    }
                    suenioModelResponse.setDuracion(0L);
                    suenioModelResponse.setHoraFin(null);
                }
                lecturasSuenioTemp.add(suenioModelResponse);
            }
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Tamaño del clon antes de ordenarlo = " + lecturasSuenioTemp.size() );

            //Ordeamos arreglo por fechas de menor a mayor

            List lecturasSuenioSorted =  lecturasSuenioTemp.stream().sorted((o1, o2) -> o1.getHoraInicio().compareTo(o2.getHoraInicio())).collect(Collectors.toList());

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Tamaño del clon despues de ordenarlo = " + lecturasSuenioSorted.size() );

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Iniciamos proceso de periodos");
            //calculamos fechas de tiempo en cada fase
            for(int i = 0; i < lecturasSuenioSorted.size(); i++){
                SuenoModelResponse suenioModelResponseActual = (SuenoModelResponse)lecturasSuenioSorted.get(i);
                if((i+1) < lecturasSuenioSorted.size()) {
                    SuenoModelResponse suenioModelResponseSiguiente = (SuenoModelResponse) lecturasSuenioSorted.get(i + 1);
                    if (suenioModelResponseActual.getSleepId() == 1 || suenioModelResponseActual.getSleepId() == 3) {
                        Long tiempoPeriodo = suenioModelResponseSiguiente.getHoraInicio() - suenioModelResponseActual.getHoraInicio();
                        suenioModelResponseActual.setDuracion(tiempoPeriodo);
                        String horaInString = getHorasYMinutos((int)(tiempoPeriodo/60000));
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Duración  = " + horaInString);
                        suenioModelResponseActual.setDuracionInStr(horaInString);
                        Long _fechaFinInMill = suenioModelResponseActual.getHoraInicio() + tiempoPeriodo;
                        suenioModelResponseActual.setHoraFin(_fechaFinInMill);
                    }
                    if (suenioModelResponseActual.getSleepId() == 2) {

                    }
                }
            }

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Finalizamos proceso de periodos");

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Iniciamos proceso de relleno de ligeros");


            for(int i = 0; i < lecturasSuenioSorted.size(); i++){
                SuenoModelResponse suenioModelResponseActual = (SuenoModelResponse)lecturasSuenioSorted.get(i);
                if((i+1) < lecturasSuenioSorted.size()) {
                    SuenoModelResponse suenioModelResponseSiguiente = (SuenoModelResponse) lecturasSuenioSorted.get(i + 1);
                    Long resultadoResta = suenioModelResponseSiguiente.getHoraInicio() - suenioModelResponseActual.getHoraFin();
                    if ( resultadoResta > 0) {
                        //Se agrega sueño ligero
                        SuenoModelResponse suenioModelResponseLigero = new SuenoModelResponse();
                        String _fechaInicioLigero =  Utils.getFechaOfMillSeconds(suenioModelResponseActual.getHoraFin());
                        if(_fechaInicioLigero != null && _fechaInicioLigero.length() == 19){
                            suenioModelResponseLigero.setYear(Integer.valueOf(_fechaInicioLigero.substring(0,4)));
                            suenioModelResponseLigero.setMonth(Integer.valueOf(_fechaInicioLigero.substring(5,7)));
                            suenioModelResponseLigero.setDay(Integer.valueOf(_fechaInicioLigero.substring(8,10)));
                            suenioModelResponseLigero.setHour(Integer.valueOf(_fechaInicioLigero.substring(11,13)));
                            suenioModelResponseLigero.setMinutes(Integer.valueOf(_fechaInicioLigero.substring(14,16)));
                            suenioModelResponseLigero.setSleepId(1);
                            suenioModelResponseLigero.setTimeSlepping_1(0);
                            suenioModelResponseLigero.setTimeSlepping_2(0);
                            suenioModelResponseLigero.setHoraFin(suenioModelResponseActual.getHoraFin() + resultadoResta);
                            suenioModelResponseLigero.setDuracion(resultadoResta);
                            String horaInString = getHorasYMinutos((int)(resultadoResta/60000));
                            suenioModelResponseLigero.setDuracionInStr(horaInString);
                            suenioModelResponseLigero.setHoraInicio(suenioModelResponseActual.getHoraFin());
                            lecturasSuenioFinal.add(suenioModelResponseLigero);
                        }
                    }
                    lecturasSuenioFinal.add(suenioModelResponseActual);
                }
            }

            List lecturasSuenioSortedFinal =  lecturasSuenioFinal.stream().filter(l -> Utils.getFechaInMillSeconds("" + l.getYear() + "-" + Utils.rellenaConCeros(String.valueOf(l.getMonth()), 2) + "-" + Utils.rellenaConCeros(String.valueOf(l.getDay()), 2) + " " + Utils.rellenaConCeros(String.valueOf(l.getHour()), 2) + ":" + Utils.rellenaConCeros(String.valueOf(l.getMinutes()), 2) + ":00") > Utils.getFechaInMillSeconds(ultimaLecturaSuenio)).sorted((o1, o2) -> o1.getHoraInicio().compareTo(o2.getHoraInicio())).collect(Collectors.toList());
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Tamaño del arreglo final despues de ordenarlo = " + lecturasSuenioSortedFinal.size() );
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "************");
            for(int i = 0; i < lecturasSuenioSortedFinal.size(); i++){
                SuenoModelResponse suenioModelResponseFinal = (SuenoModelResponse)lecturasSuenioSortedFinal.get(i);
                String _fecha = "" + suenioModelResponseFinal.getYear() + "-" + Utils.rellenaConCeros(String.valueOf(suenioModelResponseFinal.getMonth()), 2) + "-" + Utils.rellenaConCeros(String.valueOf(suenioModelResponseFinal.getDay()), 2);
                String _horaInicio = Utils.getFechaOfMillSeconds(suenioModelResponseFinal.getHoraInicio()).substring(11, 16);
                String _horaFinal  = Utils.getFechaOfMillSeconds(suenioModelResponseFinal.getHoraFin()).substring(11, 16);
                String tipoSuenio = "";
                if(suenioModelResponseFinal.getSleepId() == 1){
                    tipoSuenio = "Sueño ligero";
                }else if(suenioModelResponseFinal.getSleepId() == 2){
                    tipoSuenio = "Sueño profundo";
                }else if(suenioModelResponseFinal.getSleepId() == 3){
                    tipoSuenio = "Despierto";
                }
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Trama de sueño = " +  _fecha + " " + _horaInicio + "-" + _horaFinal + "   " + suenioModelResponseFinal.getDuracionInStr() + "     " + tipoSuenio);
            }

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "************");
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Enviamos información a la base de datos");

            String jsonSleep = "";
            ArrayList<LecturasRequest> arrayLecturasRequest = new ArrayList<>();
            int idPulsera = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.IDPULSERA, "0"));
            String _idioma = Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es");
            String _separador = ",";
            for(int i = 0; i < lecturasSuenioSortedFinal.size(); i++){
                SuenoModelResponse suenioModelResponseToJson = (SuenoModelResponse)lecturasSuenioSortedFinal.get(i);
                String _fecha = "" + suenioModelResponseToJson.getYear() + "-" + Utils.rellenaConCeros(String.valueOf(suenioModelResponseToJson.getMonth()), 2) + "-" + Utils.rellenaConCeros(String.valueOf(suenioModelResponseToJson.getDay()), 2) + " " + Utils.rellenaConCeros(String.valueOf(suenioModelResponseToJson.getHour()), 2) + ":" + Utils.rellenaConCeros(String.valueOf(suenioModelResponseToJson.getMinutes()), 2) + ":00";
                LecturasRequest sleepParam = new LecturasRequest();
                sleepParam.setDispositivo_id(idPulsera);
                sleepParam.setValor("" + suenioModelResponseToJson.getSleepId() + "/" +   (suenioModelResponseToJson.getDuracion()/60000));
                sleepParam.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_sleep);
                sleepParam.setFecha(_fecha);
                sleepParam.setBnd_store_foward(false);
                sleepParam.setIdioma(_idioma);
                if((i+1) == lecturasSuenioSortedFinal.size()){
                    _separador = "";
                }
                jsonSleep += sleepParam.toJSON() + _separador;
                arrayLecturasRequest.add(sleepParam);
            }
            SendDataEgesio sendDataEgesio = new SendDataEgesio(context);
            sendDataEgesio.ejecutaLlamadaAsyncForPeople("[" + jsonSleep+  "]", arrayLecturasRequest);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public static boolean estableceHorarioSuenio(Context context, String _hI, String _hF){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean _r = false;
        try{
            CommandManager manager;
            manager = CommandManager.getInstance(context);
            manager.setTurnSleepData(1, _hI, _hF);
            _r = true;
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public static boolean obtenHistorialDeSuenio(Context context, String _h){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        boolean _r = false;
        try{
            CommandManager manager;
            manager = CommandManager.getInstance(context);
            Long fechaConsultaInMill = Utils.getFechaInMillSeconds(_h);
            manager.setSyncSleepData(fechaConsultaInMill);

        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return _r;
    }

    public static String getHorasYMinutos(int fechaInMin){
        String r = "0h0m";
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            if(fechaInMin < 60) {
                r = "" + fechaInMin + "m";
            }else{
                int horas = fechaInMin / 60;
                r = "" + horas + "h";
                int minCal = horas * 60;
                int resultado = fechaInMin - minCal;
                r += "" + resultado + "m";
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    public static boolean escaneaPulcera(String _mac, Context context){
        boolean r = false;

        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{

            if (!isOpenLocationService(context)) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Es necesario habilitar el GPS!!! ");
                Toast.makeText(context, "Es necesario habilitar el GPS!!!", Toast.LENGTH_LONG).show();
                //Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //context.startActivity(callGPSSettingIntent);
            }else {
                BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null) {
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Bluetooth no soportado!!! ");
                    Toast.makeText(context, "Bluetooth no soportado!!!", Toast.LENGTH_LONG).show();
                }

                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                BluetoothAdapter bluetoothAdapter = App.mBluetoothLeService.mBluetoothAdapter;
                if(bluetoothAdapter != null){
                    boolean isConnect = false;
                    Set<BluetoothDevice> listDevice = bluetoothAdapter.getBondedDevices();
                    if(listDevice.size() > 0){
                        for (BluetoothDevice device : listDevice) {
                            String deviceConect = device.getAddress();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - SSSSS " + device.getAddress());
                            String idPulsera = Sharedpreferences.getInstance(Utils.myContext).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                            if(deviceConect.equals(idPulsera))
                                isConnect = true;
                        }
                    }
                    if(isConnect){
                        String idPulsera = Sharedpreferences.getInstance(Utils.myContext).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                        if(App.mBluetoothLeService.connect(idPulsera)){
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Intento conectar => " + idPulsera);
                        }else{
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "No conecto MAC => " + idPulsera );
                        }
                    }else {
                        scanLeDevice(true);
                    }

                }

            }

        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return r;
    }

    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, byte[] bytes) {
            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try{
                            //deviceBean.setRssi(i);
                            //mLeDeviceListAdapter.addDevice(deviceBean);
                            //mLeDeviceListAdapter.addDevice(bluetoothDevice);
                            String idPulsera = Sharedpreferences.getInstance(Utils.myContext).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                            if(bluetoothDevice.getAddress().equals(idPulsera)){
                                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - deviceBean = idPulsera " + bluetoothDevice.getAddress() + " = " + idPulsera);
                                if(App.mBluetoothLeService.connect(idPulsera)){
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Intento conectar => " + idPulsera);
                                }else{
                                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "No conecto MAC => " + idPulsera );
                                }
                                scanLeDevice(false);
                            }

                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - deviceBean = " + bluetoothDevice.getAddress());

                        }catch(Exception e){
                            e.printStackTrace();
                            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error 1 " + e.getMessage());
                        }
                    }
                }, 100);
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error 2 " + e.getMessage());
            }
        }

    };

    private static void scanLeDevice(final boolean enable) {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            runnable = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    String idPulsera = Sharedpreferences.getInstance(Utils.myContext).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                    if(App.mBluetoothLeService.connect(idPulsera)){
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Intento conectar => " + idPulsera);
                    }else{
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "No conecto MAC => " + idPulsera );
                    }
                }
            };
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

}