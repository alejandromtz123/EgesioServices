package com.egesio.test.egesioservices.activity;

import androidx.appcompat.app.AppCompatActivity; //
import androidx.core.app.ActivityCompat; //

import android.Manifest; //
import android.content.Context;
import android.content.Intent; //
import android.content.pm.PackageManager; //
import android.net.Uri; //
import android.os.Bundle; //
import android.os.PowerManager; //
import android.provider.Settings; //
import android.util.Log;
import android.view.View; //
import android.widget.Button; //
import android.widget.TextView; //

import com.egesio.test.egesioservices.R; //
import com.egesio.test.egesioservices.constants.Constans;
import com.egesio.test.egesioservices.procesos.HistorialLecturasProcess;
import com.egesio.test.egesioservices.utils.Sharedpreferences;
import com.egesio.test.egesioservices.utils.Utils; //

import java.util.Timer; //
import java.util.TimerTask; //

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button buttonInicia;
    private Button buttonStop;
    private Button buttonConectDevice;
    private Button buttonDisconectDevice;
    private Button buttonGetValores;
    private Button buttonSendData;
    private TextView textViewValores;
    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciaComponetes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
        Utils.guardaValores(this);
    }

    @Override
    public void onClick(View v) {
        if(R.id.buttonInicia ==  v.getId()){
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TOKEN_SEND,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIwNWI3ZmU0NS1lNmM0LTRlYjMtYjk2MC1hNzYyMDY1NjhmMDMiLCJuYW1laWQiOiJZV1J0YVc1RVpYWT0iLCJuYmYiOjE2MTE2MjA1MjUsImV4cCI6MTYxMTY2MzcyNX0.JAhh_NYp-xTF6QC6eYGoz5mKakxSvMrRPpSf64Ztf7A");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDIOMA_SEND,"es");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.GATEWAY_URL,"http://201.156.230.48:3030/");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INFORMACION_USUARIO_ID,"790");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TIEMPO_LECTURAS,"http://201.156.230.48:3030/lecturas/TiempoLecturas/Get?informacion_usuario_id=945");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.MACADDRESS, "EE:BF:60:20:A9:D9");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDPULSERA, "117");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.PERIODO, "60");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.URL_SERVICE_EGESIO, "http://201.156.230.48:3030/lecturas/lecturas/PostList");



            Utils.iniciaServicio(v.getContext());
            Utils.iniciaProcesoLectura(v.getContext());

            //HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(v.getContext());
            //historialLecturasProcess.iniciaProceso();



            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String _o = Utils.getCurrentMeasurement(v.getContext());

                        Log.d("Egesio", "Valores - " + _o);
                    }
                }, 100, 5000);
            }catch (Exception e){
                Log.d("Egesio", e.getMessage());
            }


            //new ValidaPeriodo(this).execute("");
            //boolean b = InternetConnection.getInstance().validaConexion(v.getContext());
            //Log.d("", "Hay Internet = " + b);
            //Toast.makeText(this, "Hay Internet = " + b, Toast.LENGTH_SHORT).show();
            //Utils.enviaDatosEgesioDB(v.getContext());
            //Historticos




            //new HistoricoLecturas(v.getContext()).execute("4");

            //HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(v.getContext());
            //historialLecturasProcess.iniciaProceso();
            //historialLecturasProcess.obtenHistorialLecturasReloj();



        }else if(R.id.buttonStop ==  v.getId()){
            Utils.detenServicio(v.getContext());
        }else if(R.id.buttonConectDevice ==  v.getId()){
            Utils.iniciaProcesoLectura(v.getContext());
        }else if(R.id.buttonDisconectDevice ==  v.getId()){
            Utils.detenProcesoLectura(v.getContext());
        }else if(R.id.buttonGetValores ==  v.getId()){
            Utils.obtenTodosValoresJSON(v.getContext());
        }else if(R.id.buttonSendData ==  v.getId()){
            Utils.enviaDatosEgesioDB(v.getContext());
        }
    }

