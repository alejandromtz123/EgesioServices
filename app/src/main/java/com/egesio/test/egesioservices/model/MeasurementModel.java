package com.egesio.test.egesioservices.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MeasurementModel {
    private Integer bloodOxygen;
    private Integer heartRate;
    private Double temperature;
    private String bloodPressure;
    private String nextMeasurementDate;
    private String fecha;

    public Integer getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(Integer bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getNextMeasurementDate() {
        return nextMeasurementDate;
    }

    public void setNextMeasurementDate(String nextMeasurementDate) {
        this.nextMeasurementDate = nextMeasurementDate;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String toJSON(){
        String respI =  "{";
        String json = "";
        String respF =  "}";
        JSONObject jsonObject= new JSONObject();
        try {
            /*jsonObject.put("bloodOxygen", getBloodOxygen());
            jsonObject.put("heartRate", getHeartRate());
            jsonObject.put("temperature", getTemperature());
            jsonObject.put("bloodPressure", getBloodPressure());
            jsonObject.put("nextMeasurementDate", getNextMeasurementDate());
            jsonObject.put("fecha", getFecha());
            resp = jsonObject.toString();*/

            json += "\"bloodOxygen\"" + ":" +  getBloodOxygen() + ",";
            json += "\"heartRate\"" + ":" +  getHeartRate() + ",";
            json += "\"temperature\"" + ":" +  getTemperature() + ",";
            json += "\"bloodPressure\"" + ":" + ((getBloodPressure() == null) ? null : "\"" + getBloodPressure() + "\"") + ",";
            json += "\"nextMeasurementDate\"" + ":" + ((getNextMeasurementDate() == null) ? null : "\"" + getNextMeasurementDate() + "\"") + ",";
            json += "\"fecha\"" + ":" +  ((getFecha() == null) ? null : "\"" + getFecha() + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return respI + json + respF;

    }
}
