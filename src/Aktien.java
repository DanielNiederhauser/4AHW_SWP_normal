import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

//Primarykey muss Datum sein

public class Aktien extends Application{

    final static String hostname = "localhost";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

    //Die Liste der letzten x Einträge aus der Datenbank, wobei x vom Benutzer eingegeben wird
    static Map<LocalDate, Double> javaFXTreemap = new TreeMap<LocalDate, Double>();
    //Die Liste der letzten 100 aus der API
    static Map<LocalDate, Double> aktienPreiseTreemap = new TreeMap<LocalDate, Double>();
    static Double gleitdurchschnitt;
    static Double letzterCloseWert;
    static Connection conn = null;
    static String marke;


    @Override public void start(Stage stage) {
        stage.setTitle("Aktienkurs");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Tag");
        final LineChart<String,Number> lineChart = new LineChart<String, Number>(xAxis,yAxis);
        lineChart.setTitle("Aktienkurs "+ marke.toUpperCase());
        XYChart.Series series = new XYChart.Series();
        series.setName("Close Werte");

        for (LocalDate i : javaFXTreemap.keySet()) {
            series.getData().addAll(new XYChart.Data(i.toString(), javaFXTreemap.get(i)));
        }

        if(gleitdurchschnitt>getLastCloseWert()){
            lineChart.setStyle("-fx-background-color: #FF0000;");
        }
        else {
            lineChart.setStyle("-fx-background-color: #00FF00;");

        }

        Scene scene  = new Scene(lineChart,1300,800);
        lineChart.getData().add(series);

        stage.setScene(scene);
        stage.show();
    }

    static List<String> dates = new ArrayList<>();
    static int anzahlGrafik;

    static JSONObject o;
    public static void main(String[] args) throws IOException, SQLException {
        Scanner reader = new Scanner(System.in);
        System.out.println("Von welcher Marke wollen Sie den Aktienkurs der Letzten 100 Tage wissen?[TSLA, AAPL, AMZN, ...]");
        marke = reader.next();

        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="+marke+"&outputsize=compact&apikey=WEO2Z2E1M7UWU3QXX";
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        o = json.getJSONObject("Time Series (Daily)");

        for(int i = 0;i<100;i++){
            dates.add(o.names().get(i).toString());
        }
        Collections.sort(dates);
        for(int i=0;i<dates.size();i++) {
            String temp = dates.get(i);
            aktienPreiseTreemap.put(LocalDate.parse(temp), getWert(temp));
        }
        CreateTable();
        Datenbankeintrag();

        System.out.println("Table ausgeben?");
        if(reader.next().equals("ja")){
            Datenbankausgabe();
        }

        System.out.print("Wieviele der letzten Einträge sollen für den Gleidurchschnitt verwendet werden?" );
        Gleitdurchschnitt(reader.nextInt());
        System.out.println("Letzter close-Wert: "+getLastCloseWert());
        System.out.println("Gleidurchschnitt: "+ gleitdurchschnitt);

        System.out.print("Wieviele der letzten Daten wollen Sie in der Grafik sehen? ");
        anzahlGrafik=reader.nextInt();
        javaFXTreemap=javaFX(anzahlGrafik);
        Application.launch(args);

    }
    private static double getWert (String key) throws JSONException {

        JSONObject jsonO = (JSONObject) o.get(key);
        String Wert = jsonO.getString("4. close");
        return Double.parseDouble(Wert);
    }

    private static void CreateTable(){

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
            System.out.println("* Tabelle "+marke+" erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists "+marke +
                    "(Datum date, Wert double, PRIMARY KEY(Datum));";
            myStat.executeUpdate(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }
    private static void Datenbankeintrag(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();

            for (LocalDate i : aktienPreiseTreemap.keySet()) {
                String sql = "INSERT IGNORE INTO " + marke +" values('"+i+"',"+aktienPreiseTreemap.get(i)+");";
                myStat.execute(sql);
            }

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }
    private static void Datenbankausgabe(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke);
            System.out.println("Datum                   Wert");
            while(reSe.next()){
                String zeit = reSe.getString("Datum");
                String Wert = reSe.getString("Wert");

                System.out.printf("%1s",zeit);
                System.out.printf("%20s", Wert);

                System.out.println();
            }

            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

    }
    private static void Gleitdurchschnitt(int anzahlGleitdurchschnitt) throws SQLException {
        try{
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();

            ResultSet reSe=myStat.executeQuery("SELECT round(AVG(wert),2) as 'Durchschnitt' FROM (SELECT wert FROM "+marke+" ORDER BY Datum DESC LIMIT "
                    +anzahlGleitdurchschnitt+") as t;");
            if (reSe.next()) {
                String gleitdurchschnittString = reSe.getString(1);
                gleitdurchschnitt=Double.parseDouble(gleitdurchschnittString);
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("SQL Fehler Gleitdurchschnitt");
        }
    }
    private static Map<LocalDate, Double> javaFX(int anzahl){
        Map<LocalDate, Double> treeMap = new TreeMap<LocalDate, Double>();

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke +" order by Datum DESC Limit "+anzahl);
            while(reSe.next()){
                String datum = reSe.getString("Datum");
                String Wert = reSe.getString("Wert");

                LocalDate tempLocaldate = LocalDate.parse(datum);
                Double tempDouble = Double.parseDouble(Wert);
                treeMap.put(tempLocaldate,tempDouble);

            }

            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return treeMap;

    }
    private static double getLastCloseWert(){
        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select Wert from "+marke +" order by Datum DESC limit 1");
            if (reSe.next()) {
                String temp = reSe.getString(1);
                letzterCloseWert=Double.parseDouble(temp);
            }

            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return letzterCloseWert;
    }

}