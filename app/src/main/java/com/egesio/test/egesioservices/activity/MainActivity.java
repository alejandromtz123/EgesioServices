package com.egesio.test.egesioservices.activity;

import androidx.appcompat.app.AppCompatActivity; //
import androidx.core.app.ActivityCompat; //

import android.Manifest; //
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
import com.egesio.test.egesioservices.utils.LogUtil;
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

//DESARROLLO

            /*"1615590515080" : "App - muestraValoresEgesio - token_jwt = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIxMjlkYzhhOS0zNTAwLTQ2NWYtOGM4Mi0yMDkxMGU4ZDlhYWIiLCJuYW1laWQiOiJZV1J0YVc1RVpYWT0iLCJuYmYiOjE2MTU1OTAzMjYsImV4cCI6MTYxNTYzMzUyNn0.OLEDnyqIaPPcmS3WI250HDMyNq3vEIc9U9y9OAvgDSM - 2021-03-12 17:08:35",
            "1615590515084" : "App - muestraValoresEgesio - pms_gateway_url = http://201.156.230.48:7001/ - 2021-03-12 17:08:35",
            "1615590515086" : "App - muestraValoresEgesio - instancia_id = 7 - 2021-03-12 17:08:35",
            "1615590515087" : "App - muestraValoresEgesio - dispositivo_id = 10 - 2021-03-12 17:08:35",
            "1615590515088" : "App - muestraValoresEgesio - get_tiempo_lecturas_url = http://201.156.230.48:7001/lecturas/TiempoLecturas/Get?informacion_usuario_id=1265 - 2021-03-12 17:08:35",
            "1615590515089" : "App - muestraValoresEgesio - uuid = EE:BF:60:20:A5:12 - 2021-03-12 17:08:35",
            "1615590515090" : "App - muestraValoresEgesio - usuario_informacion_id = 1265 - 2021-03-12 17:08:35",*/
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TOKEN_SEND,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIxMjlkYzhhOS0zNTAwLTQ2NWYtOGM4Mi0yMDkxMGU4ZDlhYWIiLCJuYW1laWQiOiJZV1J0YVc1RVpYWT0iLCJuYmYiOjE2MTU1OTAzMjYsImV4cCI6MTYxNTYzMzUyNn0.OLEDnyqIaPPcmS3WI250HDMyNq3vEIc9U9y9OAvgDSM");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDIOMA_SEND,"es");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.GATEWAY_URL,"http://201.156.230.48:3030/");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INFORMACION_USUARIO_ID,"1265");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TIEMPO_LECTURAS,"http://201.156.230.48:3030/lecturas/TiempoLecturas/Get?informacion_usuario_id=945");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.MACADDRESS, "EE:BF:60:20:A5:12");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDPULSERA, "10");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.PERIODO, "180");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.REGISTROS_LECTURA, "");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.URL_SERVICE_EGESIO, "http://201.156.230.48:3030/lecturas/lecturas/PostList");


