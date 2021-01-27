package com.egesio.test.egesioservices.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Medida {

    private int dispositivo_id;
    private String valor;
    private int dispositivo_parametro_id;
    private String fecha;
    private String idioma;
    private boolean bnd_store_foward;

    public int getDispositivo_id() {
        return dispositivo_id;
    }

    public void setDispositivo_id(int dispositivo_id) {
        this.dispositivo_id = dispositivo_id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getDispositivo_parametro_id() {
        return dispositivo_parametro_id;
    }

    public void setDispositivo_parametro_id(int dispositivo_parametro_id) {
        this.dispositivo_parametro_id = dispositivo_parametro_id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public boolean isBnd_store_foward() {
        return bnd_store_foward;
    }

    public void setBnd_store_foward(boolean bnd_store_foward) {
        this.bnd_store_foward = bnd_store_foward;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "[{}]";
        }

    }
}
