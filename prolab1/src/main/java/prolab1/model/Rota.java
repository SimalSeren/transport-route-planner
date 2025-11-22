package prolab1.model;

import java.util.List;

public class Rota {
    private List<String> duraklar;
    private double toplamMesafe;
    private double toplamUcret;
    private int toplamSure;
    private int aktarmaSayisi;

    public Rota(List<String> duraklar, double toplamMesafe, double toplamUcret, int toplamSure, int aktarmaSayisi) {
        this.duraklar = duraklar;
        this.toplamMesafe = toplamMesafe;
        this.toplamUcret = toplamUcret;
        this.toplamSure = toplamSure;
        this.aktarmaSayisi = aktarmaSayisi;
    }

    public List<String> getDuraklar() {
        return duraklar;
    }

    public double getToplamMesafe() {
        return toplamMesafe;
    }

    public double getToplamUcret() {
        return toplamUcret;
    }

    public int getToplamSure() {
        return toplamSure;
    }

    public int getAktarmaSayisi() {
        return aktarmaSayisi;
    }
}