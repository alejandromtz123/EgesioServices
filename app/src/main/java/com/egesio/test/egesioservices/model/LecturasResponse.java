package com.egesio.test.egesioservices.model;

public class LecturasResponse {
    private String lectura_valor;
    private String lectura_fecha;
    private String dispositivo_nombre;
    private String dispositivo_direccion_mac;
    private String parametro_descripcion;
    private String parametro_nombre;
    private String parametro_id;

    public String getLectura_valor() {
        return lectura_valor;
    }

    public void setLectura_valor(String lectura_valor) {
        this.lectura_valor = lectura_valor;
    }

    public String getLectura_fecha() {
        return lectura_fecha;
    }

    public void setLectura_fecha(String lectura_fecha) {
        this.lectura_fecha = lectura_fecha;
    }

    public String getDispositivo_nombre() {
        return dispositivo_nombre;
    }

    public void setDispositivo_nombre(String dispositivo_nombre) {
        this.dispositivo_nombre = dispositivo_nombre;
    }

    public String getDispositivo_direccion_mac() {
        return dispositivo_direccion_mac;
    }

    public void setDispositivo_direccion_mac(String dispositivo_direccion_mac) {
        this.dispositivo_direccion_mac = dispositivo_direccion_mac;
    }

    public String getParametro_descripcion() {
        return parametro_descripcion;
    }

    public void setParametro_descripcion(String parametro_descripcion) {
        this.parametro_descripcion = parametro_descripcion;
    }

    public String getParametro_id() {
        return parametro_id;
    }

    public String getParametro_nombre() {
        return parametro_nombre;
    }

    public void setParametro_nombre(String parametro_nombre) {
        this.parametro_nombre = parametro_nombre;
    }

    public void setParametro_id(String parametro_id) {
        this.parametro_id = parametro_id;
    }
}
