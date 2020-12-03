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
import java.sql.*;

public class Kalender extends Application {
    final static String hostname = "localhost";
    final static String port = "3306";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

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

    public static List<LocalDate> Feiertage = new ArrayList<>();

    public static List<LocalDate> montage = new ArrayList<>();
    public static List<LocalDate> dienstage = new ArrayList<>();
    public static List<LocalDate> mittwoche = new ArrayList<>();
    public static List<LocalDate> donnerstage = new ArrayList<>();
    public static List<LocalDate> freitage = new ArrayList<>();
    public static List<LocalDate> samstage = new ArrayList<>();
    public static List<LocalDate> sonntage = new ArrayList<>();
    public static void main(String[] args)throws JSONException, MalformedURLException, IOException {
        Scanner reader = new Scanner(System.in);
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

        zaehlMethode();

        //Listen sortieren für schönere Ausgabe
        listenSortieren(montage,dienstage,mittwoche,donnerstage,freitage,samstage,sonntage);

        System.out.println("Feiertaganzahl alleine [a] oder die Feiertage selbst auch [s] ausgeben?");
        String wahl=reader.next();
        if(wahl.equals("a")){
            feiertagNurAnzahlAusgeben(montag, dienstag, mittwoch, donnerstag, freitag, samstag, sonntag);
        }
        if(wahl.equals("s")){
            feiertagAnzahlAusgebenErweitert(montag, dienstag, mittwoch, donnerstag, freitag, samstag, sonntag);

        }
        launch(args);
        //Für Datenbank Tabelle erstellen, falls nicht vorhanden
        System.out.println("Wollen Sie mit der Datenbank interagieren? [ja,nein]");
        if(reader.next().equals("nein")){
                try {
                    conn.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }

            System.exit(0);
        }
        CreateTable();
        System.out.println("Wollen Sie Die Eingabe speichern?[ja,nein]");
        if(reader.next().equals("ja")){
            Datenbankeintrag();
        }
        System.out.println("Wollen Sie Die Datenbank ausgeben?[ja,nein]");
        if(reader.next().equals("ja")){
            Datenbankausgabe();
        }
        else{
            try {
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

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
    public static void zaehlMethode(){
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
    }

    public static void feiertagAnzahlAusgebenErweitert(int mo, int di, int mi, int don, int fr, int sa, int so) {
        System.out.println("Montage: " + mo + " " + montage);
        System.out.println("Dienstage: " + di + " " + dienstage);
        System.out.println("Mittwoche: " + mi + " " + mittwoche);
        System.out.println("Donnerstage: " + don + " " + donnerstage);
        System.out.println("Freitage: " + fr + " " + freitage);
        System.out.println("Samstage: " + sa + " " + samstage);
        System.out.println("Sonntage: " + so + " " + sonntage);
    }
    public static void feiertagNurAnzahlAusgeben(int mo, int di, int mi, int don, int fr, int sa, int so) {
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
            JSONObject jsonO = (JSONObject) json.get(keys.get(i));
            anzahl.add( jsonO.getString("datum"));

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
    static Connection conn = null;
    private static void Datenbankeintrag(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String sql = "INSERT INTO Kalender values(" +"'"+timestamp+"',"+montag+","+dienstag+","+mittwoch+","+donnerstag+","+freitag+","+samstag+","
                    +sonntag+","+startjahr+","+endjahr+")";
            myStat.execute(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }
    private static void CreateTable(){
        Connection conn = null;

        try {
            System.out.println("* Treiber laden");
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Exception e) {
            System.err.println("Unable to load driver.");
            e.printStackTrace();
        }
        try {
            System.out.println("* Verbindung aufbauen");
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            System.out.println("* Tabelle Kalender erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists Kalender" +
                    "(Datum datetime, " +
                    "Montag int, "+
                    "Dienstag int, "+
                    "Mittwoch int, "+
                    "Donnerstag int, "+
                    "Freitag int, "+
                    "Samstag int, "+
                    "Sonntag int, "+
                    "Startjahr int, "+
                    "Endjahr int)";
            myStat.executeUpdate(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }

    private static void Datenbankausgabe(){
        final String hostname = "localhost";
        final String port = "3306";
        final String dbname = "java";
        final String user = "java";
        final String password = "java";

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from kalender");
            System.out.println("Zeit                                 Montag      Dienstag        Mittwoch        Donnerstag      Freitag     Samstag" +
                    "       Sonntag         Startjahr       Endjahr");
            while(reSe.next()){
                String zeit = reSe.getString("Datum");
                String Montag = reSe.getString("Montag");
                String Dienstag = reSe.getString("Dienstag");
                String Mittwoch = reSe.getString("Mittwoch");
                String Donnerstag = reSe.getString("Donnerstag");
                String Freitag = reSe.getString("Freitag");
                String Samstag = reSe.getString("Samstag");
                String Sonntag = reSe.getString("Sonntag");
                String startjahr =reSe.getString("Startjahr");
                String endjahr =reSe.getString("Endjahr");


                System.out.printf("%1s",zeit);
                System.out.printf("%20s", Montag);
                System.out.printf("%11s", Dienstag);
                System.out.printf("%16s", Mittwoch);
                System.out.printf("%17s", Donnerstag);
                System.out.printf("%15s", Freitag);
                System.out.printf("%12s", Samstag);
                System.out.printf("%14s", Sonntag);
                System.out.printf("%19s", startjahr);
                System.out.printf("%16s", endjahr);
                System.out.println();
            }

            System.out.println("* Datenbank-Verbindung beenden");
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

    }

}