package com.egesio.test.egesioservices.constants;

public class Constans {
    public final static String EXTRA_SEND_DATA_TO_BLE = "EXTRA_SEND_DATA_TO_BLE";
    public final static String ADDRESS = "address";
    public final static String NAME = "name";

    public final static String URL_DEV_SERV = "http://201.156.230.48:3030/lecturas/lecturas/PostList";

    // Credenciales para obtener un JWT y poder hacer peticiones (POST).
    public final static String URL_AUTH = "http://201.156.230.48:3030/egesio/authenticate";
    /*
    {
        usuario: "adminDev",
        contrasenia: "admin123456"
    }
    */

    public final static String TOKENTMPBARER = "Barer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzYWY1ZmNkMi00MjU2LTRjOTMtOGJiMi1kOGU4YWIxNjViMDUiLCJuYW1laWQiOiI3OTAiLCJ1c3VhcmlvIjoiZWRnYXIubWFydGluZXpAcGVvcGxlLW1lZGlhLmNvbS5teCIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IlBlcnNvbmEiLCJuYmYiOjE2MTAwNDc4MDksImV4cCI6MTYxNzgyMzgwOX0.QVLImdmRQS5pmD5li03TdZTpVV-HIT-FKzI5fQIm-OY";

    public final static int timeout = 300000;

    public static final String MyPREFERENCES = "EgesioPref";
    public static final String PREFS_NAME_EGESIO = "NativeStorage";
    public static final String MACADDRESS = "macAddressKey";
    public static final String IDPULSERA = "idPuslseraKey";
    public static final String PERIODO = "periodoKey";
    public static final String REGISTROS_LECTURA = "registros_lectura_adroid";

    public static final int LECTURAS = 15;

    public static final String USER_KEY = "USER_KEY";
    public static final String PASSWORD_KEY = "PASSWORD_KEY";
    public static final String TOKEN_KEY = "TOKEN_KEY";

    public static final String TIEMPO_LECTURAS = "TIEMPO_LECTURAS";
    public static final String GATEWAY_URL = "GATEWAY_URL";
    public static final String INFORMACION_USUARIO_ID = "INFORMACION_USUARIO_ID";

    public static final String TEMPERATURE_KEY = "TEMPERATURE_KEY";
    public static final String HEART_KEY = "HEART_KEY";
    public static final String BLOOD_OXYGEN_KEY = "BLOOD_OXYGEN_KEY";
    public static final String BLOOD_PRESSURE_KEY = "BLOOD_PRESSURE_KEY";

    public static final String COUNT_GENERAL = "COUNT_GENERAL";
    public static final String LAST_TIME_GENERAL = "LAST_TIME_GENERAL";

    public static final String IDIOMA_SEND = "IDIOMA_SEND";
    public static final String TOKEN_SEND = "TOKEN_SEND";
    public static final String REFRESH_TOKEN_SEND = "REFRESH_TOKEN_SEND";
    public static final String URL_SERVICE_EGESIO = "URL_SERVICE_EGESIO";

    public static final int modelo_dispositivo_parametros_temperatura = 1;
    public static final int modelo_dispositivo_parametros_ritmo_cardiaco = 2;
    public static final int modelo_dispositivo_parametros_oxigenacion_sangre = 3;
    public static final int modelo_dispositivo_parametros_presion_arterial = 4;

    public static final String FECHA_ULTIMA_LECTURA = "FECHA_ULTIMA_LECTURA";
    public static final String FECHA_PROXIMA_LECTURA = "FECHA_PROXIMA_LECTURA";

}
