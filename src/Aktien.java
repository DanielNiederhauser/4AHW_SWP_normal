import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;


//Primarykey muss Datum sein

public class Aktien extends Application{

    final static String hostname = "localhost";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

    //Die Liste der letzten x Einträge aus der Datenbank, wobei x vom Benutzer eingegeben wird
    static TreeMap<LocalDate, Double> javaFXTreemap = new TreeMap<LocalDate, Double>();
    static List<Double> JavaFXGleitdurchschnitt = new ArrayList<Double>();
    //Die Liste der letzten 100 aus der API
    static Map<LocalDate, Double> aktienPreiseTreemap = new TreeMap<LocalDate, Double>();
    static Double gleitdurchschnitt;
    static int gleitdurchschnittAnzahl;
    static Double letzterCloseWert;
    static Connection conn = null;
    private static String marke;


    @Override public void start(Stage stage) throws Exception{
        List<String> aktien = ladeDatei("src/AktienListe.txt");


        for(int a=0;a<aktien.size();a++){
            marke = aktien.get(a);
            //A4RM4YPCEWJ1VANI3
            String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + marke + "&outputsize=full&apikey=WEO2Z2E1M7UWU3QXX";
            JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
            o = json.getJSONObject("Time Series (Daily)");

            for (int i = 0; i < o.length(); i++) {
                dates.add(o.names().get(i).toString());
            }
            Collections.sort(dates);
            for (int i = 0; i < dates.size(); i++) {
                String temp = dates.get(i);
                aktienPreiseTreemap.put(LocalDate.parse(temp), getWert(temp));
            }
            CreateTable();
            Datenbankeintrag();

            /*System.out.println("Table ausgeben?");
            if (reader.next().equals("ja")) {
                Datenbankausgabe();
            }*/

            Gleitdurchschnitt(gleitdurchschnittAnzahl);

            Map<LocalDate, Double> letztexmal2Werte = GetAlleWerteFuerGleitdurchschnitt(anzahlGrafik);
            List<Double> letztexmal2WerteDouble = treeMapZuGeordnetenListe(letztexmal2Werte);

            JavaFXGleitdurchschnitt = GleitdurchschnittList(letztexmal2WerteDouble);

            javaFXTreemap = javaFXWerteNormal(anzahlGrafik);

            stage.setTitle("Aktienkurs");
            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Tag");
            final LineChart<String,Number> lineChart = new LineChart<String, Number>(xAxis,yAxis);
            lineChart.setTitle("Aktienkurs "+ marke.toUpperCase());
            XYChart.Series series = new XYChart.Series();
            XYChart.Series series1 = new XYChart.Series();

            //
            /*for(Map.Entry e : javaFXTreemap.entrySet()){
                System.out.println(e.getKey() + " = " +e.getValue());
            }
            for(int z = 0; z<JavaFXGleitdurchschnitt.size();z++){
                System.out.println("Gleitdurchschnitt: "+ JavaFXGleitdurchschnitt.get(z));
            }*/
            //
            int b=0;
            for (LocalDate i : javaFXTreemap.keySet()) {
                series.getData().addAll(new XYChart.Data(i.toString(), javaFXTreemap.get(i)));
                series1.getData().addAll(new XYChart.Data(i.toString(), JavaFXGleitdurchschnitt.get(b)));
                b++;
            }
            System.out.println("Letzer Eintrag normal: " +javaFXTreemap.get(javaFXTreemap.lastKey()));
            System.out.println("Letzer Eintrag Gleitdurchschnitt: "+ JavaFXGleitdurchschnitt.get(JavaFXGleitdurchschnitt.size()-1));


            Scene scene  = new Scene(lineChart,1300,800);



            lineChart.getData().add(series);
            lineChart.getData().add(series1);
            lineChart.setCreateSymbols(false);

            if(JavaFXGleitdurchschnitt.get(JavaFXGleitdurchschnitt.size()-1)>getLastCloseWert()){
                scene.getStylesheets().add("red.css");
            }
            else {
                scene.getStylesheets().add("green.css");

            }
            scene.getStylesheets().add("lineFarben.css");

            series.setName("Close Werte");
            series1.setName("Gleitdurchschnitt");

            saveAsPng(lineChart, "src//Bilder//"+marke.toUpperCase()+"_"+LocalDate.now()+".png");
            stage.setScene(scene);
            stage.show();
            stage.close();
            System.out.println("fertig mit "+marke);
            System.out.println();
        }

    }



