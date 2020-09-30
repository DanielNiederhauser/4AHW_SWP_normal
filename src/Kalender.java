import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.*;

class Kalender {

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        List<LocalDate> Feiertage = new ArrayList<>();
        int jahre = 1;
        int montag = 0, dienstag = 0, mittwoch = 0, donnerstag = 0, freitag = 0, samstag = 0, sonntag = 0;
        int startjahr, endjahr;


        System.out.print("Startjahr: ");
        startjahr = reader.nextInt();


        System.out.print("Endjahr (inklusive): ");
        endjahr = reader.nextInt();
        feiertageGenerieren(Feiertage, startjahr, endjahr);

        for (int i = 0; i < Feiertage.size(); i++) {

            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                montag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
                dienstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.WEDNESDAY)) {
                mittwoch++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
                donnerstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                freitag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                samstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                sonntag++;
            }
        }
        feiertagAnzahlAusgeben(montag, dienstag, mittwoch, donnerstag, freitag, samstag, sonntag);

    }

    public static void feiertageGenerieren(List<LocalDate> feiertage, int startjahr, int endjahr) {
        for (int i = startjahr; i <= startjahr + endjahr - startjahr; i++) {
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