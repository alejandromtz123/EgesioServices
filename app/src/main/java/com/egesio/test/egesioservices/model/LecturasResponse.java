package com.egesio.test.egesioservices.model;

public class LecturasResponse {
    private String lectura_valor;
    private String lectura_fecha;
    private String dispositivo_nombre;
    private String dispositivo_direccion_mac;
    private String parametro_descripcion;
    private String parametro_nombre;
    private String parametro_id;
    private String lectura_fuera_rango;
    private String patologia_id;
    private String patologia_nombre;
    private String patologia_descripcion;
    private String patologia_codigo_color;
    private String rango_clave;
    private String rango_comentario;
    private String rango_comentario_ingles;

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

    public String getLectura_fuera_rango() {
        return lectura_fuera_rango;
    }

    public void setLectura_fuera_rango(String lectura_fuera_rango) {
        this.lectura_fuera_rango = lectura_fuera_rango;
    }

    public String getPatologia_id() {
        return patologia_id;
    }

    public void setPatologia_id(String patologia_id) {
        this.patologia_id = patologia_id;
    }

    public String getPatologia_nombre() {
        return patologia_nombre;
    }

    public void setPatologia_nombre(String patologia_nombre) {
        this.patologia_nombre = patologia_nombre;
    }

    public String getPatologia_descripcion() {
        return patologia_descripcion;
    }

    public void setPatologia_descripcion(String patologia_descripcion) {
        this.patologia_descripcion = patologia_descripcion;
    }

    public String getPatologia_codigo_color() {
        return patologia_codigo_color;
    }

    public void setPatologia_codigo_color(String patologia_codigo_color) {
        this.patologia_codigo_color = patologia_codigo_color;
    }

    public String getRango_clave() {
        return rango_clave;
    }

    public void setRango_clave(String rango_clave) {
        this.rango_clave = rango_clave;
    }

    public String getRango_comentario() {
        return rango_comentario;
    }

    public void setRango_comentario(String rango_comentario) {
        this.rango_comentario = rango_comentario;
    }

    public String getRango_comentario_ingles() {
        return rango_comentario_ingles;
    }

    public void setRango_comentario_ingles(String rango_comentario_ingles) {
        this.rango_comentario_ingles = rango_comentario_ingles;
    }
}
