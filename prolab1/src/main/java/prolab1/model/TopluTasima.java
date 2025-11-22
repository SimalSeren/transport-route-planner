package prolab1.model;

public abstract class TopluTasima extends Arac {
    public TopluTasima(String tur) {
        this.tur = tur;
    }

    @Override
    public double mesafeBasiUcret(double mesafe) {
        throw new UnsupportedOperationException("Toplu taşıma için mesafe bazlı ücret hesaplanmaz.");
    }

    @Override
    public abstract double durakBasiUcret(Durak suankiDurak, Durak sonrakiDurak);
}