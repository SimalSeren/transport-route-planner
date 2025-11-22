package prolab1.model;

public abstract class Arac {
    protected String tur;

    public String getTur() { return tur; }
    public abstract double mesafeBasiUcret(double mesafe);
    public abstract double durakBasiUcret(Durak suankiDurak, Durak sonrakiDurak);
}