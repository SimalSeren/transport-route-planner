package prolab1.service;

import prolab1.model.*;
import prolab1.util.MesafeHesaplayici;
import prolab1.exception.RotaBulunamadiException;

import java.util.*;

public class RotaPlanlayici {
    private SehirVerisi sehirVerisi;
    private Yolcu yolcu;
    private Taksi taksi;

    public RotaPlanlayici(SehirVerisi sehirVerisi, Yolcu yolcu) {
        this.sehirVerisi = sehirVerisi;
        this.yolcu = yolcu;
        this.taksi = sehirVerisi.getTaxi();
        System.out.println("Taksi Açılış Ücreti: " + taksi.getOpeningFee() + " TL");
        System.out.println("Taksi Kilometre Başına Ücret: " + taksi.getCostPerKm() + " TL/km");
    }

    public static class DurakMesafeSonuc {
        private Durak durak;
        private double mesafe;
        private String atlananDurakMesaji;

        public DurakMesafeSonuc(Durak durak, double mesafe, String atlananDurakMesaji) {
            this.durak = durak;
            this.mesafe = mesafe;
            this.atlananDurakMesaji = atlananDurakMesaji;
        }

        public Durak getDurak() {
            return durak;
        }

        public double getMesafe() {
            return mesafe;
        }

        public String getAtlananDurakMesaji() {
            return atlananDurakMesaji;
        }
    }

    public DurakMesafeSonuc enYakinDuragiBul(double enlem, double boylam) {
        Durak enYakinDurak = null;
        double enKisaMesafe = Double.MAX_VALUE;
        String atlananDurakMesaji = null;

        for (Durak durak : sehirVerisi.getDuraklar()) {
            double mesafe = MesafeHesaplayici.haversine(enlem, boylam, durak.getLat(), durak.getLon());
            if (mesafe < enKisaMesafe) {
                if (durak.getNextStops() == null && durak.getTransfer() == null) {
                    atlananDurakMesaji = "Uyarı: " + durak.getName() + " durağına rota planlanamıyor, bir sonraki en yakın durak aranıyor.";
                    continue;
                }
                enKisaMesafe = mesafe;
                enYakinDurak = durak;
                atlananDurakMesaji = null;
            }
        }

        return new DurakMesafeSonuc(enYakinDurak, enKisaMesafe, atlananDurakMesaji);
    }

    public double taksiMaliyetHesapla(double mesafe) {
        double maliyet = taksi.mesafeBasiUcret(mesafe);
        System.out.println("Taksi Maliyeti Hesaplama - Mesafe: " + mesafe + " km, Maliyet: " + maliyet + " TL");
        return maliyet;
    }

    public Rota rotaHesapla(String baslangicDurakId, String hedefDurakId) throws RotaBulunamadiException {
        Map<String, Durak> durakMap = new HashMap<>();
        for (Durak durak : sehirVerisi.getDuraklar()) {
            durakMap.put(durak.getId(), durak);
        }

        Map<String, Double> mesafeler = new HashMap<>();
        Map<String, String> oncekiDuraklar = new HashMap<>();
        Map<String, Double> ucretler = new HashMap<>();
        Map<String, Integer> sureler = new HashMap<>();
        Set<String> ziyaretEdilen = new HashSet<>();

        for (Durak durak : sehirVerisi.getDuraklar()) {
            mesafeler.put(durak.getId(), Double.MAX_VALUE);
            ucretler.put(durak.getId(), 0.0);
            sureler.put(durak.getId(), 0);
        }

        mesafeler.put(baslangicDurakId, 0.0);

        PriorityQueue<String> kuyruk = new PriorityQueue<>(Comparator.comparing(mesafeler::get));
        kuyruk.add(baslangicDurakId);

        while (!kuyruk.isEmpty()) {
            String suankiDurakId = kuyruk.poll();
            if (ziyaretEdilen.contains(suankiDurakId)) continue;
            ziyaretEdilen.add(suankiDurakId);

            Durak suankiDurak = durakMap.get(suankiDurakId);
            if (suankiDurak == null) continue;

            List<SonrakiDurak> sonrakiDuraklar = new ArrayList<>();
            if (suankiDurak.getNextStops() != null) {
                sonrakiDuraklar.addAll(suankiDurak.getNextStops());
            }
            if (suankiDurak.getTransfer() != null) {
                SonrakiDurak transferDurak = new SonrakiDurak(
                        suankiDurak.getTransfer().getTransferStopId(),
                        suankiDurak.getTransfer().getTransferUcret(),
                        suankiDurak.getTransfer().getTransferSure()
                );
                sonrakiDuraklar.add(transferDurak);
            }

            for (SonrakiDurak sonraki : sonrakiDuraklar) {
                String sonrakiDurakId = sonraki.getStopId();
                if (ziyaretEdilen.contains(sonrakiDurakId)) continue;

                Durak sonrakiDurak = durakMap.get(sonrakiDurakId);
                if (sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );

                double yeniMesafe = mesafeler.get(suankiDurakId) + duraklarArasiMesafe;
                if (yeniMesafe < mesafeler.get(sonrakiDurakId)) {
                    mesafeler.put(sonrakiDurakId, yeniMesafe);
                    ucretler.put(sonrakiDurakId, ucretler.get(suankiDurakId) + sonraki.getUcret());
                    sureler.put(sonrakiDurakId, sureler.get(suankiDurakId) + sonraki.getSure());
                    oncekiDuraklar.put(sonrakiDurakId, suankiDurakId);
                    kuyruk.add(sonrakiDurakId);
                }
            }
        }

        if (!oncekiDuraklar.containsKey(hedefDurakId)) {
            throw new RotaBulunamadiException("Hedef durağa rota bulunamadı: " + hedefDurakId + ". Lütfen başka bir hedef seçin.");
        }

        List<String> duraklar = new ArrayList<>();
        String suanki = hedefDurakId;
        while (suanki != null) {
            duraklar.add(suanki);
            suanki = oncekiDuraklar.get(suanki);
        }
        Collections.reverse(duraklar);

        int aktarmaSayisi = 0;
        for (int i = 0; i < duraklar.size() - 1; i++) {
            String suankiDurakId = duraklar.get(i);
            String sonrakiDurakId = duraklar.get(i + 1);
            Durak suankiDurak = durakMap.get(suankiDurakId);
            if (suankiDurak.getTransfer() != null && suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurakId)) {
                aktarmaSayisi++;
            }
        }

