package prolab1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Aktarma {
    @JsonProperty("transferStopId")
    private String transferStopId;

    @JsonProperty("transferSure")
    private int transferSure;

    @JsonProperty("transferUcret")
    private double transferUcret;

    public Aktarma() {
    }

    public String getTransferStopId() {
        return transferStopId;
    }

    public void setTransferStopId(String transferStopId) {
        this.transferStopId = transferStopId;
    }

    public int getTransferSure() {
        return transferSure;
    }

    public void setTransferSure(int transferSure) {
        this.transferSure = transferSure;
    }

    public double getTransferUcret() {
        return transferUcret;
    }

    public void setTransferUcret(double transferUcret) {
        this.transferUcret = transferUcret;
    }
}