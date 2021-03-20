package com.egesio.test.egesioservices.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.egesio.test.egesioservices.bean.BaseEvent;
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.service.BluetoothLeService;
import com.egesio.test.egesioservices.utils.LogUtil;
import com.egesio.test.egesioservices.utils.Utils;

import de.greenrobot.event.EventBus;

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

    public static BluetoothLeService mBluetoothLeService;
    private String NOTIFICATION_CHANNEL_ID = "17";



    public static boolean mConnected = false;
    public static boolean isConnecting = false;
    public static boolean BLE_ON = false;


    @Override
    public void onCreate() {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            super.onCreate();
            bindBleService();
            Utils.myContext = this;
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            BLE_ON = adapter.isEnabled();
            LogUtil.Imprime(TAG, Utils.getNombreMetodo() + " - " + "BLE_ON   " + BLE_ON);
            creaNotificacion();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    public void onCreateForIonic() {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            super.onCreate();
            bindBleService();
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            BLE_ON = adapter.isEnabled();
            LogUtil.Imprime(TAG, Utils.getNombreMetodo() + " - " + "BLE_ON   " + BLE_ON);
            creaNotificacion();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
            try {
                onCreateForIonic();
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    LogUtil.Imprime(TAG, Utils.getNombreMetodo() + " - " + "Unable to initialize Bluetooth");
                }
                LogUtil.Imprime(TAG, Utils.getNombreMetodo() + " - " + "onServiceConnected");
                EventBus.getDefault().post(new BaseEvent(BaseEvent.EventType.ONSERVICECONNECTED));
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
            }
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
            try {
                mBluetoothLeService = null;
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
            }
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
        }
    };

    private void bindBleService() {
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        }catch (Exception e){
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error " + e.getMessage());
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

    @SuppressLint("WrongConstant")
    public void creaNotificacion(){
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.START_PROCESS);
        try {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                //notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            Resources activityRes = getApplicationContext().getApplicationContext().getResources();
            int backResId = activityRes.getIdentifier("fcm_push_icon", "drawable", getApplicationContext().getApplicationContext().getPackageName());

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(backResId)
                    .setTicker("Hearty365")
                    //     .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle("Egesio")
                    .setContentText("Egesio se encuentra ejecutandose en segundo plano.")
                    .setContentInfo("Info");

            notificationManager.notify(/*notification id*/1, notificationBuilder.build());

        }catch (Exception e){
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - Error - " + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + Constans.END_PROCESS);
    }

}
