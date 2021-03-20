package com.egesio.test.egesioservices.utils;

import android.util.Log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogUtil implements Serializable {
    private static final String IDENT = "MessageEgesioCorp1.8.21 - ";
    private static boolean DEBUGGE = false;
    private static boolean DEBUGGE_FIREBASE = true;

    public static void Imprime(final String _TAG, final String _MESSAGE){
        try{
            if(DEBUGGE){
                Log.d(_TAG, _TAG + " - " + IDENT + _MESSAGE);
            }
            if(DEBUGGE_FIREBASE){
                try {

                    if(Utils.myContext != null) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime now = LocalDateTime.now();
                        String fecha = dtf.format(now);
                        Map data = new HashMap();
                        data.put(new Date().getTime(), _TAG + " - " + _MESSAGE + " - " + fecha);
                        Utils.lecturasLogFirebase.add(data);
                        String _separador = ",";
                        Log.d("doInBackgFirebaseSend", "Tamaño = " + Utils.lecturasLogFirebase.size());
                        if(Utils.lecturasLogFirebase.size() > 40){
                            Log.d("doInBackgFirebaseSend", "Entre = ");
                            String jsonSend = "";
                            for(int i = 0; i < Utils.lecturasLogFirebase.size() ; i++){
                                if((i+1) == Utils.lecturasLogFirebase.size())
                                    _separador = "";
                                Long myKey = 0L;
                                String myValue = "";
                                try{
                                    myKey = (Long)Utils.lecturasLogFirebase.get(i).keySet().toArray()[0];
                                    myValue = Utils.lecturasLogFirebase.get(i).get(myKey).replace("\"", "");
                                }catch (Exception e){
                                    myKey = 0L;
                                    myValue = "";
                                    Log.d("doInBackgFirebaseSend", "Error = " + e.getMessage());
                                }
                                jsonSend += "\"" + myKey + "\": \"" + Utils.lecturasLogFirebase.get(i).get(myKey).replace("\"", "").replace("{","").replace("}","") + "\"" + _separador;
                            }
                            Log.d("doInBackgFirebaseSend", "Antes de enviar = ");
                            Log.d("doInBackgFirebaseSend", "Json = " + jsonSend);
                            Utils.lecturasLogFirebase.removeAll(Utils.lecturasLogFirebase);
                            new SendDataFirebase(Utils.myContext).execute("{" + jsonSend + "}");
                        }
                    }
                }catch (Exception e){
                    Log.d(_TAG, IDENT + _MESSAGE);
                }
            }
        }catch (Exception e){
            Log.d(_TAG, IDENT + _MESSAGE);
        }
    }
}