//PRODUCCION
/*
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TOKEN_SEND,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzMTVmZWZhOS1mZmI2LTRlNGMtODk4Ni05ZWJiMWY5N2NjNmIiLCJuYW1laWQiOiJZV1J0YVc1RVpYWT0iLCJuYmYiOjE2MTQ4OTEyNjEsImV4cCI6MTYxNDkzNDQ2MX0.TytKD_o55bVIMnzo8SZqcDob9zE0_J-rYqKpnuj91mg");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDIOMA_SEND,"es");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.GATEWAY_URL,"https://gatewayparatodos.egesio.online/");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INFORMACION_USUARIO_ID,"1489");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TIEMPO_LECTURAS,"https://gatewayparatodos.egesio.online/lecturas/TiempoLecturas/Get?informacion_usuario_id=1489");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.MACADDRESS, "EE:BF:60:20:A9:D9");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDPULSERA, "737");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.PERIODO, "180");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.REGISTROS_LECTURA, "");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.URL_SERVICE_EGESIO, "https://gatewayparatodos.egesio.online/lecturas/lecturas/PostList");
*/



            Utils.myContext = this;

            /*

2021-02-03 17:30:25.138 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: token_jwt : "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI0ZTIyMTkwMS0xZmNiLTQzOTQtODYzOS0wNTA0ZGE4YWVkMGYiLCJuYW1laWQiOiI3OTAiLCJ1c3VhcmlvIjoiZWRnYXIubWFydGluZXpAcGVvcGxlLW1lZGlhLmNvbS5teCIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IlBlcnNvbmEiLCJuYmYiOjE2MTE5NjQ1ODMsImV4cCI6MTYxOTc0MDU4M30.S7IgiplwQ7h9xoTX9YnRDIr5tyq3EkwhH14Y0rbl3p0"
2021-02-03 17:30:25.139 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: pms_gateway_url : "http://201.156.230.48:3030/"
2021-02-03 17:30:25.139 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: post_lecturas_url : "http://201.156.230.48:3030/lecturas/lecturas/PostList"
2021-02-03 17:30:25.139 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: instancia_id : 7
2021-02-03 17:30:25.140 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: dispositivo_id : 117
2021-02-03 17:30:25.140 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: get_tiempo_lecturas_url : "http://201.156.230.48:3030/lecturas/TiempoLecturas/Get?informacion_usuario_id=945"
2021-02-03 17:30:25.141 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: uuid : "EE:BF:60:20:A9:D9"
2021-02-03 17:30:25.141 10332-10332/com.pmsoluciones.egesiox D/EGESIOAV: usuario_informacion_id : 945




             */


            // Inicia el servicio
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INICIA_SERVICIO_MANUAL, "1");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.ENVIADO_DATOS, "false");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INTENTOS_HIST, "0");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INICIA_PROCESO, "1");

            Utils.iniciaServicio(v.getContext());
            //Utils.prendeTodosLosSensores(v.getContext());



            try{
                Timer mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String respuesta = "disconnected";
                        try{

                           if(Utils.isDeviceConnect){
                               LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + "Iniciamos proceso de historico - " + respuesta);
                                Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.DETENER_SERVICIO_MANUAL, "0");
                                Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INTENTOS_HIST, "0");
                                HistorialLecturasProcess historialLecturasProcess = new HistorialLecturasProcess(v.getContext());
                                historialLecturasProcess.iniciaProceso(1);
                                mTimer.cancel();
                            }
                        }catch(Exception e){

                        }


                    }
                }, 100, 3000);
            }catch (Exception e){
                Log.d("Egesio", e.getMessage());
            }


        }else if(R.id.buttonStop ==  v.getId()){
            //Proceso para obtener Lecturas de sueño
/*
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TOKEN_SEND,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIxYTc2ZmQwZC1jYjVmLTRkMDgtOGJjYy1hZjJmYjhlNWQ4NzMiLCJuYW1laWQiOiJZV1J0YVc1RVpYWT0iLCJuYmYiOjE2MTQ5NzgyMDQsImV4cCI6MTYxNTAyMTQwNH0._nSD5q46XXJ01hdMtQqgI99DdAV_lPz6kHkkMWuyADE");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDIOMA_SEND,"es");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.GATEWAY_URL,"http://201.156.230.48:3030/");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.INFORMACION_USUARIO_ID,"945");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.TIEMPO_LECTURAS,"http://201.156.230.48:3030/lecturas/TiempoLecturas/Get?informacion_usuario_id=945");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.MACADDRESS, "EE:BF:60:20:A9:D9");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.IDPULSERA, "117");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.PERIODO, "180");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.REGISTROS_LECTURA, "");
            Sharedpreferences.getInstance(v.getContext()).escribeValorString(Constans.URL_SERVICE_EGESIO, "http://201.156.230.48:3030/lecturas/lecturas/PostList");
*/




        }else if(R.id.buttonConectDevice ==  v.getId()){
            Utils.iniciaProcesoLectura(v.getContext());
        }else if(R.id.buttonDisconectDevice ==  v.getId()){
            //Utils.detenProcesoLectura(v.getContext());
        }else if(R.id.buttonGetValores ==  v.getId()){
            //Utils.obtenTodosValoresJSON(v.getContext());
        }else if(R.id.buttonSendData ==  v.getId()){
            //Utils.enviaDatosEgesioDB(v.getContext());
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
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        Intent intent = new Intent();
        String packageName = getApplicationContext().getPackageName();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(getApplicationContext().POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            getApplicationContext().startActivity(intent);
        }else{
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        }
    }

}