package prolab1.model;

public class Ogrenci extends Yolcu {
    public Ogrenci(String ad) {
        super(ad, 0.5);
    }

    @Override
    public double ucretHesapla(double bazUcret) {
        return bazUcret * (1 - getIndirimOrani());
    }
}