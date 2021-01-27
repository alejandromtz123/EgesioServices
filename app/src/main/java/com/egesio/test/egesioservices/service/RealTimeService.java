package com.egesio.test.egesioservices.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.command.CommandManager;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.model.LecturasRequest;
import com.egesio.test.egesioservices.utils.CallHelper;
import com.egesio.test.egesioservices.utils.DataHandlerUtils;
import com.egesio.test.egesioservices.utils.SendDataFirebase;
import com.egesio.test.egesioservices.utils.Sharedpreferences;
import com.egesio.test.egesioservices.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Double.isNaN;


public class RealTimeService extends Service {


    private CallHelper callHelper;

    private static final String TAG = RealTimeService.class.getSimpleName();
    public static final String ACTION_REALTIME_BROADCAST = RealTimeService.class.getName() + "ConnectBroadcastRealTime";
    public static final String ACTION_GATT_DISCONNECTED_ALL = RealTimeService.class.getName() + "DisconnectBroadcastRealTime";

    private CommandManager manager;
    private SharedPreferences sharedpreferences;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private NotificationChannel notificationChannel;
    private String NOTIFICATION_CHANNEL_ID = "17";

    @Override
    public void onCreate()
    {
        super.onCreate();
        try{
            Log.d(TAG, "Inicio Proceso onCreate");
            creaNotificacion();
            manager = CommandManager.getInstance(this);
        }catch (Exception e){
            Utils.isServiceStart = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            callHelper = new CallHelper(this);
            callHelper.start();
            Log.d(TAG, "Inicio Proceso onStartCommand");
            creaAlarmaRecurrenteBluetooth();
            LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            Sharedpreferences.getInstance(this).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
            //sendMessageToUI(this);
            Log.d(TAG, "Validacion : " + Utils.isDeviceConnect);
            //if(/*Utils.isServiceStart && */Utils.isDeviceConnect){
            sendMessageToUI(this);
            //    Intent intentDevice = new Intent(RealTimeService.ACTION_REALTIME_BROADCAST);
            //    LocalBroadcastManager.getInstance(this).sendBroadcastSync(intentDevice);
            //}
        }catch (Exception e){
            try{
                if(App.mConnected)
                    App.mBluetoothLeService.disconnect();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
                Intent intentGeoRatioMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
                getApplicationContext().startService(intentGeoRatioMonitoring);
            }catch (Exception e2){
            }
        }
        return super.onStartCommand(intent, flags, startId); //START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        callHelper.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try{
            if(App.mConnected)
                App.mBluetoothLeService.disconnect();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
            Intent intentGeoRatioMonitoring = new Intent(getApplicationContext(), RealTimeService.class);
            getApplicationContext().startService(intentGeoRatioMonitoring);
        }catch (Exception e){
        }
    }

    private void sendMessageToUI(Context context) {
        Log.d(TAG, "Entre a : sendMessageToUI");
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "Valido bluetooth a : " + App.mBluetoothLeService);
                    if(App.mBluetoothLeService != null){
                        Log.d(TAG, "Lanzo nueva peticion");
                        Intent intent = new Intent(RealTimeService.ACTION_REALTIME_BROADCAST);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                        mTimer.cancel();
                    }
                }
            }, 100, 100);
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
    }


    private IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(RealTimeService.ACTION_REALTIME_BROADCAST);
        intentFilter.addAction(RealTimeService.ACTION_GATT_DISCONNECTED_ALL);
        return intentFilter;
    }

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RealTimeService.ACTION_REALTIME_BROADCAST.equals(action)) {
                Log.d(TAG, "Entre a iniciar la pulsera");
                String idPulsera = Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00");
                Log.d(TAG, "Id de Pulsera: " + idPulsera);
                //if(!App.mConnected)
                App.mBluetoothLeService.connect(idPulsera);
                //else


                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = false;


                //    App.mBluetoothLeService.disconnect();
                Log.d(TAG, "Va a enviar datos");
                new SendDataFirebase(context).execute("{\"action\": \"ACTION_MEDICION_BROADCAST - " + Utils.getHora() + "\"}");
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_CONNECTED");
                new SendDataFirebase(context).execute("{\"action\": \"ACTION_GATT_CONNECTED - " +  Utils.getHora() + "\"}");
                App.mBluetoothLeService.initialize();

                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = true;


                //manager.heartRateSensor(1);
                //manager.temperatureSensor(1);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action) && !Utils.isDeviceDisconnectManual) {
                Log.d(TAG, "DISCONECT");
                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = false;
                Utils.isDeviceConnecting = true;

                new SendDataFirebase(context).execute("{\"action\": \"DISCONECT - " + Utils.getHora() + "\"}");
                Sharedpreferences.getInstance(context).escribeValorString(Constans.LAST_TIME_GENERAL, String.valueOf(System.currentTimeMillis()));
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mGattUpdateReceiver);
                if(!App.mConnected){
                    LocalBroadcastManager.getInstance(context).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                    App.mBluetoothLeService.initialize();
                    App.mBluetoothLeService.connect(Sharedpreferences.getInstance(context).obtenValorString(Constans.MACADDRESS, "00:00:00:00:00:00"));
                }
            }else if (RealTimeService.ACTION_GATT_DISCONNECTED_ALL.equals(action)) {
                Log.d(TAG, "DISCONNECT MANUAL");
                new SendDataFirebase(context).execute("{\"action\": \"DISCONNECT MANUAL - " + Utils.getHora() + "\"}");
                //LocalBroadcastManager.getInstance(context).unregisterReceiver(mGattUpdateReceiver);
                App.mBluetoothLeService.disconnect();
                //App.mConnected = false;
                Utils.isDeviceConnect = false;
                Utils.isDeviceDisconnect = true;
                Utils.isDeviceDisconnectManual = true;
                Utils.isDeviceConnecting = false;

            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "ACTION_DATA_AVAILABLE MANUAL");
                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                List<Integer> data = DataHandlerUtils.bytesToArrayList(txValue);
                //new SendDataFirebase(context).execute("{\"action\": \"DATA_AVAILABLE - " + data + " - " + Utils.getHora() + "\"}");
                //manager.temperatureSensor(1);
                if (data.get(4) == 0X91) {
                    manager.findBracelet();

                    Utils.isDeviceConnect = true;
                    Utils.isDeviceDisconnect = false;
                    Utils.isDeviceDisconnectManual = false;
                    Utils.isDeviceConnecting = false;

                    manager.heartRateSensor(1);
                }

                if (data.get(0) == 0xAB && data.get(4) == 0x31) {
                    switch (data.get(5)) {
                        case 0X0A:
                            //Heart Rate（Real-time）
                            int heartRate = data.get(6);
                            //new SendDataFirebase(context).execute("{\"action\": \"HEART - " + data + " - " + Utils.getHora() + "\"}");
                            //manager.getOneClickMeasurementCommand(1);
                            manager.temperatureSensor(1);
                            manager.bloodOxygenSensor(1);
                            Utils.guardaDato(context, Constans.HEART_KEY, String.valueOf(heartRate));
                            Utils.validaLecturas(context, manager);
                            break;
                        case 0x12:
                            //Blood Oxygen（Real-time）
                            int bloodOxygen = data.get(6);
                            //new SendDataFirebase(context).execute("{\"action\": \"OXYGEN - " + data + " - " + Utils.getHora() + "\"}");
                            manager.temperatureSensor(1);
                            manager.bloodPressureSensor(1);
                            Utils.guardaDato(context, Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
                            Utils.validaLecturas(context, manager);
                            break;
                        case 0x22:
                            //Blood Pressure（Real-time）
                            int bloodPressureHypertension = data.get(6);
                            int bloodPressureHypotension = data.get(7);
                            //new SendDataFirebase(context).execute("{\"action\": \"PRESSURE - " + data + " - " + Utils.getHora() + "\"}");
                            manager.temperatureSensor(1);
                            manager.heartRateSensor(1);
                            Utils.guardaDato(context, Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
                            Utils.validaLecturas(context, manager);
                            break;
                    }
                }

                if (data.get(0) == 0xAB && data.get(4) == 0x86) {
                    //new SendDataFirebase(context).execute("{\"action\": \"TEMP - " + Utils.getHora() + "\"}");
                    int entero = data.get(6);
                    int decimal = data.get(7);
                    Utils.guardaDato(context, Constans.TEMPERATURE_KEY, entero + "." + decimal);
                    Utils.validaLecturas(context, manager);

                }

                if (data != null && data.size() == 13 && data.get(4) == 0x32) {
                    //new SendDataFirebase(context).execute("{\"action\": \"REGRESO TODAAAAAS - " + Utils.getHora() + "\"}");

                    Integer heartRate = data.get(6);
                    Integer bloodOxygen = data.get(7);
                    Integer bloodPressureHypertension = data.get(8);
                    Integer bloodPressureHypotension = data.get(9);


                    Utils.guardaDato(context, Constans.HEART_KEY, String.valueOf(heartRate));
                    Utils.guardaDato(context, Constans.BLOOD_OXYGEN_KEY, String.valueOf(bloodOxygen));
                    Utils.guardaDato(context, Constans.BLOOD_PRESSURE_KEY, bloodPressureHypertension + "/" + bloodPressureHypotension);
                    Utils.guardaDato(context, Constans.TEMPERATURE_KEY, data.get(11) + "." + data.get(12));

                }

                //2.Bracelet single heart rate measurement, blood oxygen, blood pressure data
                if ((data.get(0) == 0xAB && data.get(4) == 0x51)){
                    Log.d(TAG, "Entre 2 data-> " + data);
                    Utils.timerLecturasHistoricas = 5;

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

                        if(data.get(5) == 0x13){
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

                        if(data.get(5) == 0x20) {
                            Integer heart = data.get(16);
                            if (heart != null && !isNaN(heart) && heart > 40 && heart < 226) {
                                LecturasRequest lecturasRequestHeart = new LecturasRequest();
                                lecturasRequestHeart.setDispositivo_id(idPulsera);
                                lecturasRequestHeart.setValor(String.valueOf(heart));
                                lecturasRequestHeart.setDispositivo_parametro_id(Constans.modelo_dispositivo_parametros_ritmo_cardiaco);
                                lecturasRequestHeart.setFecha(fechaRegistro);
                                lecturasRequestHeart.setBnd_store_foward(false);
                                lecturasRequestHeart.setIdioma(_idioma);
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
                    }
                }
            }
        }
    };

    void creaAlarmaRecurrenteBluetooth(){
        final int requestCode = 1337;
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000 , pendingIntent );

    }


    @SuppressLint("WrongConstant")
    public void creaNotificacion(){
        mNotifyManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, null);
        mBuilder.setContentTitle("Egesio Servicios Activos")
                .setContentText("Servicios en completa ejecución")
                .setTicker("Servicios en completa ejecución")
                //.setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Egesio Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Egesio Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotifyManager.createNotificationChannel(notificationChannel);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            startForeground(17, mBuilder.build());
        }
        else
        {
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotifyManager.notify(17, mBuilder.build());
        }
    }






}
