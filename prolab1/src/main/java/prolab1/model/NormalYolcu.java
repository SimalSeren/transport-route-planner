package prolab1.model;

public class NormalYolcu extends Yolcu {
    public NormalYolcu(String ad) {
        super(ad, 0.0);
    }

    @Override
    public double ucretHesapla(double bazUcret) {
        return bazUcret;
    }
}