import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.*;

class Kalender {
    public static void main(String[] args)throws JSONException, MalformedURLException, IOException {
        Scanner reader = new Scanner(System.in);
        List<LocalDate> Feiertage = new ArrayList<>();
        int montag = 0, dienstag = 0, mittwoch = 0, donnerstag = 0, freitag = 0, samstag = 0, sonntag = 0;
        int startjahr, endjahr;

        List<String> dynamischeFeiertage = new ArrayList<>();
        dynamischeFeiertage.add("Christi Himmelfahrt");
        dynamischeFeiertage.add("Ostermontag");
        dynamischeFeiertage.add("Fronleichnam");
        dynamischeFeiertage.add("Pfingstmontag");
        List<String> alleDynamischen = new ArrayList<>();

        System.out.print("Startjahr: ");
        startjahr = reader.nextInt();

        System.out.print("Endjahr (inklusive): ");
        endjahr = reader.nextInt();
        for (int i = startjahr; i <= endjahr; i++) {
            JSONObject json = new JSONObject(IOUtils.toString(new URL("https://feiertage-api.de/api/?jahr=" + i + "&nur_land=BY"), Charset.forName("UTF-8")));
            List<String> tage = getWert(json,dynamischeFeiertage);
            for (int j =0;j<tage.size();j++){
                alleDynamischen.add(tage.get(j));
            }

        }
        feiertageGenerieren(Feiertage, startjahr, endjahr);

        for (int i=0;i<alleDynamischen.size();i++){
            LocalDate localDate = LocalDate.parse(alleDynamischen.get(i));
            Feiertage.add(localDate);
        }

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

    private static List<String> getWert(JSONObject json, List<String> keys) {

        List<String> anzahl = new ArrayList<>();
        for(int i = 0; i< keys.size();i++) {
            JSONObject bestaetigt = (JSONObject) json.get(keys.get(i));
            anzahl.add( bestaetigt.getString("datum"));

        }
        return anzahl;
    }

}
