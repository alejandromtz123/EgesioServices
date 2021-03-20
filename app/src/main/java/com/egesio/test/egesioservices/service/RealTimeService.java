package com.egesio.test.egesioservices.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.model.SuenoModelResponse;
import com.egesio.test.egesioservices.utils.DataHandlerUtils;
import com.egesio.test.egesioservices.utils.LogUtil;
import com.egesio.test.egesioservices.utils.Sharedpreferences;
import com.egesio.test.egesioservices.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Double.isNaN;


public class RealTimeService extends Service {

    private static final String TAG = RealTimeService.class.getSimpleName();
    public static final String ACTION_REALTIME_BROADCAST = RealTimeService.class.getName() + "ConnectBroadcastRealTime";
    public static final String ACTION_GATT_DISCONNECTED_ALL = RealTimeService.class.getName() + "DisconnectBroadcastRealTime";

    private CommandManager manager;
    private Timer mTimerSensorOn = new Timer(true);
    private TimerTask timeTaskSensorOn;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Utils.myContext = this;
        Utils.lecturasHistoricas = new ArrayList<>();
        Utils.lecturasLogFirebase = new ArrayList<>();
        Utils.lecturasSuenio = new ArrayList<>();
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            manager = CommandManager.getInstance(this);
        }catch (Exception e){
            Utils.isServiceStart = false;
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Utils.myContext = this;
            Utils.lecturasHistoricas = new ArrayList<>();
            Utils.lecturasLogFirebase = new ArrayList<>();
            Utils.lecturasSuenio = new ArrayList<>();
            if("0".equals(Sharedpreferences.getInstance(this).obtenValorString(Constans.DETENER_SERVICIO_MANUAL, "0"))) {
                //if(Utils.isDeviceConnect)
                //creaNotificacion();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Inicio Proceso onStartCommand");
                creaAlarmaRecurrenteBluetooth();
                LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                Sharedpreferences.getInstance(this).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Validacion : " + Utils.isDeviceConnect);
                sendMessageToUI(this);
            }
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            try{
                if(App.mConnected)
                    App.mBluetoothLeService.disconnect();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
                Intent intentGeoRatioMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
                getApplicationContext().startService(intentGeoRatioMonitoring);
            }catch (Exception e2){
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e2.getMessage());
            }
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return super.onStartCommand(intent, flags, startId); //START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            if(App.mConnected)
                App.mBluetoothLeService.disconnect();
            if(mTimerSensorOn != null) mTimerSensorOn.cancel();
            if(timeTaskSensorOn != null) timeTaskSensorOn.cancel();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
            if("0".equals(Sharedpreferences.getInstance(this).obtenValorString(Constans.DETENER_SERVICIO_MANUAL, "0"))){
                Intent intentRealTimeMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
                getApplicationContext().startService(intentRealTimeMonitoring);
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
            try {
                Intent intentRealTimeMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
                startService(intentRealTimeMonitoring);
            }catch (Exception e2){
                e2.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error2 startForegroundService " + e2.getMessage());
            }
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            if(App.mConnected)
                App.mBluetoothLeService.disconnect();
            if(mTimerSensorOn != null) mTimerSensorOn.cancel();
            if(timeTaskSensorOn != null) timeTaskSensorOn.cancel();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
            Sharedpreferences.getInstance(this).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
            Sharedpreferences.getInstance(this).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "1");
            Intent intentGeoRatioMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
            getApplicationContext().startService(intentGeoRatioMonitoring);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    private void sendMessageToUI(Context context) {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Valido bluetooth a : " + App.mBluetoothLeService);
                    if(App.mBluetoothLeService != null){
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Lanzo nueva peticion");
                        Intent intent = new Intent(RealTimeService.ACTION_REALTIME_BROADCAST);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                        mTimer.cancel();
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }


    private IntentFilter makeGattUpdateIntentFilter() {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(RealTimeService.ACTION_REALTIME_BROADCAST);
        intentFilter.addAction(RealTimeService.ACTION_GATT_DISCONNECTED_ALL);
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        return intentFilter;
    }

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RealTimeService.ACTION_REALTIME_BROADCAST.equals(action)) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Entre a iniciar la pulsera");
                String idPulsera = Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Id de Pulsera: " + idPulsera);

                Utils.escaneaPulcera(idPulsera, context);

                /*
                if(App.mBluetoothLeService.connect(idPulsera)){
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Intento conectar => " + idPulsera);
                }else{
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "No conecto MAC => " + idPulsera );
                }*/
                //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Va a enviar datos");
                //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ACTION_MEDICION_BROADCAST");
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ACTION_GATT_CONNECTED");
                App.mBluetoothLeService.initialize();
                Utils.isDeviceConnect = true;
                Utils.isDeviceDisconnect = false;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = false;

                //manager.heartRateSensor(1);
                //manager.temperatureSensor(1);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action) && !Utils.isDeviceDisconnectManual) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "DISCONECT");
                if("1".equals(Sharedpreferences.getInstance(context).obtenValorString(Constans.DETENER_SERVICIO_MANUAL, "0"))){
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "DISCONECT DETENER_SERVICIO_MANUAL = 1");
                    return;
                }

                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = false;

                Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mGattUpdateReceiver);
                if(!App.mConnected){
                    LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - App.mConnected = " + App.mConnected);
                    LocalBroadcastManager.getInstance(context).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                    App.mBluetoothLeService.initialize();
                    //App.mBluetoothLeService.connect(Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00"));
                    try{
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Iniciamos nuevamente despues de ACTION_GATT_DISCONNECTED");
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(mGattUpdateReceiver);


                        if (!Utils.isMyServiceRunning(RealTimeService.class, context)) {
                            Sharedpreferences.getInstance(context).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
                            Sharedpreferences.getInstance(context).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "1");
                            Intent intentRealTimeMonitoring = new Intent(context, RealTimeService.class);
                            context.startService(intentRealTimeMonitoring);
                            Utils.isServiceStart = true;
                        }else{
                            Utils.detenServicio(context);
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




                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
                    }
                }
            }else if (RealTimeService.ACTION_GATT_DISCONNECTED_ALL.equals(action)) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "DISCONNECT MANUAL");
                //LocalBroadcastManager.getInstance(context).unregisterReceiver(mGattUpdateReceiver);
                App.mBluetoothLeService.disconnect();
                //App.mConnected = false;
                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = true;
                Utils.isDeviceConnecting = false;

            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "ACTION_DATA_AVAILABLE MANUAL");
                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                List<Integer> data = DataHandlerUtils.bytesToArrayList(txValue);
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "DATA_AVAILABLE : " + data);
                //manager.temperatureSensor(1);
                Utils.isDeviceConnect = true;
                Utils.isDeviceDisconnect = false;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = false;

                if (data.get(0) == 0xAB){
                    hayRespuestaPulsera(context);
                }

                if (data.get(4) == 0X91) {
                    validaConexionPulsera(context);
                    lecturaData0x91NivelBateria(context);
                    Utils.prendeTodosLosSensores(context);
                }

                if (data.get(4) == 0X92) {
                    lecturaData0x92Firmware(data, context);
                }

                if (data.get(0) == 0xAB && data.get(4) == 0x31) {
                    lecturaData0x31(data, context);
                }

                if (data.get(0) == 0xAB && data.get(4) == 0x86) {
                    lecturaData0x86TemperaturaTiempoReal(data, context);
                }

                if (data.size() == 13 && data.get(4) == 0x32) {
                    lecturaData0x32(data, context);
                }

                //1.Current pedometer, calories, sleep data
                if (data.get(0) == 0xAB && data.get(4) == 0x51 && data.get(5) == 0x08) {
                    lecturaData0x51And0x08CaloriasPasos(data, context);
                }

                //2.Bracelet single heart rate measurement, blood oxygen, blood pressure data
                if ((data.get(0) == 0xAB && data.get(4) == 0x51)){
                    lecturaData0x51(data, context);
                }

                //3.The hourly  measurement data (pedometers, calories, heart rate, blood oxygen, blood pressure, sleep time)
                if (data.get(0) == 0xAB && data.get(4) == 0x51 && data.get(5) == 0x20){//Hourly
                    lecturaData0x51And0x20Hourly(data, context);
                }

                if (data.get(0) == 0){
                    lecturaData0x0VecesDespierto(data, context);
                }

                if (data.get(0) == 0xAB && data.get(4) == 0x52){
                    lecturaData0x52TramasSuenio(data, context);
                }

            }
        }
    };

    void lecturaData0x51And0x20Hourly(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "the steps, calories, heart rate, blood oxygen,blood pressure data from hourly measure");
            int year = data.get(6) + 2000;
            int month = data.get(7);
            int day = data.get(8);
            //Note: When you receive the whole point data, we need to add one hour
            int hour = data.get(9)+1;
            String time= year+"-"+month+"-"+day+" "+hour;
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "hourly_time  "+time);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH");
            long timeInMillis=0;
            try {
                Date date = sdf.parse(time);
                timeInMillis=date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //-->Importante SPUtils.putLong(mContext, SPUtils.HOURLY_MEASURETIME, timeInMillis);
            Sharedpreferences.getInstance(context).escribeValorString(Constans.HOURLY_MEASURETIME, String.valueOf(timeInMillis));
            //pedometers
            int steps = (data.get(10) << 16) + (data.get(11) << 8) + data.get(12);
            //calories
            int calories = (data.get(13) << 16) + (data.get(14) << 8) + data.get(15);
            float distance = (steps * 0.7f)/1000;//If the user does not tell you his stride, by default he walked 0.7m every step
            BigDecimal bd = new BigDecimal((double) distance);
            BigDecimal bigDecimal = bd.setScale(2, RoundingMode.HALF_UP);
            float distance2 = bigDecimal.floatValue();
            //heart rate
            int heartRate = data.get(16);
            //blood oxygen
            int bloodOxygen = data.get(17);
            //blood pressure
            int bloodPressure_high = data.get(18);
            int bloodPressure_low = data.get(19);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x51And0x08CaloriasPasos(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "steps calories and sleep data current");
            //Current pedometer
            int steps = (data.get(6) << 16) + (data.get(7) << 8) + data.get(8);

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - steps = " + steps);
            float distance = (steps * 0.7f)/1000;//If the user does not tell you his stride, by default he walked 0.7m every step
            //Current  calories

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Distancia = " + distance);

            int calories =(data.get(9) << 16) + (data.get(10) << 8) + data.get(11);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Calorias = " + calories);
            //Current  sleep data
            long shallowSleep = (data.get(12) * 60 + data.get(13)) * 60 * 1000L;
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - shallowSleep = " + shallowSleep);
            long deepSleep = (data.get(14) * 60 + data.get(15)) * 60 * 1000L;
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - deepSleep = " + deepSleep);
            long sleepTime = shallowSleep+deepSleep;
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - sleepTime = " + sleepTime);
            int wake_times = data.get(16);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - wake_times = " + wake_times);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x51(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Utils.timerLecturasHistoricas = 25;
            try {
                int n = 60;
                if(Utils.lecturasHistoricas.size()<25){
                    n = 60 + Utils.lecturasHistoricas.size();
                    Sharedpreferences.getInstance(context).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, String.valueOf(n));
                }
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "-->> lecturasHistoricas" + Utils.lecturasHistoricas.size());
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            }

            if (data.size() != 13 && data.size() != 20){
                return;
            }

            if(data.get(5) == 0x13 || data.get(5) == 0x20) {
                SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int idPulsera = Integer.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.IDPULSERA, "0"));
                String _idioma = Sharedpreferences.getInstance(context).obtenValorString(Constans.IDIOMA_SEND, "es");
                String fechaRegistro = "";
                try {
                    int year = (2000 + data.get(6));
                    int month = (data.get(7));
                    int day = data.get(8);
                    int hour = (data.get(9)) + (data.get(5) == 0x20 ? 1 : 0);
                    int min = data.get(10);
                    fechaRegistro = "" + year + "-" +
                            Utils.rellenaConCeros(String.valueOf(month), 2) + "-" +
                            Utils.rellenaConCeros(String.valueOf(day), 2) + " " +
                            Utils.rellenaConCeros(String.valueOf(hour), 2) + ":" +
                            Utils.rellenaConCeros(String.valueOf(min), 2) + ":" + "00";
                    Date dateRegistro = sdfDateFormat.parse(fechaRegistro);
                    Long timeInSecsRegistro = dateRegistro.getTime();
                } catch (Exception e) {
                    fechaRegistro = "0000-00-00 00:00:00";
                }

                //Temperatura cada 5 min
                if(data.get(5) == 0x13){
                    lecturaData0x13(data, idPulsera, _idioma, fechaRegistro);
                }

                //Valores de Hora
                if(data.get(5) == 0x20) {
                    lecturaData0x20(data, idPulsera, _idioma, fechaRegistro);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x32(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Integer heartRate = data.get(6);
            Integer bloodOxygen = data.get(7);
            Integer bloodPressureHypertension = data.get(8);
            Integer bloodPressureHypotension = data.get(9);

            Utils.guardaDato(context, Constans.HEART_KEY, String.valueOf(heartRate));
            Utils.guardaDato(context, Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
            Utils.guardaDato(context, Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
            Utils.guardaDato(context, Constans.TEMPERATURE_KEY, data.get(11) + "." + data.get(12));
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x86TemperaturaTiempoReal(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Valores de Temperatura : = " + data);
            int entero = data.get(6);
            int decimal = data.get(7);
            Utils.guardaDato(context, Constans.TEMPERATURE_KEY, entero + "." + decimal);
            Utils.validaLecturas(context, manager, 1);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x31(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            switch (data.get(5)) {
                case 0X0A:
                    //Heart Rate（Real-time）
                    int heartRate = data.get(6);
                    //new SendDataFirebase(context).execute("{\"action\": \"HEART - " + data + " - " + Utils.getHora() + "\"}");
                    //manager.getOneClickMeasurementCommand(1);
                    manager.temperatureSensor(1);
                    manager.bloodOxygenSensor(1);
                    Utils.guardaDato(context, Constans.HEART_KEY, String.valueOf(heartRate));
                    Utils.validaLecturas(context, manager, 0);
                    break;
                case 0x12:
                    //Blood Oxygen（Real-time）
                    int bloodOxygen = data.get(6);
                    //new SendDataFirebase(context).execute("{\"action\": \"OXYGEN - " + data + " - " + Utils.getHora() + "\"}");
                    manager.temperatureSensor(1);
                    manager.bloodPressureSensor(1);
                    Utils.guardaDato(context, Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
                    Utils.validaLecturas(context, manager, 0);
                    break;
                case 0x22:
                    //Blood Pressure（Real-time）
                    int bloodPressureHypertension = data.get(6);
                    int bloodPressureHypotension = data.get(7);
                    //new SendDataFirebase(context).execute("{\"action\": \"PRESSURE - " + data + " - " + Utils.getHora() + "\"}");
                    manager.temperatureSensor(1);
                    manager.heartRateSensor(1);
                    Utils.guardaDato(context, Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
                    Utils.validaLecturas(context, manager, 0);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x91NivelBateria(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //manager.findBracelet();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x0VecesDespierto(List<Integer> data, Context context){
        //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALXPACK0 " + "second packet data from hourly measure");
            //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALXPACK0 " + "second packet---"+ Utils.getHora());

            long timeInMillis = Long.valueOf(Sharedpreferences.getInstance(context).obtenValorString(Constans.HOURLY_MEASURETIME, "0"));
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALXPACK0 = " + Utils.getFechaOfMillSeconds(timeInMillis)  + " - " + data);


            //sleep time
            long  shwllow_time = data.get(1) * 60 * 60 * 1000L + data.get(2) * 60 * 1000L;
            long deep_time = data.get(3) * 60 * 60 * 1000L + data.get(4) * 60 * 1000L;
            long total_time = shwllow_time + deep_time;
            int wake_times = data.get(5);
            //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALXPACK0 " + "---->>>> "+ data);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALXPACK0 " + "---->>>> "+ "shwllow_time=" + shwllow_time + "," + "deep_time=" + deep_time + "," + "total_time=" + total_time + "," + "wake_times=" + wake_times);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        //LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x52TramasSuenio(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALEPACK52 " + "--->>> " + data);
            Utils.timerLecturasSuenio = 15;
            int year, month, day, hour, minutes, sleepId, timeSlepping;
            String sleepString;

            year = (2000 + data.get(6));
            month = data.get(7);
            day = data.get(8);
            hour = data.get(9);
            minutes = data.get(10) + hour * 60;
            sleepId = data.get(11);
            timeSlepping = data.get(12) * 16 * 16 + data.get(13);

            if (sleepId == 1)
                sleepString = "normal";
            else if (sleepId == 2)
                sleepString = "profundo";
            else
                sleepString = "reposo";

            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALEPACK52 " + "--->>> Sleep {self.mac_address}: " + day + "/" + month + "/" + year);
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - ALEPACK52 " + "--->>> Tiempo definido: " + minutes + " minutos. Fase de sueño medido: " + sleepString + " durante " + timeSlepping + " minutos");

            SuenoModelResponse suenioModelResponse = new SuenoModelResponse();
            suenioModelResponse.setYear(year);
            suenioModelResponse.setMonth(month);
            suenioModelResponse.setDay(day);
            suenioModelResponse.setHour(hour);
            suenioModelResponse.setMinutes(data.get(10));
            suenioModelResponse.setSleepId(sleepId);
            suenioModelResponse.setTimeSlepping_1(data.get(12));
            suenioModelResponse.setTimeSlepping_2(data.get(13));
            Utils.lecturasSuenio.add(suenioModelResponse);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x13(List<Integer> data, int idPulsera, String _idioma, String fechaRegistro){
        if (data.get(11) != null && data.get(12) != null && !isNaN(data.get(11)) && !isNaN(data.get(12))) {
            Double temp = Double.parseDouble(data.get(11) + "." + data.get(12));
            if (temp != null && !isNaN(temp) && temp >= 35 && temp < 43) {
                LecturasRequest lecturasRequestTemperatura = new LecturasRequest();
                lecturasRequestTemperatura.setDispositivo_id(idPulsera);
                lecturasRequestTemperatura.setValor(String.valueOf(temp));
                lecturasRequestTemperatura.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_temperatura);
                lecturasRequestTemperatura.setFecha(fechaRegistro);
                lecturasRequestTemperatura.setBnd_store_foward(false);
                lecturasRequestTemperatura.setIdioma(_idioma);
                Utils.lecturasHistoricas.add(lecturasRequestTemperatura);
            }
        }
    }

    void lecturaData0x20(List<Integer> data, int idPulsera, String _idioma, String fechaRegistro){
        Integer heart = data.get(16);
        if (heart != null && !isNaN(heart) && heart > 40 && heart < 226) {
            LecturasRequest lecturasRequestHeart = new LecturasRequest();
            lecturasRequestHeart.setDispositivo_id(idPulsera);
            lecturasRequestHeart.setValor(String.valueOf(heart));
            lecturasRequestHeart.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_ritmo_cardiaco);
            lecturasRequestHeart.setFecha(fechaRegistro);
            lecturasRequestHeart.setBnd_store_foward(false);
            lecturasRequestHeart.setIdioma(_idioma);
            //int n =  60 + Utils.lecturasHistoricas.size();
            //Sharedpreferences.getInstance(context).escribeValorString(Constans.PROGRESO_SINCRONIZANDO, String.valueOf(n));
            Utils.lecturasHistoricas.add(lecturasRequestHeart);
        }
        Integer bloodOxygen = data.get(17);
        if (bloodOxygen != null && !isNaN(bloodOxygen) && bloodOxygen > 70 && bloodOxygen <= 100) {
            LecturasRequest lecturasRequestbloodOxygen = new LecturasRequest();
            lecturasRequestbloodOxygen.setDispositivo_id(idPulsera);
            lecturasRequestbloodOxygen.setValor(String.valueOf(bloodOxygen));
            lecturasRequestbloodOxygen.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_oxigenacion_sangre);
            lecturasRequestbloodOxygen.setFecha(fechaRegistro);
            lecturasRequestbloodOxygen.setBnd_store_foward(false);
            lecturasRequestbloodOxygen.setIdioma(_idioma);
            Utils.lecturasHistoricas.add(lecturasRequestbloodOxygen);
        }
        Integer bloodPressureHypertension = data.get(18);
        Integer bloodPressureHypotension = data.get(19);
        if (bloodPressureHypertension != null && bloodPressureHypotension != null && !isNaN(bloodPressureHypertension) &&
                bloodPressureHypertension > 70 && bloodPressureHypertension < 200 &&
                !isNaN(bloodPressureHypotension) && bloodPressureHypotension > 40 && bloodPressureHypotension < 130) {
            LecturasRequest lecturasRequestbloodPressure = new LecturasRequest();
            lecturasRequestbloodPressure.setDispositivo_id(idPulsera);
            lecturasRequestbloodPressure.setValor(bloodPressureHypertension + "/" + bloodPressureHypotension);
            lecturasRequestbloodPressure.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_presion_arterial);
            lecturasRequestbloodPressure.setFecha(fechaRegistro);
            lecturasRequestbloodPressure.setBnd_store_foward(false);
            lecturasRequestbloodPressure.setIdioma(_idioma);
            Utils.lecturasHistoricas.add(lecturasRequestbloodPressure);
        }
    }

    void creaAlarmaRecurrenteBluetooth(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        final int requestCode = 1337;
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000 , pendingIntent );
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void lecturaData0x92Firmware(List<Integer> data, Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            if(data.size() == 18){
                String _firmware = data.get(6) + "." + data.get(7);
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Firmware = " + _firmware);
                //nativeStorage.setItem("firmware_pulsera", data[6] + "." + data[7]);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.FIRMWARE_PULSERA, _firmware);
                Sharedpreferences.getInstance(context).escribeNativeStorageValorString(Constans.FIRMWARE_PULSERA, _firmware);
                Sharedpreferences.getInstance(context).escribeValorString(Constans.FIRMWARE_PULSERA_2, _firmware);
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void validaConexionPulsera(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        Utils.isDeviceConnect = true;
        Utils.isDeviceDisconnect = false;
        Utils.isDeviceDisconnectManual = false;
        Utils.isDeviceConnecting = false;
        //manager.temperatureSensor(1);
        manager.heartRateSensor(1);
        try{
            if(mTimerSensorOn != null){
                if(timeTaskSensorOn == null) {
                    timeTaskSensorOn = new TimerTask() {
                        @Override
                        public void run() {
                            int _c = --Utils.timerSensorInactive;
                            LogUtil.Imprime(TAG,  "Contador de sensor = " + _c);
                            if (_c <= 0) {
                                //manager.temperatureSensor(1);
                                manager.heartRateSensor(1);
                                if(_c <= -100){
                                    Utils.timerSensorInactive = 0;
                                    sendMessageToUI(context);
                                }
                            }
                        }
                    };
                }
                mTimerSensorOn.schedule(timeTaskSensorOn, 100, 1000);
            }
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    void hayRespuestaPulsera(Context context){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try{
            Utils.isDeviceConnect = true;
            Utils.isDeviceDisconnect = false;
            Utils.isDeviceDisconnectManual = false;
            Utils.isDeviceConnecting = false;
            Utils.timerSensorInactive = 10;
        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

}