        return new Rota(duraklar, mesafeler.get(hedefDurakId), ucretler.get(hedefDurakId), sureler.get(hedefDurakId), aktarmaSayisi);
    }

    public Rota enAzAktarmaliRotaHesapla(String baslangicDurakId, String hedefDurakId) throws RotaBulunamadiException {
        Map<String, Durak> durakMap = new HashMap<>();
        for (Durak durak : sehirVerisi.getDuraklar()) {
            durakMap.put(durak.getId(), durak);
        }

        Map<String, Integer> aktarmaSayilari = new HashMap<>();
        Map<String, String> oncekiDuraklar = new HashMap<>();
        Map<String, Double> mesafeler = new HashMap<>();
        Map<String, Integer> sureler = new HashMap<>();
        Set<String> ziyaretEdilen = new HashSet<>();

        for (Durak durak : sehirVerisi.getDuraklar()) {
            aktarmaSayilari.put(durak.getId(), Integer.MAX_VALUE);
            mesafeler.put(durak.getId(), Double.MAX_VALUE);
            sureler.put(durak.getId(), 0);
        }

        aktarmaSayilari.put(baslangicDurakId, 0);
        mesafeler.put(baslangicDurakId, 0.0);

        PriorityQueue<String> kuyruk = new PriorityQueue<>(Comparator.comparing(aktarmaSayilari::get));
        kuyruk.add(baslangicDurakId);

        while (!kuyruk.isEmpty()) {
            String suankiDurakId = kuyruk.poll();
            if (ziyaretEdilen.contains(suankiDurakId)) continue;
            ziyaretEdilen.add(suankiDurakId);

            Durak suankiDurak = durakMap.get(suankiDurakId);
            if (suankiDurak == null) continue;

            List<SonrakiDurak> sonrakiDuraklar = new ArrayList<>();
            if (suankiDurak.getNextStops() != null) {
                sonrakiDuraklar.addAll(suankiDurak.getNextStops());
            }
            if (suankiDurak.getTransfer() != null) {
                SonrakiDurak transferDurak = new SonrakiDurak(
                        suankiDurak.getTransfer().getTransferStopId(),
                        suankiDurak.getTransfer().getTransferUcret(),
                        suankiDurak.getTransfer().getTransferSure()
                );
                sonrakiDuraklar.add(transferDurak);
            }

            for (SonrakiDurak sonraki : sonrakiDuraklar) {
                String sonrakiDurakId = sonraki.getStopId();
                if (ziyaretEdilen.contains(sonrakiDurakId)) continue;

                Durak sonrakiDurak = durakMap.get(sonrakiDurakId);
                if (sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                int yeniAktarmaSayisi = aktarmaSayilari.get(suankiDurakId);
                if (suankiDurak.getTransfer() != null && suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurakId)) {
                    yeniAktarmaSayisi++;
                }

                if (yeniAktarmaSayisi < aktarmaSayilari.get(sonrakiDurakId)) {
                    aktarmaSayilari.put(sonrakiDurakId, yeniAktarmaSayisi);
                    mesafeler.put(sonrakiDurakId, mesafeler.get(suankiDurakId) + duraklarArasiMesafe);
                    sureler.put(sonrakiDurakId, sureler.get(suankiDurakId) + sonraki.getSure());
                    oncekiDuraklar.put(sonrakiDurakId, suankiDurakId);
                    kuyruk.add(sonrakiDurakId);
                }
            }
        }

        if (!oncekiDuraklar.containsKey(hedefDurakId)) {
            throw new RotaBulunamadiException("En az aktarmalı rota bulunamadı: " + hedefDurakId);
        }

        List<String> duraklar = new ArrayList<>();
        String suanki = hedefDurakId;
        while (suanki != null) {
            duraklar.add(suanki);
            suanki = oncekiDuraklar.get(suanki);
        }
        Collections.reverse(duraklar);

        int aktarmaSayisi = aktarmaSayilari.get(hedefDurakId);
        return new Rota(duraklar, mesafeler.get(hedefDurakId), 0.0, sureler.get(hedefDurakId), aktarmaSayisi);
    }
}