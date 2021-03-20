package com.egesio.test.egesioservices.model;

import com.egesio.test.egesioservices.app.App;
import com.egesio.test.egesioservices.utils.LogUtil;
import com.egesio.test.egesioservices.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class LecturasRequest{

    private final static String TAG = LecturasRequest.class.getSimpleName();

    private Integer id;
    private Integer informacion_usuario_id; // id información usuario.
    private String valor;
    private Integer dispositivo_id;
    private Integer tipo_operacion;
    private String fecha;
    private boolean bnd_store_foward;
    private Integer dispositivo_parametro_id; // Filtrar por el tipo de lectura.
    private String fecha_inicio; // Filtrar desde la fecha.
    private String fecha_fin; // Filtrar desde hasta fecha.
    private Integer numero_lecturas; // Para traer las últimas N lecturas del usuario
    private String idioma;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getInformacion_usuario_id() {
        return informacion_usuario_id;
    }

    public void setInformacion_usuario_id(Integer informacion_usuario_id) {
        this.informacion_usuario_id = informacion_usuario_id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Integer getDispositivo_id() {
        return dispositivo_id;
    }

    public void setDispositivo_id(Integer dispositivo_id) {
        this.dispositivo_id = dispositivo_id;
    }

    public Integer getTipo_operacion() {
        return tipo_operacion;
    }

    public void setTipo_operacion(Integer tipo_operacion) {
        this.tipo_operacion = tipo_operacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public boolean isBnd_store_foward() {
        return bnd_store_foward;
    }

    public void setBnd_store_foward(boolean bnd_store_foward) {
        this.bnd_store_foward = bnd_store_foward;
    }

    public Integer getDispositivo_parametro_id() {
        return dispositivo_parametro_id;
    }

    public void setDispositivo_parametro_id(Integer dispositivo_parametro_id) {
        this.dispositivo_parametro_id = dispositivo_parametro_id;
    }

    public String getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public String getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public Integer getNumero_lecturas() {
        return numero_lecturas;
    }

    public void setNumero_lecturas(Integer numero_lecturas) {
        this.numero_lecturas = numero_lecturas;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String toJSON(){
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("dispositivo_id", getDispositivo_id());
            jsonObject.put("valor", getValor());
            jsonObject.put("dispositivo_parametro_id", getDispositivo_parametro_id());
            jsonObject.put("fecha", getFecha());
            jsonObject.put("idioma", getIdioma());
            jsonObject.put("bnd_store_foward", isBnd_store_foward());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.Imprime(TAG,  Utils.getNombreMetodo() + " - " + e.getMessage());
            return "[{}]";
        }
    }
}