    static List<String> dates = new ArrayList<>();
    static int anzahlGrafik;
    static JSONObject o;
    static Scanner reader = new Scanner(System.in);
    static LocalDate startdatum;
    public static void main(String[] args) throws IOException, SQLException {

        System.out.print("Wieviele der letzten Einträge sollen für den Gleidurchschnitt verwendet werden? " );
        gleitdurchschnittAnzahl=reader.nextInt();

        System.out.print("Wieviele der letzten Einträge wollen Sie in der Grafik sehen? ");
        anzahlGrafik = reader.nextInt();

        System.out.print("Von welchem Datum rückwirkend? [1999-10-01]");
        startdatum = LocalDate.parse(reader.next());

        Application.launch(args);

    }
    private static double getWert (String key) throws JSONException {

        JSONObject jsonO = (JSONObject) o.get(key);
        String Wert = jsonO.getString("5. adjusted close");
        return Double.parseDouble(Wert);
    }
    private static void CreateTable(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Exception e) {
            System.err.println("Unable to load driver.");
            e.printStackTrace();
        }
        try {
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
            ResultSet reSe=myStat.executeQuery("Select * from "+marke+" where Datum < '" + startdatum+ "';");
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

            String sql = "SELECT round(AVG(wert),2) as 'Durchschnitt' FROM (SELECT wert FROM "+marke+" where Datum <'"+startdatum+"' ORDER BY Datum DESC LIMIT "
                    +anzahlGleitdurchschnitt+") as t;";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                String gleitdurchschnittString = reSe.getString(1);
                gleitdurchschnitt=Double.parseDouble(gleitdurchschnittString);
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("SQL Fehler Gleitdurchschnitt");
        }
    }
    private static TreeMap<LocalDate, Double> javaFXWerteNormal(int anzahl){
        TreeMap<LocalDate, Double> treeMap = new TreeMap<LocalDate, Double>();

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke +" where Datum < '"+startdatum+"' order by Datum DESC Limit "+anzahl+ ";");
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
            String sql = "Select Wert from "+marke +" where Datum < '"+startdatum+"' order by Datum DESC limit 1 "+";";
            ResultSet reSe=myStat.executeQuery(sql);
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
    private static Map<LocalDate, Double> GetAlleWerteFuerGleitdurchschnitt(int anzahl){
        try {
            Map<LocalDate, Double> treeMap = new TreeMap<LocalDate, Double>();

            Connection conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke +" where Datum < '"+startdatum+"' order by Datum desc limit "+
                    (anzahl+gleitdurchschnittAnzahl)+";");
            while(reSe.next()){
                String datum = reSe.getString("Datum");
                String Wert = reSe.getString("Wert");

                LocalDate tempLocaldate = LocalDate.parse(datum);
                Double tempDouble = Double.parseDouble(Wert);
                treeMap.put(tempLocaldate,tempDouble);
            }
            conn.close();
            return treeMap;
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return null;
    }
    public static List<Double> GleitdurchschnittList(List<Double> anzahlPlusGleitdurchschnittanzahl){
        List<Double> JavaFXGleitdurchschnitt= new ArrayList<>();
        double Wert=0;

        for(int i = 0;i<anzahlPlusGleitdurchschnittanzahl.size()-gleitdurchschnittAnzahl;i++){
            for(int j=i;j<gleitdurchschnittAnzahl+i;j++){
                Wert+=anzahlPlusGleitdurchschnittanzahl.get(j);
            }
            JavaFXGleitdurchschnitt.add(Wert/gleitdurchschnittAnzahl);

            Wert=0;
        }
        return JavaFXGleitdurchschnitt;
    }
    public static List<Double> treeMapZuGeordnetenListe(Map<LocalDate, Double> treemap){
        List<Double> fertige = new ArrayList<Double>();
        for (LocalDate i : treemap.keySet()) {
            fertige.add(treemap.get(i));
        }
        return fertige;
    }
    private static List<String> ladeDatei(String datName) {
        List<String> fertige = new ArrayList<>();
        File file = new File(datName);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(datName));
            String zeile = null;
            while ((zeile = in.readLine()) != null) {
                fertige.add(zeile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
        return fertige;
    }
    public void saveAsPng(LineChart lineChart, String path) {
        WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}