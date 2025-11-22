package prolab1.model;

public abstract class Yolcu {
    private String ad;
    private double indirimOrani;

    public Yolcu(String ad, double indirimOrani) {
        this.ad = ad;
        this.indirimOrani = indirimOrani;
    }

    public String getAd() { return ad; }
    public double getIndirimOrani() { return indirimOrani; }
    public abstract double ucretHesapla(double bazUcret);
}