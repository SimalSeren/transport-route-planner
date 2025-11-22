package prolab1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Taksi extends Arac {
    @JsonProperty("openingFee")
    private double openingFee;

    @JsonProperty("costPerKm")
    private double costPerKm;

    public Taksi() {
        this.tur = "taksi";
        this.openingFee = 0.0;
        this.costPerKm = 0.0;
    }

    public Taksi(double openingFee, double costPerKm) {
        this.tur = "taksi";
        this.openingFee = openingFee;
        this.costPerKm = costPerKm;
    }

    public double getOpeningFee() {
        return openingFee;
    }

    public void setOpeningFee(double openingFee) {
        this.openingFee = openingFee;
    }

    public double getCostPerKm() {
        return costPerKm;
    }

    public void setCostPerKm(double costPerKm) {
        this.costPerKm = costPerKm;
    }

    @Override
    public double mesafeBasiUcret(double mesafe) {
        return openingFee + (mesafe * costPerKm);
    }

    @Override
    public double durakBasiUcret(Durak suankiDurak, Durak sonrakiDurak) {
        throw new UnsupportedOperationException("Taksi için durak bazlı ücret hesaplanmaz.");
    }
}