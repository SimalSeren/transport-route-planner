package prolab1.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import prolab1.model.*;
import prolab1.service.RotaPlanlayici;
import prolab1.util.MesafeHesaplayici;
import prolab1.exception.GecersizKonumException;

import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Ana {
    static class RotaSonuc {
        String rotaTipi;
        double toplamMesafe;
        double toplamMaliyet;
        int toplamSure;
        int aktarmaSayisi;
        List<String> adimlar;
        LocalTime varisZamani;
        private boolean yurumeIcerir;
        private double yurumeMesafesi;
        private double skor;

        RotaSonuc(String rotaTipi) {
            this.rotaTipi = rotaTipi;
            this.toplamMesafe = 0.0;
            this.toplamMaliyet = 0.0;
            this.toplamSure = 0;
            this.aktarmaSayisi = 0;
            this.adimlar = new ArrayList<>();
            this.yurumeIcerir = false;
            this.yurumeMesafesi = 0.0;
            this.skor = 0.0;
        }

        void setVarisZamani(String baslangicZamani, int toplamSure) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime baslangic = LocalTime.parse(baslangicZamani, formatter);
            this.varisZamani = baslangic.plusMinutes(toplamSure);
        }

        String getVarisZamani() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return varisZamani.format(formatter);
        }

        void ekleYurume(double mesafe, String aciklama) {
            this.yurumeIcerir = true;
            this.yurumeMesafesi = mesafe;
            this.toplamMesafe += mesafe;
            this.toplamSure += (int) (mesafe / 0.0833); 
            this.adimlar.add(String.format("(%.2f km, 0.00 TL)   🚶   %s", mesafe, aciklama));
        }

        void ekleTaksi(double mesafe, double maliyet, String aciklama) {
            this.toplamMesafe += mesafe;
            this.toplamMaliyet += maliyet;
            this.toplamSure += (int) (mesafe / 0.5); 
            this.adimlar.add(String.format("(%.2f km, %.2f TL)   🚖   %s", mesafe, maliyet, aciklama));
        }

        void hesaplaSkor() {
            this.skor = (toplamMaliyet * 0.4) + (toplamSure * 0.4) + (aktarmaSayisi * 2.0);
        }

        double getSkor() {
            return skor;
        }
    }

    static class Cuzdan {
        double nakit;
        double krediKartiLimiti;
        double kentkartBakiyesi;

        Cuzdan(double nakit, double krediKartiLimiti, double kentkartBakiyesi) {
            this.nakit = nakit;
            this.krediKartiLimiti = krediKartiLimiti;
            this.kentkartBakiyesi = kentkartBakiyesi;
        }
    }

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = Ana.class.getResourceAsStream("/transport.json");
            if (is == null) {
                throw new GecersizKonumException("Hata: transport.json dosyası bulunamadı.");
            }
            SehirVerisi sehirVerisi = mapper.readValue(is, SehirVerisi.class);

            Scanner tarayici = new Scanner(System.in);
            Yolcu yolcu = yolcuSec(tarayici);
            RotaPlanlayici planlayici = new RotaPlanlayici(sehirVerisi, yolcu);

            System.out.println("Merhaba " + yolcu.getAd() + "! Rota planlamaya başlayalım.\n");

            System.out.println("Seyahat başlangıç zamanını girin (örneğin, 14:30):");
            String baslangicZamani;
            while (true) {
                baslangicZamani = tarayici.nextLine();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime.parse(baslangicZamani, formatter);
                    break;
                } catch (Exception e) {
                    System.out.println("Hata: Lütfen geçerli bir zaman formatı girin (örneğin, 14:30).");
                }
            }

            System.out.println("\nBaşlangıç Enlem ve Boylam:\n");
            System.out.print("Enlem (40.82103 - 40.76200): ");
            double baslangicEnlem = getDoubleInput(tarayici, "");
            System.out.print("\nBoylam (29.92512 - 29.96550): ");
            double baslangicBoylam = getDoubleInput(tarayici, "");
            System.out.println("\nHedef Enlem ve Boylam\n");
            System.out.print("Enlem (40.82103 - 40.76200): ");
            double hedefEnlem = getDoubleInput(tarayici, "");
            System.out.print("\nBoylam (29.92512 - 29.96550): ");
            double hedefBoylam = getDoubleInput(tarayici, "");

            System.out.println("\n\nCüzdan Bilgilerinizi Girin:\n");
            System.out.print("Nakit Miktarı (TL): ");
            double nakit = getDoubleInput(tarayici, "");
            System.out.print("Kredi Kartı Limiti (TL): ");
            double krediKartiLimiti = getDoubleInput(tarayici, "");
            System.out.print("Kentkart Bakiyesi (TL): ");
            double kentkartBakiyesi = getDoubleInput(tarayici, "");
            Cuzdan cuzdan = new Cuzdan(nakit, krediKartiLimiti, kentkartBakiyesi);

            RotaPlanlayici.DurakMesafeSonuc baslangicSonuc = planlayici.enYakinDuragiBul(baslangicEnlem, baslangicBoylam);
            RotaPlanlayici.DurakMesafeSonuc hedefSonuc = planlayici.enYakinDuragiBul(hedefEnlem, hedefBoylam);

            Durak baslangicDurak = baslangicSonuc.getDurak();
            Durak hedefDurak = hedefSonuc.getDurak();

            double baslangicMesafe = baslangicSonuc.getMesafe();
            double hedefMesafe = hedefSonuc.getMesafe();

            if (baslangicSonuc.getAtlananDurakMesaji() != null) {
                System.out.println("\n" + baslangicSonuc.getAtlananDurakMesaji());
            }
            if (hedefSonuc.getAtlananDurakMesaji() != null) {
                System.out.println("\n" + hedefSonuc.getAtlananDurakMesaji());
            }

            System.out.println("\nBaşlangıç Noktası: (" + baslangicEnlem + ", " + baslangicBoylam + ")");
            System.out.println("En Yakın Başlangıç Durağı: " + baslangicDurak.getName() + " (" + String.format("%.2f", baslangicMesafe) + " km)");
            System.out.println("Hedef Noktası: (" + hedefEnlem + ", " + hedefBoylam + ")");
            System.out.println("En Yakın Hedef Durağı: " + hedefDurak.getName() + " (" + String.format("%.2f", hedefMesafe) + " km)\n");

            List<RotaSonuc> rotaSecenekleri = new ArrayList<>();

            RotaSonuc sadeceOtobus = hesaplaSadeceOtobus(planlayici, sehirVerisi, yolcu, baslangicDurak, hedefDurak, baslangicMesafe, hedefMesafe);
            if (sadeceOtobus != null) {
                sadeceOtobus.setVarisZamani(baslangicZamani, sadeceOtobus.toplamSure);
                rotaSecenekleri.add(sadeceOtobus);
            }

            RotaSonuc sadeceTramvay = hesaplaSadeceTramvay(planlayici, sehirVerisi, yolcu, baslangicDurak, hedefDurak, baslangicMesafe, hedefMesafe);
            if (sadeceTramvay != null) {
                sadeceTramvay.setVarisZamani(baslangicZamani, sadeceTramvay.toplamSure);
                rotaSecenekleri.add(sadeceTramvay);
            }

            RotaSonuc otobusTramvayAktarma = hesaplaOtobusTramvayAktarma(planlayici, sehirVerisi, yolcu, baslangicDurak, hedefDurak, baslangicMesafe, hedefMesafe);
            if (otobusTramvayAktarma != null) {
                otobusTramvayAktarma.setVarisZamani(baslangicZamani, otobusTramvayAktarma.toplamSure);
                rotaSecenekleri.add(otobusTramvayAktarma);
            }

            RotaSonuc taksiKombinasyon = hesaplaTaksiKombinasyon(planlayici, sehirVerisi, yolcu, baslangicDurak, hedefDurak, baslangicMesafe, hedefMesafe);
            if (taksiKombinasyon != null) {
                taksiKombinasyon.setVarisZamani(baslangicZamani, taksiKombinasyon.toplamSure);
                rotaSecenekleri.add(taksiKombinasyon);
            }

            RotaSonuc sadeceTaksi = hesaplaSadeceTaksi(planlayici, baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
            sadeceTaksi.setVarisZamani(baslangicZamani, sadeceTaksi.toplamSure);
            rotaSecenekleri.add(sadeceTaksi);

            RotaSonuc enAzAktarmali = hesaplaEnAzAktarmali(planlayici, sehirVerisi, yolcu, baslangicDurak, hedefDurak, baslangicMesafe, hedefMesafe);
            if (enAzAktarmali != null) {
                enAzAktarmali.setVarisZamani(baslangicZamani, enAzAktarmali.toplamSure);
                rotaSecenekleri.add(enAzAktarmali);
            }

            for (RotaSonuc rota : rotaSecenekleri) {
                rota.hesaplaSkor();
            }
            RotaSonuc enIyiRota = rotaSecenekleri.stream().min(Comparator.comparing(RotaSonuc::getSkor)).orElse(null);
            if (enIyiRota != null) {
                System.out.println("\n📌 En Uygun Rota: " + enIyiRota.rotaTipi + " (Skor: " + String.format("%.2f", enIyiRota.getSkor()) + ")\n");
            }

            System.out.println("\nTüm Rota Seçenekleri:\n");
            for (int i = 0; i < rotaSecenekleri.size(); i++) {
                RotaSonuc rota = rotaSecenekleri.get(i);
                System.out.printf("%d. %s 📍 (Toplam: %.2f TL 💰, %d dk ⏳, %.2f km 📏, %d aktarma 🔄, Varış: %s)\n",
                        (i + 1), rota.rotaTipi, rota.toplamMaliyet, rota.toplamSure, rota.toplamMesafe, rota.aktarmaSayisi, rota.getVarisZamani());
                for (String adim : rota.adimlar) {
                    System.out.println("   " + adim);
                }
                System.out.println();
            }

            System.out.println("\nLütfen bir rota seçin (1-" + rotaSecenekleri.size() + "):");
            int secim;
            while (true) {
                try {
                    secim = Integer.parseInt(tarayici.nextLine());
                    if (secim >= 1 && secim <= rotaSecenekleri.size()) break;
                    System.out.println("Hata: 1 ile " + rotaSecenekleri.size() + " arasında bir sayı girin.");
                } catch (NumberFormatException e) {
                    System.out.println("Hata: Lütfen bir sayı girin.");
                }
            }

            RotaSonuc secilenRota = rotaSecenekleri.get(secim - 1);

            System.out.println("\n\nÖdeme Yöntemi Seçin:");
            System.out.println("1) Nakit");
            System.out.println("2) Kredi Kartı");
            System.out.println("3) Kentkart");
            String odemeYontemi = odemeYontemiSec(tarayici);

            boolean odemeGecerli = false;
            switch (odemeYontemi) {
                case "Nakit":
                    if (cuzdan.nakit >= secilenRota.toplamMaliyet) {
                        odemeGecerli = true;
                        cuzdan.nakit -= secilenRota.toplamMaliyet;
                    }
                    break;
                case "Kredi Kartı":
                    if (cuzdan.krediKartiLimiti >= secilenRota.toplamMaliyet) {
                        odemeGecerli = true;
                        cuzdan.krediKartiLimiti -= secilenRota.toplamMaliyet;
                    }
                    break;
                case "Kentkart":
                    if (cuzdan.kentkartBakiyesi >= secilenRota.toplamMaliyet) {
                        odemeGecerli = true;
                        cuzdan.kentkartBakiyesi -= secilenRota.toplamMaliyet;
                    }
                    break;
            }

            if (!odemeGecerli) {
                System.out.println("\nHata: Seçilen ödeme yöntemi için yeterli bakiye yok! Lütfen başka bir ödeme yöntemi seçin.");
                System.out.println("Ödeme Yöntemi Seçin:");
                System.out.println("1) Nakit");
                System.out.println("2) Kredi Kartı");
                System.out.println("3) Kentkart");
                odemeYontemi = odemeYontemiSec(tarayici);
                switch (odemeYontemi) {
                    case "Nakit":
                        odemeGecerli = cuzdan.nakit >= secilenRota.toplamMaliyet;
                        if (odemeGecerli) cuzdan.nakit -= secilenRota.toplamMaliyet;
                        break;
                    case "Kredi Kartı":
                        odemeGecerli = cuzdan.krediKartiLimiti >= secilenRota.toplamMaliyet;
                        if (odemeGecerli) cuzdan.krediKartiLimiti -= secilenRota.toplamMaliyet;
                        break;
                    case "Kentkart":
                        odemeGecerli = cuzdan.kentkartBakiyesi >= secilenRota.toplamMaliyet;
                        if (odemeGecerli) cuzdan.kentkartBakiyesi -= secilenRota.toplamMaliyet;
                        break;
                }
                if (!odemeGecerli) {
                    System.out.println("Hata: Yeterli bakiye yok. Seyahat planlanamadı.");
                    return;
                }
            }

            System.out.println("\n\nSEÇİLEN ROTA (" + secilenRota.rotaTipi + "):\n");
            for (String adim : secilenRota.adimlar) {
                System.out.println(adim);
            }

            System.out.println("\n\nBaşlangıç Zamanı: " + baslangicZamani);
            System.out.println("Varış Zamanı: " + secilenRota.getVarisZamani());
            System.out.println("Ödeme Yöntemi: " + odemeYontemi);
            System.out.println("Toplam Maliyet: " + String.format("%.2f", secilenRota.toplamMaliyet) + " TL");
            System.out.println("Toplam Süre: " + secilenRota.toplamSure + " dk");
            System.out.println("Aktarma Sayısı: " + secilenRota.aktarmaSayisi);
            System.out.println("\n\nKalan Bakiye:");
            System.out.printf("Nakit: %.2f TL\n", cuzdan.nakit);
            System.out.printf("Kredi Kartı Limiti: %.2f TL\n", cuzdan.krediKartiLimiti);
            System.out.printf("Kentkart Bakiyesi: %.2f TL\n", cuzdan.kentkartBakiyesi);

        } catch (GecersizKonumException e) {
            System.out.println("Hata: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("Beklenmedik bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static RotaSonuc hesaplaSadeceOtobus(RotaPlanlayici planlayici, SehirVerisi sehirVerisi, Yolcu yolcu,
                                                Durak baslangicDurak, Durak hedefDurak, double baslangicMesafe, double hedefMesafe) {
        if (!baslangicDurak.getId().startsWith("bus") || !hedefDurak.getId().startsWith("bus")) {
            System.out.println("Uyarı: Sadece Otobüs rotası mevcut değil. Başlangıç ve hedef duraklar otobüs durakları olmalı.");
            return null;
        }

        RotaSonuc sonuc = new RotaSonuc("Sadece Otobüs");
        double toplamMaliyet = 0.0;
        int toplamSure = 0;
        double toplamMesafe = 0.0;

        if (baslangicMesafe <= 3) {
            sonuc.ekleYurume(baslangicMesafe, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Yürüme");
        } else {
            double taksiMaliyet = planlayici.taksiMaliyetHesapla(baslangicMesafe);
            toplamMaliyet += taksiMaliyet;
            toplamSure += (int) (baslangicMesafe / 0.5);
            toplamMesafe += baslangicMesafe;
            sonuc.ekleTaksi(baslangicMesafe, taksiMaliyet, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Taksi");
        }

        try {
            Rota rota = planlayici.rotaHesapla(baslangicDurak.getId(), hedefDurak.getId());
            for (int i = 0; i < rota.getDuraklar().size() - 1; i++) {
                String suankiDurakKimligi = rota.getDuraklar().get(i);
                String sonrakiDurakKimligi = rota.getDuraklar().get(i + 1);

                if (!suankiDurakKimligi.startsWith("bus") || !sonrakiDurakKimligi.startsWith("bus")) {
                    System.out.println("Uyarı: Sadece Otobüs rotası mevcut değil. Rota otobüs durakları dışına çıkıyor.");
                    return null;
                }

                Durak suankiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(suankiDurakKimligi))
                        .findFirst()
                        .orElse(null);
                Durak sonrakiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(sonrakiDurakKimligi))
                        .findFirst()
                        .orElse(null);

                if (suankiDurak == null || sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                toplamMesafe += duraklarArasiMesafe;

                double adimMaliyeti = 0.0;
                if (suankiDurak.getNextStops() != null) {
                    for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                        if (sonraki.getStopId().equals(sonrakiDurakKimligi)) {
                            adimMaliyeti = yolcu.ucretHesapla(sonraki.getUcret());
                            break;
                        }
                    }
                }

                toplamMaliyet += adimMaliyeti;
                sonuc.adimlar.add(String.format("(%.2f km, %.2f TL)   🚌   %s (%s) -> %s (%s)   --   (otobüs)",
                        duraklarArasiMesafe, adimMaliyeti, suankiDurak.getName(), suankiDurak.getType(),
                        sonrakiDurak.getName(), sonrakiDurak.getType()));
            }

            if (hedefMesafe <= 3) {
                sonuc.ekleYurume(hedefMesafe, hedefDurak.getName() + " Durağından Hedef Noktasına Yürüme");
            } else {
                double taksiMaliyet = planlayici.taksiMaliyetHesapla(hedefMesafe);
                toplamMaliyet += taksiMaliyet;
                toplamSure += (int) (hedefMesafe / 0.5);
                toplamMesafe += hedefMesafe;
                sonuc.ekleTaksi(hedefMesafe, taksiMaliyet, hedefDurak.getName() + " Durağından Hedef Noktasına Taksi");
            }

            toplamSure += rota.getToplamSure();
            sonuc.toplamMaliyet = toplamMaliyet;
            sonuc.toplamSure = toplamSure;
            sonuc.toplamMesafe = toplamMesafe;
            sonuc.aktarmaSayisi = rota.getAktarmaSayisi();
            return sonuc;

        } catch (Exception e) {
            System.out.println("Hata: Sadece Otobüs rotası hesaplanamadı: " + e.getMessage());
            return null;
        }
    }

    private static RotaSonuc hesaplaSadeceTramvay(RotaPlanlayici planlayici, SehirVerisi sehirVerisi, Yolcu yolcu,
                                                 Durak baslangicDurak, Durak hedefDurak, double baslangicMesafe, double hedefMesafe) {
        if (!baslangicDurak.getId().startsWith("tram") || !hedefDurak.getId().startsWith("tram")) {
            System.out.println("Uyarı: Sadece Tramvay rotası mevcut değil. Başlangıç ve hedef duraklar tramvay durakları olmalı.");
            return null;
        }

        RotaSonuc sonuc = new RotaSonuc("Sadece Tramvay");
        double toplamMaliyet = 0.0;
        int toplamSure = 0;
        double toplamMesafe = 0.0;

        if (baslangicMesafe <= 3) {
            sonuc.ekleYurume(baslangicMesafe, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Yürüme");
        } else {
            double taksiMaliyet = planlayici.taksiMaliyetHesapla(baslangicMesafe);
            toplamMaliyet += taksiMaliyet;
            toplamSure += (int) (baslangicMesafe / 0.5);
            toplamMesafe += baslangicMesafe;
            sonuc.ekleTaksi(baslangicMesafe, taksiMaliyet, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Taksi");
        }

        try {
            Rota rota = planlayici.rotaHesapla(baslangicDurak.getId(), hedefDurak.getId());
            for (int i = 0; i < rota.getDuraklar().size() - 1; i++) {
                String suankiDurakKimligi = rota.getDuraklar().get(i);
                String sonrakiDurakKimligi = rota.getDuraklar().get(i + 1);

                if (!suankiDurakKimligi.startsWith("tram") || !sonrakiDurakKimligi.startsWith("tram")) {
                    System.out.println("Uyarı: Sadece Tramvay rotası mevcut değil. Rota tramvay durakları dışına çıkıyor.");
                    return null;
                }

                Durak suankiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(suankiDurakKimligi))
                        .findFirst()
                        .orElse(null);
                Durak sonrakiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(sonrakiDurakKimligi))
                        .findFirst()
                        .orElse(null);

                if (suankiDurak == null || sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                toplamMesafe += duraklarArasiMesafe;

                double adimMaliyeti = 0.0;
                if (suankiDurak.getNextStops() != null) {
                    for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                        if (sonraki.getStopId().equals(sonrakiDurakKimligi)) {
                            adimMaliyeti = yolcu.ucretHesapla(sonraki.getUcret());
                            break;
                        }
                    }
                }

                toplamMaliyet += adimMaliyeti;
                sonuc.adimlar.add(String.format("(%.2f km, %.2f TL)   🚋   %s (%s) -> %s (%s)   --   (tramvay)",
                        duraklarArasiMesafe, adimMaliyeti, suankiDurak.getName(), suankiDurak.getType(),
                        sonrakiDurak.getName(), sonrakiDurak.getType()));
            }

            if (hedefMesafe <= 3) {
                sonuc.ekleYurume(hedefMesafe, hedefDurak.getName() + " Durağından Hedef Noktasına Yürüme");
            } else {
                double taksiMaliyet = planlayici.taksiMaliyetHesapla(hedefMesafe);
                toplamMaliyet += taksiMaliyet;
                toplamSure += (int) (hedefMesafe / 0.5);
                toplamMesafe += hedefMesafe;
                sonuc.ekleTaksi(hedefMesafe, taksiMaliyet, hedefDurak.getName() + " Durağından Hedef Noktasına Taksi");
            }

            toplamSure += rota.getToplamSure();
            sonuc.toplamMaliyet = toplamMaliyet;
            sonuc.toplamSure = toplamSure;
            sonuc.toplamMesafe = toplamMesafe;
            sonuc.aktarmaSayisi = rota.getAktarmaSayisi();
            return sonuc;

        } catch (Exception e) {
            System.out.println("Hata: Sadece Tramvay rotası hesaplanamadı: " + e.getMessage());
            return null;
        }
    }

    private static RotaSonuc hesaplaOtobusTramvayAktarma(RotaPlanlayici planlayici, SehirVerisi sehirVerisi, Yolcu yolcu,
                                                         Durak baslangicDurak, Durak hedefDurak, double baslangicMesafe, double hedefMesafe) {
        if (!((baslangicDurak.getId().startsWith("bus") && hedefDurak.getId().startsWith("tram")) ||
                (baslangicDurak.getId().startsWith("tram") && hedefDurak.getId().startsWith("bus")))) {
            System.out.println("Uyarı: Otobüs + Tramvay Aktarması rotası mevcut değil. Başlangıç ve hedef duraklar uygun değil.");
            return null;
        }

        RotaSonuc sonuc = new RotaSonuc("Otobüs + Tramvay Aktarması");
        double toplamMaliyet = 0.0;
        int toplamSure = 0;
        double toplamMesafe = 0.0;

        if (baslangicMesafe <= 3) {
            sonuc.ekleYurume(baslangicMesafe, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Yürüme");
        } else {
            double taksiMaliyet = planlayici.taksiMaliyetHesapla(baslangicMesafe);
            toplamMaliyet += taksiMaliyet;
            toplamSure += (int) (baslangicMesafe / 0.5);
            toplamMesafe += baslangicMesafe;
            sonuc.ekleTaksi(baslangicMesafe, taksiMaliyet, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Taksi");
        }

        try {
            Rota rota = planlayici.rotaHesapla(baslangicDurak.getId(), hedefDurak.getId());
            for (int i = 0; i < rota.getDuraklar().size() - 1; i++) {
                String suankiDurakKimligi = rota.getDuraklar().get(i);
                String sonrakiDurakKimligi = rota.getDuraklar().get(i + 1);

                Durak suankiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(suankiDurakKimligi))
                        .findFirst()
                        .orElse(null);
                Durak sonrakiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(sonrakiDurakKimligi))
                        .findFirst()
                        .orElse(null);

                if (suankiDurak == null || sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                toplamMesafe += duraklarArasiMesafe;

                double adimMaliyeti = 0.0;
                if (suankiDurak.getNextStops() != null) {
                    for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                        if (sonraki.getStopId().equals(sonrakiDurakKimligi)) {
                            adimMaliyeti = yolcu.ucretHesapla(sonraki.getUcret());
                            break;
                        }
                    }
                }
                if (suankiDurak.getTransfer() != null && suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurakKimligi)) {
                    double transferUcret = suankiDurak.getTransfer().getTransferUcret();
                    adimMaliyeti = yolcu.ucretHesapla(transferUcret * 0.5);
                }

                toplamMaliyet += adimMaliyeti;

                String aracTipi = suankiDurakKimligi.startsWith("bus") ? "otobüs" : "tramvay";
                if (sonrakiDurakKimligi.startsWith("bus") && !aracTipi.equals("otobüs")) {
                    aracTipi = "tramvay -> otobüs";
                } else if (sonrakiDurakKimligi.startsWith("tram") && !aracTipi.equals("tramvay")) {
                    aracTipi = "otobüs -> tramvay";
                }

                sonuc.adimlar.add(String.format("(%.2f km, %.2f TL)   %s   %s (%s) -> %s (%s)   --   (%s)",
                        duraklarArasiMesafe, adimMaliyeti, suankiDurakKimligi.startsWith("bus") ? "🚌" : "🚋",
                        suankiDurak.getName(), suankiDurak.getType(), sonrakiDurak.getName(), sonrakiDurak.getType(), aracTipi));
            }

            if (hedefMesafe <= 3) {
                sonuc.ekleYurume(hedefMesafe, hedefDurak.getName() + " Durağından Hedef Noktasına Yürüme");
            } else {
                double taksiMaliyet = planlayici.taksiMaliyetHesapla(hedefMesafe);
                toplamMaliyet += taksiMaliyet;
                toplamSure += (int) (hedefMesafe / 0.5);
                toplamMesafe += hedefMesafe;
                sonuc.ekleTaksi(hedefMesafe, taksiMaliyet, hedefDurak.getName() + " Durağından Hedef Noktasına Taksi");
            }

            toplamSure += rota.getToplamSure();
            sonuc.toplamMaliyet = toplamMaliyet;
            sonuc.toplamSure = toplamSure;
            sonuc.toplamMesafe = toplamMesafe;
            sonuc.aktarmaSayisi = rota.getAktarmaSayisi();
            return sonuc;

        } catch (Exception e) {
            System.out.println("Hata: Otobüs + Tramvay Aktarması rotası hesaplanamadı: " + e.getMessage());
            return null;
        }
    }

    private static RotaSonuc hesaplaTaksiKombinasyon(RotaPlanlayici planlayici, SehirVerisi sehirVerisi, Yolcu yolcu,
                                                    Durak baslangicDurak, Durak hedefDurak, double baslangicMesafe, double hedefMesafe) {
        RotaSonuc sonuc = new RotaSonuc("Taksi + Otobüs/Tramvay Kombinasyonu");
        double toplamMaliyet = 0.0;
        int toplamSure = 0;
        double toplamMesafe = 0.0;

        if (baslangicMesafe <= 3) {
            sonuc.ekleYurume(baslangicMesafe, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Yürüme");
        } else {
            double taksiMaliyet = planlayici.taksiMaliyetHesapla(baslangicMesafe);
            toplamMaliyet += taksiMaliyet;
            toplamSure += (int) (baslangicMesafe / 0.5);
            toplamMesafe += baslangicMesafe;
            sonuc.ekleTaksi(baslangicMesafe, taksiMaliyet, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Taksi");
        }

        try {
            Rota rota = planlayici.rotaHesapla(baslangicDurak.getId(), hedefDurak.getId());
            for (int i = 0; i < rota.getDuraklar().size() - 1; i++) {
                String suankiDurakKimligi = rota.getDuraklar().get(i);
                String sonrakiDurakKimligi = rota.getDuraklar().get(i + 1);

                Durak suankiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(suankiDurakKimligi))
                        .findFirst()
                        .orElse(null);
                Durak sonrakiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(sonrakiDurakKimligi))
                        .findFirst()
                        .orElse(null);

                if (suankiDurak == null || sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                toplamMesafe += duraklarArasiMesafe;

                double adimMaliyeti = 0.0;
                if (suankiDurak.getNextStops() != null) {
                    for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                        if (sonraki.getStopId().equals(sonrakiDurakKimligi)) {
                            adimMaliyeti = yolcu.ucretHesapla(sonraki.getUcret());
                            break;
                        }
                    }
                }
                if (suankiDurak.getTransfer() != null && suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurakKimligi)) {
                    double transferUcret = suankiDurak.getTransfer().getTransferUcret();
                    adimMaliyeti = yolcu.ucretHesapla(transferUcret * 0.5);
                }

                toplamMaliyet += adimMaliyeti;

                String aracTipi = suankiDurakKimligi.startsWith("bus") ? "otobüs" : "tramvay";
                if (sonrakiDurakKimligi.startsWith("bus") && !aracTipi.equals("otobüs")) {
                    aracTipi = "tramvay -> otobüs";
                } else if (sonrakiDurakKimligi.startsWith("tram") && !aracTipi.equals("tramvay")) {
                    aracTipi = "otobüs -> tramvay";
                }

                sonuc.adimlar.add(String.format("(%.2f km, %.2f TL)   %s   %s (%s) -> %s (%s)   --   (%s)",
                        duraklarArasiMesafe, adimMaliyeti, suankiDurakKimligi.startsWith("bus") ? "🚌" : "🚋",
                        suankiDurak.getName(), suankiDurak.getType(), sonrakiDurak.getName(), sonrakiDurak.getType(), aracTipi));
            }

            if (hedefMesafe <= 3) {
                sonuc.ekleYurume(hedefMesafe, hedefDurak.getName() + " Durağından Hedef Noktasına Yürüme");
            } else {
                double taksiMaliyet = planlayici.taksiMaliyetHesapla(hedefMesafe);
                toplamMaliyet += taksiMaliyet;
                toplamSure += (int) (hedefMesafe / 0.5);
                toplamMesafe += hedefMesafe;
                sonuc.ekleTaksi(hedefMesafe, taksiMaliyet, hedefDurak.getName() + " Durağından Hedef Noktasına Taksi");
            }

            toplamSure += rota.getToplamSure();
            sonuc.toplamMaliyet = toplamMaliyet;
            sonuc.toplamSure = toplamSure;
            sonuc.toplamMesafe = toplamMesafe;
            sonuc.aktarmaSayisi = rota.getAktarmaSayisi();
            return sonuc;

        } catch (Exception e) {
            System.out.println("Hata: Taksi + Otobüs/Tramvay Kombinasyonu rotası hesaplanamadı: " + e.getMessage());
            return null;
        }
    }

    private static RotaSonuc hesaplaSadeceTaksi(RotaPlanlayici planlayici, double baslangicEnlem, double baslangicBoylam,
                                               double hedefEnlem, double hedefBoylam) {
        RotaSonuc sonuc = new RotaSonuc("Sadece Taksi");
        double mesafe = MesafeHesaplayici.haversine(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
        double maliyet = planlayici.taksiMaliyetHesapla(mesafe);
        int sure = (int) (mesafe / 0.5);

        sonuc.toplamMesafe = mesafe;
        sonuc.toplamMaliyet = maliyet;
        sonuc.toplamSure = sure;
        sonuc.aktarmaSayisi = 0;
        sonuc.ekleTaksi(mesafe, maliyet, "Başlangıç Noktasından Hedef Noktasına Taksi");

        return sonuc;
    }

    private static RotaSonuc hesaplaEnAzAktarmali(RotaPlanlayici planlayici, SehirVerisi sehirVerisi, Yolcu yolcu,
                                                  Durak baslangicDurak, Durak hedefDurak, double baslangicMesafe, double hedefMesafe) {
        RotaSonuc sonuc = new RotaSonuc("En Az Aktarmalı");
        double toplamMaliyet = 0.0;
        int toplamSure = 0;
        double toplamMesafe = 0.0;

        if (baslangicMesafe <= 3) {
            sonuc.ekleYurume(baslangicMesafe, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Yürüme");
        } else {
            double taksiMaliyet = planlayici.taksiMaliyetHesapla(baslangicMesafe);
            toplamMaliyet += taksiMaliyet;
            toplamSure += (int) (baslangicMesafe / 0.5);
            toplamMesafe += baslangicMesafe;
            sonuc.ekleTaksi(baslangicMesafe, taksiMaliyet, "Başlangıç Noktasından " + baslangicDurak.getName() + " Durağına Taksi");
        }

        try {
            Rota rota = planlayici.enAzAktarmaliRotaHesapla(baslangicDurak.getId(), hedefDurak.getId());
            for (int i = 0; i < rota.getDuraklar().size() - 1; i++) {
                String suankiDurakKimligi = rota.getDuraklar().get(i);
                String sonrakiDurakKimligi = rota.getDuraklar().get(i + 1);

                Durak suankiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(suankiDurakKimligi))
                        .findFirst()
                        .orElse(null);
                Durak sonrakiDurak = sehirVerisi.getDuraklar().stream()
                        .filter(d -> d.getId().equals(sonrakiDurakKimligi))
                        .findFirst()
                        .orElse(null);

                if (suankiDurak == null || sonrakiDurak == null) continue;

                double duraklarArasiMesafe = MesafeHesaplayici.haversine(
                        suankiDurak.getLat(), suankiDurak.getLon(),
                        sonrakiDurak.getLat(), sonrakiDurak.getLon()
                );
                toplamMesafe += duraklarArasiMesafe;

                double adimMaliyeti = 0.0;
                if (suankiDurak.getNextStops() != null) {
                    for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                        if (sonraki.getStopId().equals(sonrakiDurakKimligi)) {
                            adimMaliyeti = yolcu.ucretHesapla(sonraki.getUcret());
                            break;
                        }
                    }
                }
                if (suankiDurak.getTransfer() != null && suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurakKimligi)) {
                    double transferUcret = suankiDurak.getTransfer().getTransferUcret();
                    adimMaliyeti = yolcu.ucretHesapla(transferUcret * 0.5);
                }

                toplamMaliyet += adimMaliyeti;

                String aracTipi = suankiDurakKimligi.startsWith("bus") ? "otobüs" : "tramvay";
                if (sonrakiDurakKimligi.startsWith("bus") && !aracTipi.equals("otobüs")) {
                    aracTipi = "tramvay -> otobüs";
                } else if (sonrakiDurakKimligi.startsWith("tram") && !aracTipi.equals("tramvay")) {
                    aracTipi = "otobüs -> tramvay";
                }

                sonuc.adimlar.add(String.format("(%.2f km, %.2f TL)   %s   %s (%s) -> %s (%s)   --   (%s)",
                        duraklarArasiMesafe, adimMaliyeti, suankiDurakKimligi.startsWith("bus") ? "🚌" : "🚋",
                        suankiDurak.getName(), suankiDurak.getType(), sonrakiDurak.getName(), sonrakiDurak.getType(), aracTipi));
            }

            if (hedefMesafe <= 3) {
                sonuc.ekleYurume(hedefMesafe, hedefDurak.getName() + " Durağından Hedef Noktasına Yürüme");
            } else {
                double taksiMaliyet = planlayici.taksiMaliyetHesapla(hedefMesafe);
                toplamMaliyet += taksiMaliyet;
                toplamSure += (int) (hedefMesafe / 0.5);
                toplamMesafe += hedefMesafe;
                sonuc.ekleTaksi(hedefMesafe, taksiMaliyet, hedefDurak.getName() + " Durağından Hedef Noktasına Taksi");
            }

            toplamSure += rota.getToplamSure();
            sonuc.toplamMaliyet = toplamMaliyet;
            sonuc.toplamSure = toplamSure;
            sonuc.toplamMesafe = toplamMesafe;
            sonuc.aktarmaSayisi = rota.getAktarmaSayisi();
            return sonuc;

        } catch (Exception e) {
            System.out.println("Hata: En Az Aktarmalı rota hesaplanamadı: " + e.getMessage());
            return null;
        }
    }

    private static Yolcu yolcuSec(Scanner scanner) {
        System.out.println("1) Öğrenci (%50 indirim) \n2) Yaşlı (%75 indirim) \n3) Normal (indirim yok)");
        System.out.println("Yolcu tipini seçin:");
        int secim;
        while (true) {
            try {
                secim = Integer.parseInt(scanner.nextLine());
                if (secim >= 1 && secim <= 3) break;
                System.out.println("Hata: 1, 2 veya 3 girin.");
            } catch (NumberFormatException e) {
                System.out.println("Hata: Lütfen bir sayı girin.");
            }
        }
        System.out.println("\nAdınızı girin:");
        String ad = scanner.nextLine();

        switch (secim) {
            case 1:
                return new Ogrenci(ad);
            case 2:
                return new Yasli(ad);
            default:
                return new NormalYolcu(ad);
        }
    }

    private static String odemeYontemiSec(Scanner scanner) {
        int secim;
        while (true) {
            try {
                secim = Integer.parseInt(scanner.nextLine());
                if (secim >= 1 && secim <= 3) break;
                System.out.println("Hata: 1, 2 veya 3 girin.");
            } catch (NumberFormatException e) {
                System.out.println("Hata: Lütfen bir sayı girin.");
            }
        }
        switch (secim) {
            case 1:
                return "Nakit";
            case 2:
                return "Kredi Kartı";
            default:
                return "Kentkart";
        }
    }

    private static double getDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().replace(",", ".");
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Hata: Lütfen geçerli bir sayı girin (örneğin, 40.78259 veya 40,78259).");
            }
        }
    }
}