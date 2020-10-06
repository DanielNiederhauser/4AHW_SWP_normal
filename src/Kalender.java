import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
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

public class Kalender extends Application {
    final static String MontagDiagramm = "Montag";
    final static String DienstagDiagramm = "Dienstag";
    final static String MittwochDiagramm = "Mittwoch";
    final static String DonnerstagDiagramm = "Donnerstag";
    final static String FreitagDiagramm = "Freitag";
    final static String SamstagDiagramm = "Samstag";
    final static String SonntagDiagramm = "Sonntag";

    @Override public void start(Stage stage) {
        stage.setTitle("Feiertagsvergleich");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Feiertagsverlgeich");
        xAxis.setLabel("Tag");
        yAxis.setLabel("Anzahl");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Feiertage von den Jahren " + startjahr + " bis " + endjahr);
        series1.getData().add(new XYChart.Data(MontagDiagramm, montag));
        series1.getData().add(new XYChart.Data(DienstagDiagramm, dienstag));
        series1.getData().add(new XYChart.Data(MittwochDiagramm, mittwoch));
        series1.getData().add(new XYChart.Data(DonnerstagDiagramm, donnerstag));
        series1.getData().add(new XYChart.Data(FreitagDiagramm, freitag));
        series1.getData().add(new XYChart.Data(SamstagDiagramm, samstag));
        series1.getData().add(new XYChart.Data(SonntagDiagramm, sonntag));



        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(series1);
        stage.setScene(scene);
        stage.show();
    }
    static int montag = 0, dienstag = 0, mittwoch = 0, donnerstag = 0, freitag = 0, samstag = 0, sonntag = 0;
    static int startjahr, endjahr;
    public static void main(String[] args)throws JSONException, MalformedURLException, IOException {
        Scanner reader = new Scanner(System.in);
        List<LocalDate> Feiertage = new ArrayList<>();


        List<LocalDate> montage = new ArrayList<>();
        List<LocalDate> dienstage = new ArrayList<>();
        List<LocalDate> mittwoche = new ArrayList<>();
        List<LocalDate> donnerstage = new ArrayList<>();
        List<LocalDate> freitage = new ArrayList<>();
        List<LocalDate> samstage = new ArrayList<>();
        List<LocalDate> sonntage = new ArrayList<>();


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
                montage.add(Feiertage.get(i));
                montag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
                dienstage.add(Feiertage.get(i));
                dienstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.WEDNESDAY)) {
                mittwoche.add(Feiertage.get(i));
                mittwoch++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
                donnerstage.add(Feiertage.get(i));
                donnerstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                freitage.add(Feiertage.get(i));
                freitag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                samstage.add(Feiertage.get(i));
                samstag++;
            }
            if (Feiertage.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                sonntage.add(Feiertage.get(i));
                sonntag++;
            }
        }

        //Listen sortieren für schönere Ausgabe
        listenSortieren(montage,dienstage,mittwoche,donnerstage,freitage,samstage,sonntage);

        feiertagAnzahlAusgeben(montag, dienstag, mittwoch, donnerstag, freitag, samstag, sonntag, montage, dienstage,
                mittwoche, donnerstage, freitage, samstage, sonntage);
        launch(args);
    }
    public static void feiertageGenerieren(List<LocalDate> feiertage, int startjahr, int endjahr) {
        for (int i = startjahr; i <= startjahr + endjahr - startjahr; i++) {
            feiertage.add(LocalDate.of(i, 1, 1));
            feiertage.add(LocalDate.of(i, 1, 6));
            feiertage.add(LocalDate.of(i, 5, 1));
            feiertage.add(LocalDate.of(i, 8, 15));
            feiertage.add(LocalDate.of(i, 10, 26));
            feiertage.add(LocalDate.of(i, 11, 1));
            feiertage.add(LocalDate.of(i, 12, 8));
            feiertage.add(LocalDate.of(i, 12, 25));
            feiertage.add(LocalDate.of(i, 12, 26));


        }
    }

    public static void feiertagAnzahlAusgeben(int mo, int di, int mi, int don, int fr, int sa, int so, List<LocalDate> Montage,
                                              List<LocalDate> Dienstage, List<LocalDate> Mittwoche, List<LocalDate> Donnerstage,
                                              List<LocalDate> Freitage, List<LocalDate> Samstage, List<LocalDate> Sonntage) {
        System.out.println("Montage: " + mo + " " + Montage);
        System.out.println("Dienstage: " + di + " " + Dienstage);
        System.out.println("Mittwoche: " + mi + " " + Mittwoche);
        System.out.println("Donnerstage: " + don + " " + Donnerstage);
        System.out.println("Freitage: " + fr + " " + Freitage);
        System.out.println("Samstage: " + sa + " " + Samstage);
        System.out.println("Sonntage: " + so + " " + Sonntage);
    }

    private static List<String> getWert(JSONObject json, List<String> keys) {

        List<String> anzahl = new ArrayList<>();
        for(int i = 0; i< keys.size();i++) {
            JSONObject bestaetigt = (JSONObject) json.get(keys.get(i));
            anzahl.add( bestaetigt.getString("datum"));

        }
        return anzahl;
    }
    private static void listenSortieren(List<LocalDate> Montage,
                                        List<LocalDate> Dienstage, List<LocalDate> Mittwoche, List<LocalDate> Donnerstage,
                                        List<LocalDate> Freitage, List<LocalDate> Samstage, List<LocalDate> Sonntage){
        Collections.sort(Montage);
        Collections.sort(Dienstage);
        Collections.sort(Mittwoche);
        Collections.sort(Donnerstage);
        Collections.sort(Freitage);
        Collections.sort(Samstage);

    }
}