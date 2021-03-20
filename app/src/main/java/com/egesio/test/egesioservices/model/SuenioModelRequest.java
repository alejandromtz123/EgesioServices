package com.egesio.test.egesioservices.model;

public class SuenioModelRequest {

    private String fechaInicio;
    private String fechaFin;
    private int tipo;
    private int tiempoContinuo;

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getTiempoContinuo() {
        return tiempoContinuo;
    }

    public void setTiempoContinuo(int tiempoContinuo) {
        this.tiempoContinuo = tiempoContinuo;
    }
}
