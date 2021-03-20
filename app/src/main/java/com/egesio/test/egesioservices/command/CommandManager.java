package com.egesio.test.egesioservices.command;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.egesio.test.egesioservices.constants.BleConstans;
import com.egesio.test.egesioservices.constants.Constans;

import java.util.Calendar;

public class CommandManager {

    private static final String TAG = "CommandManager";
    private static Context mContext;
    private static CommandManager instance;

    private CommandManager() {
    }

    public static synchronized CommandManager getInstance(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    public void sendVibrar(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB1;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte)control;
        broadcastData(bytes);
    }

    public void setClearData(){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x23;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) 0x00;
        broadcastData(bytes);
    }

    public void sensorTest(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB3;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }

    public void findBracelet() {
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x71;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }

    public void setResetBand(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xFF;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }

    public void getOneClickMeasurementCommand(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x32;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void realTimeAndOnceMeasure(int status, int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        bytes[5] = (byte) status;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void heartRateSensor(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        bytes[5] = (byte) 0X0A;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void bloodOxygenSensor(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        bytes[5] = (byte) 0X12;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void bloodPressureSensor(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        bytes[5] = (byte) 0X22;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void temperatureSensor(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x86;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control; // Turn ON sensor.
        broadcastData(bytes);
    }

    public void getDateSetCommand(int control){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] data = new byte[14];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 11;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x93;
        data[5] = (byte) 0x80;
        data[6] = (byte) control;
        data[7] = (byte) ((year & 0xff00) >> 8);
        data[8] = (byte) (year & 0xff);
        data[9] = (byte) (month & 0xff);
        data[10] = (byte) (day & 0xff);
        data[11] = (byte) (hour & 0xff);
        data[12] = (byte) (minute & 0xff);
        data[13] = (byte) (second & 0xff);
        broadcastData(data);
    }

    public void buttonClick(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB6;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }

    public void turnOnHourlyMeasurement(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x78;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    public void getPullDownSinchronizationData(int control, Long timeInMillis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] bytes = new byte[12];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 9;
        bytes[3] = (byte) 0xff;
        bytes[4] = (byte) 0x51;  //ID
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        bytes[7] = (byte) ((year - 2000));
        bytes[8] = (byte) month;
        bytes[9] = (byte) day;
        bytes[10] = (byte) hour;
        bytes[11] = (byte) minute;
        broadcastData(bytes);
    }

    //TEST
    public void getVersionInfo(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x92;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }

    public void setTurnSleepData(long timeInMillis, String horaInicial, String horaFinal) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int horaStart = Integer.valueOf(horaInicial.substring(0, 2));
        int minStart = Integer.valueOf(horaInicial.substring(3, 5));
        int horaStop = Integer.valueOf(horaFinal.substring(0, 2));
        int minStop = Integer.valueOf(horaFinal.substring(3, 5));
        byte[] data = new byte[11];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 8;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x7F;
        data[5] = (byte) 0x80;
        data[6] = (byte) 0x01;
        data[7] = (byte) horaStart;
        data[8] = (byte) minStart;
        data[9] = (byte) horaStop;
        data[10] = (byte) minStop;
        broadcastData(data);
    }

    public void setSyncSleepData(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        byte[] data = new byte[10];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 7;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x52;
        data[5] = (byte) 0x80;
        data[6] = (byte) 0x00;
        data[7] = (byte) ((year - 2000));
        data[8] = (byte) (month);
        data[9] = (byte) (day - 1);
        broadcastData(data);
    }



    private void broadcastData(byte[] bytes) {
        final Intent intent = new Intent(BleConstans.ACTION_SEND_DATA_TO_BLE);
        intent.putExtra(Constans.EXTRA_SEND_DATA_TO_BLE, bytes);
        try {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }




}
