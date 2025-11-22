package prolab1.model;

public class Yasli extends Yolcu {
    public Yasli(String ad) {
        super(ad, 0.75);
    }

    @Override
    public double ucretHesapla(double bazUcret) {
        return bazUcret * (1 - getIndirimOrani());
    }
}