public void iniciaComponetes(){
        buttonInicia           = findViewById(R.id.buttonInicia);
        buttonStop             = findViewById(R.id.buttonStop);
        buttonConectDevice     = findViewById(R.id.buttonConectDevice);
        buttonDisconectDevice  = findViewById(R.id.buttonDisconectDevice);
        buttonGetValores       = findViewById(R.id.buttonGetValores);
        buttonSendData         = findViewById(R.id.buttonSendData);
        textViewValores        = findViewById(R.id.textViewValores);
        buttonInicia.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonConectDevice.setOnClickListener(this);
        buttonDisconectDevice.setOnClickListener(this);
        buttonGetValores.setOnClickListener(this);
        buttonSendData.setOnClickListener(this);
    }

    public void initPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
        }

        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }else{
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        }
    }


    private static int periodo = 100;
    private static int sumPeriodoStartService = 0;
    private static int sumPeriodoStopService = 0;
    private static int sumDeviceConnect = 0;
    private static int sumDeviceDisconnect = 0;

    public static void validaDeviceDisconnect(Context context, int awaitTime) {
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sumDeviceDisconnect += periodo;
                    if ( Utils.isDeviceDisconnect ) {
                        Log.d("Egesio", "Pulsera SI Desconectada y NO mitiendo");
                        //textViewValores.setText("Servicio SI Iniciado");
                        sumDeviceDisconnect = 0;
                        mTimer.cancel();
                    }else if(sumDeviceDisconnect > awaitTime){
                        sumDeviceDisconnect = 0;
                        mTimer.cancel();
                    }
                    Log.d("Egesio", "Pulsera NO Desconectada y SI emitiendo - " + sumDeviceDisconnect + " - " + Utils.isServiceStop + " - " + awaitTime);
                }
            }, 100, periodo);
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void validaDeviceConnect(Context context, int awaitTime) {
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    sumDeviceConnect += periodo;
                    if ( Utils.isDeviceConnect ) {
                        Log.d("Egesio", "Pulsera SI Conectada y SI mitiendo");
                        //textViewValores.setText("Servicio SI Iniciado");
                        sumDeviceConnect = 0;
                        mTimer.cancel();
                    }else if(sumDeviceConnect > awaitTime){
                        sumDeviceConnect = 0;
                        mTimer.cancel();
                    }
                    Log.d("Egesio", "Pulsera NO Conectada y NO emitiendo - " + sumDeviceConnect + " - " + Utils.isServiceStop + " - " + awaitTime);
                }
            }, 100, periodo);
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void validaStopServicio(Context context, int awaitTime) {
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    sumPeriodoStopService += periodo;
                    if ( Utils.isServiceStop ) {
                        Log.d("Egesio", "Servicio SI Detenido");
                        //textViewValores.setText("Servicio SI Iniciado");
                        sumPeriodoStopService = 0;
                        mTimer.cancel();
                    }else if(sumPeriodoStopService > awaitTime){
                        sumPeriodoStopService = 0;
                        mTimer.cancel();
                    }
                    Log.d("Egesio", "Servicio NO Deteido - " + sumPeriodoStopService + " - " + Utils.isServiceStop + " - " + awaitTime);
                }
            }, 100, periodo);
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }

    public static void validaIniciaServicio(Context context, int awaitTime) {
        try{
            Timer mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    sumPeriodoStartService += periodo;
                    if ( Utils.isServiceStart ) {
                        Log.d("Egesio", "Servicio SI Iniciado");
                        //textViewValores.setText("Servicio SI Iniciado");
                        sumPeriodoStartService = 0;
                        mTimer.cancel();
                    }else if(sumPeriodoStartService > awaitTime){
                        sumPeriodoStartService = 0;
                        mTimer.cancel();
                    }
                    Log.d("Egesio", "Servicio NO Iniciado - " + sumPeriodoStartService + " - " + Utils.isServiceStart + " - " + awaitTime);
                }
            }, 100, periodo);
        }catch (Exception e){
            Log.d("Egesio", e.getMessage());
        }
    }








}