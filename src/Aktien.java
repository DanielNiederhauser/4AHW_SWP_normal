import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Aktien extends Application{
    @Override public void start(Stage stage) {
        stage.setTitle("Aktienkurs");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Aktienkurs");
        xAxis.setLabel("Tag");
        yAxis.setLabel("Wert");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Aktienkurs: ");
        series1.getData().add(new XYChart.Data(100, 200));
        series1.getData().add(new XYChart.Data(150, 100));
        series1.getData().add(new XYChart.Data(120, 130));

        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(series1);
        stage.setScene(scene);
        stage.show();
    }
    //DB-Stuff
    final static String hostname = "localhost";
    final static String port = "3306";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

    //static String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=60min&outputsize=full&apikey=WEO2Z2E1M7UWU3QX";
    static String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&outputsize=full&apikey=WEO2Z2E1M7UWU3QXX";


    //https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&outputsize=full&apikey=WEO2Z2E1M7UWU3QX
    public static void main(String[] args) throws IOException {
        List<String> gefundene = new ArrayList<>();
        List<String> zuSuchen = new ArrayList<>();
        zuSuchen.add("4. close");
        for (int i = 0; i <= 10; i++) {
            JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
            List<String> tage = getWert(json,gefundene);

        }
        launch(args);
        int zaehler=0;
        for (String i : gefundene){
            System.out.println(gefundene);
            zaehler++;
        }
        System.out.println(gefundene);

    }

    private static List<String> getWert(JSONObject json, List<String> keys) {
        List<String> anzahl = new ArrayList<>();
        for(int i = 0; i< keys.size();i++) {
            JSONObject jsonO = (JSONObject) json.get(keys.get(i));
            anzahl.add( jsonO.getString("1. Information"));

        }
        return anzahl;
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
            System.out.println("* Tabelle Aktien erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists AAPL" +
                    "(Datum datetime, Wert double)";
            myStat.executeUpdate(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }
    static Connection conn = null;
    static double Wert=100.05;
    private static void Datenbankeintrag(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String sql = "INSERT INTO"+"AAPL"+" values(" +"'"+timestamp+"',"+Wert+")";
            myStat.execute(sql);

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
            ResultSet reSe=myStat.executeQuery("Select * from "+"AAPL");
            System.out.println("Zeit                                 Wert");
            while(reSe.next()){
                String zeit = reSe.getString("Datum");
                String Montag = reSe.getString("Montag");

                System.out.printf("%1s",zeit);
                System.out.printf("%20s", Montag);
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
