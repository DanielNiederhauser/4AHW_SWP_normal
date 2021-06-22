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
    static Map<LocalDate, Double> aktienPreiseTreemapBerechnet = new TreeMap<LocalDate, Double>();
    static Map<LocalDate, Double> Splitmap = new TreeMap<>();

    static Double gleitdurchschnitt;
    static int gleitdurchschnittAnzahl;
    static Double letzterCloseWert;
    static Connection conn = null;
    private static String marke;
    private static Double split=1.0;
    static Map<LocalDate, Double> gleitdurchschnittTreemap = new TreeMap<>();
    static List<LocalDate> LocaldateListeGleitdurchschnitt = new ArrayList<>();


    @Override public void start(Stage stage) throws Exception{
        List<String> aktien = ladeDatei("src/AktienListe.txt");
        String apiKey = ladeKey("src/APIKey.txt");

        for(int a=0;a<aktien.size();a++){
            dates.clear();
            marke = aktien.get(a);
            String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + marke + "&outputsize=full&apikey="+apiKey;
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
            CreateTableRohdaten();
            DatenbankeintragRohdaten();
            System.out.println("Rohdateien fertig!");


            //splitkorriegieren();
            CreateTable();
            for (int i = 0; i < dates.size(); i++) {
                String temp = dates.get(i);
                aktienPreiseTreemapBerechnet.put(LocalDate.parse(temp), getSplitkorrigiert(temp));
            }
            Datenbankeintrag();
            System.out.println("DB Eintrag fertig");



            letztexDatums();
            //System.out.println("LocaldateListeGleitdurchschnitt size: "+ LocaldateListeGleitdurchschnitt.size());

            Collections.sort(LocaldateListeGleitdurchschnitt);

            Map<LocalDate, Double> TreemapAlleFuerGleitdurchschnitt = GetAlleWerteFuerGleitdurchschnitt();
            //System.out.println("TreemapAlleFuerGleitdurchschnitt:" +TreemapAlleFuerGleitdurchschnitt.size());
            //macht eine Treemap von Werten, die später für die Berechnung der Double Liste für den Graphen gebraucht  wird
            //macht eine Double Liste wo die Werte in der richtigen Reihenfolge (dem Datum nach) sind
            List<Double> DoubleListeUnberechnet = treeMapZuGeordnetenDoubleListe(TreemapAlleFuerGleitdurchschnitt);
            //System.out.println("DoublelisteUnberechnet size: "+ DoubleListeUnberechnet.size());
            //Berechnete Gleitdurchschnittliste
            JavaFXGleitdurchschnitt = GleitdurchschnittList(DoubleListeUnberechnet);
            //System.out.println("JavaFXGleitdurchschnitt size: "+ JavaFXGleitdurchschnitt.size());
            //System.out.println(JavaFXGleitdurchschnitt);
            //db eintrag
            fertigeGleitdurchschnittTreemap();



            CreateTableGleitdurchschnitt();
            //System.out.println("Gleitdurchschnittlist"+gleitdurchschnittTreemap);
            //System.out.println("Size: "+ gleitdurchschnittTreemap.size());
            DatenbankeintragxSchnitt();

            //Liste für normalen Graphen also den, wo nicht der x Schnitt ist
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
            //System.out.println("Letzer Eintrag normal: " +javaFXTreemap.get(javaFXTreemap.lastKey()));
            //System.out.println("Letzer Eintrag Gleitdurchschnitt: "+ JavaFXGleitdurchschnitt.get(JavaFXGleitdurchschnitt.size()-1));

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

        gleitdurchschnittAnzahl=200;

        //anzahlGrafik = 500;

        //System.out.print("Von welchem Datum rückwirkend? [1999-10-01]");
        //startdatum = LocalDate.parse(reader.next());

        startdatum=LocalDate.of(2012,01,01);
        if(startdatum==null){
            startdatum=LocalDate.now();
        }
        Application.launch(args);
    }
    private static double getWert (String key) throws JSONException {

        JSONObject jsonO = (JSONObject) o.get(key);
        String Wert = jsonO.getString("4. close");
        return Double.parseDouble(Wert);
    }
    private static double getSplitkorrigiert (String key) throws JSONException {

        JSONObject jsonO = (JSONObject) o.get(key);
        String Wert = jsonO.getString("5. adjusted close");
        return Double.parseDouble(Wert);
    }
    private static Double getSplit (String key) throws JSONException {

        JSONObject jsonO = (JSONObject) o.get(key);
        String split = jsonO.getString("8. split coefficient");
        return Double.parseDouble(split);
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
                    "(Datum date, Wert double, Split double, PRIMARY KEY(Datum));";
            myStat.executeUpdate(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }
    private static void CreateTableGleitdurchschnitt(){
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
            System.out.println("* Tabelle "+marke+"_"+gleitdurchschnittAnzahl+"Schnitt erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists "+marke+"_"+gleitdurchschnittAnzahl+"Schnitt(Datum date, Wert double, PRIMARY KEY(Datum));";
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

            for (LocalDate i : aktienPreiseTreemapBerechnet.keySet()) {
                String sql = "INSERT IGNORE INTO " + marke +" values('"+i+"',"+aktienPreiseTreemapBerechnet.get(i)+", "+Splitmap.get(i)+");";
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
    private static void DatenbankeintragRohdaten(){
        try {

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();

            for (LocalDate i : aktienPreiseTreemap.keySet()) {
                String sql = "INSERT IGNORE INTO " + marke +"_ROH values('"+i+"',"+aktienPreiseTreemap.get(i)+");";
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

    private static void fillBerechnete(){

    }
    private static void fertigeGleitdurchschnittTreemap(){

        for(int i=0;i<AnzahlDatenbankeintraege();i++){

            gleitdurchschnittTreemap.put(LocaldateListeGleitdurchschnitt.get(i), JavaFXGleitdurchschnitt.get(i));
        }

    }
    private static void letztexDatums(){

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke+" order by Datum DESC limit "+(AnzahlDatenbankeintraege()+1)+";");
            while(reSe.next()){
                String zeit = reSe.getString("Datum");

                LocaldateListeGleitdurchschnitt.add(LocalDate.parse(zeit));
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
    private static void DatenbankeintragxSchnitt(){
        try {

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();

            for (LocalDate i : gleitdurchschnittTreemap.keySet()) {
                String sql = "INSERT IGNORE INTO " + marke +"_"+gleitdurchschnittAnzahl+"Schnitt values('"+i+"',"+gleitdurchschnittTreemap.get(i)+");";
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
    private static TreeMap<LocalDate, Double> javaFXWerteNormal(int anzahl){
        TreeMap<LocalDate, Double> treeMap = new TreeMap<LocalDate, Double>();

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke +" where Datum > '"+startdatum+"';");
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
    private static Map<LocalDate, Double> GetAlleWerteFuerGleitdurchschnitt(){
        try {
            Map<LocalDate, Double> treeMap = new TreeMap<LocalDate, Double>();

            Connection conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            ResultSet reSe=myStat.executeQuery("Select * from "+marke +" order by Datum desc limit "+(AnzahlDatenbankeintraege()+gleitdurchschnittAnzahl)+";");
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
    public static List<Double> GleitdurchschnittList(List<Double> unberechneteListe){
        List<Double> JavaFXGleitdurchschnitt= new ArrayList<>();
        double Wert=0;

        for(int i = 0;i<unberechneteListe.size()-gleitdurchschnittAnzahl;i++){
            for(int j=i;j<gleitdurchschnittAnzahl+i;j++){
                Wert+=unberechneteListe.get(j);
            }
            JavaFXGleitdurchschnitt.add(Wert/gleitdurchschnittAnzahl);

            Wert=0;
        }
        return JavaFXGleitdurchschnitt;
    }
    public static List<Double> treeMapZuGeordnetenDoubleListe(Map<LocalDate, Double> treemap){
        List<Double> fertige = new ArrayList<Double>();
        for (LocalDate i : treemap.keySet()) {
            fertige.add(treemap.get(i));
        }
        return fertige;
    }
    public static List<String> ladeDatei(String datName) {
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
    private static String ladeKey(String path) {
        File file = new File(path);
        String key="";

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
            String zeile = null;
            if ((zeile = in.readLine()) != null) {
                key = zeile;
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
        return key;
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
    public static Integer AnzahlDatenbankeintraege(){
        int anzahl=0;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select count(*) from "+marke +" where Datum >= '"+startdatum+"';";
            //Select count(Wert) from tsla where Datum < '2021-04-21';
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                String temp = reSe.getString(1);
                anzahl=Integer.parseInt(temp);
            }

            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return anzahl;
    }
    public static void treemapausgabe(){
        for (LocalDate i : aktienPreiseTreemap.keySet()) {
            aktienPreiseTreemap.replace(i, /*Splitkorrigiertes*/200.0);
        }
    }
    public static void splitkorriegieren(){
        int size = aktienPreiseTreemap.size();
        Map<LocalDate, Double> tempMap = aktienPreiseTreemap;
        double tempDouble, tempSplit;
        for(int i=0;i<size;i++){
            LocalDate ld = ((TreeMap<LocalDate, Double>) tempMap).lastKey();
            double splitTemp = getSplit(ld.toString());
            tempDouble= getWert(ld.toString());
            tempSplit=getSplit(ld.toString());
            aktienPreiseTreemapBerechnet.put(ld,tempDouble/split);
            Splitmap.put(ld, tempSplit);
            split*=splitTemp;

            tempMap.remove(ld);
        }
    }
    private static void CreateTableRohdaten(){
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
            System.out.println("* Tabelle "+marke+"_roh erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists "+marke +"_roh"+
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
}