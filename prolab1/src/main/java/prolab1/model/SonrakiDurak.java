package prolab1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SonrakiDurak {
    @JsonProperty("stopId")
    private String stopId;

    @JsonProperty("mesafe")
    private double mesafe;

    @JsonProperty("sure")
    private int sure;

    @JsonProperty("ucret")
    private double ucret;

    public SonrakiDurak() {
    }

    public SonrakiDurak(String stopId, double ucret, int sure) {
        this.stopId = stopId;
        this.ucret = ucret;
        this.sure = sure;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public double getMesafe() {
        return mesafe;
    }

    public void setMesafe(double mesafe) {
        this.mesafe = mesafe;
    }

    public int getSure() {
        return sure;
    }

    public void setSure(int sure) {
        this.sure = sure;
    }

    public double getUcret() {
        return ucret;
    }

    public void setUcret(double ucret) {
        this.ucret = ucret;
    }
}