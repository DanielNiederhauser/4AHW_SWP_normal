import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.time.*;
import java.util.List;

class Kalender {
    public static void main(String[] args) {
        List<LocalDate> Feiertage = new ArrayList<>();
        int jahre = 1;
        int montag = 0, dienstag = 0, mittwoch = 0, donnerstag = 0, freitag = 0, samstag = 0, sonntag = 0;
        feiertageGenerieren(Feiertage, jahre - 1);
        System.out.println("Size: " + Feiertage.size());
        for (int i = 0; i < Feiertage.size(); i++) {
            System.out.println(Feiertage.get(i).getDayOfWeek());
            if (Feiertage.get(i).getDayOfWeek().equals("MONDAY")) {
                montag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("TUESDAY")) {
                dienstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("WEDNESDAY")) {
                mittwoch++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("THURSDAY")) {
                donnerstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("FRIDAY")) {
                freitag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("SATURDAY")) {
                samstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals("SUNDAY")) {
                sonntag++;
            } else {
                System.out.println("Falsch");
            }
        }
        feiertagAnzahlAusgeben(montag, dienstag, mittwoch, donnerstag, freitag, samstag, sonntag);

    }

    public static void feiertageGenerieren(List<LocalDate> feiertage, int jahre) {
        for (int i = 2020; i <= 2020 + jahre; i++) {
            feiertage.add(LocalDate.of(i, 1, 1));
            feiertage.add(LocalDate.of(i, 1, 6));
            feiertage.add(LocalDate.of(i, 5, 1));
            feiertage.add(LocalDate.of(i, 8, 15));
            feiertage.add(LocalDate.of(i, 10, 26));
            feiertage.add(LocalDate.of(i, 12, 25));
            feiertage.add(LocalDate.of(i, 12, 26));
            feiertage.add(LocalDate.of(i, 12, 1));
        }
    }

    public static void feiertagAnzahlAusgeben(int mo, int di, int mi, int don, int fr, int sa, int so) {
        System.out.println("Montage: " + mo);
        System.out.println("Dienstage: " + di);
        System.out.println("Mittwoche: " + mi);
        System.out.println("Donnerstage: " + don);
        System.out.println("Freitage: " + fr);
        System.out.println("Samstage: " + sa);
        System.out.println("Sonntage: " + so);
    }
}





