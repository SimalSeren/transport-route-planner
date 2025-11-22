package prolab1.model;

public class Tramvay extends TopluTasima {
    public Tramvay() {
        super("tramvay");
    }

    @Override
    public double durakBasiUcret(Durak suankiDurak, Durak sonrakiDurak) {
        if (suankiDurak.getNextStops() != null) {
            for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                if (sonraki.getStopId().equals(sonrakiDurak.getId())) {
                    return sonraki.getUcret();
                }
            }
        }
        return 0.0;
    }
}