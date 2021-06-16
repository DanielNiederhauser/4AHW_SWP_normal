import javax.security.auth.login.LoginException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.stream.Collectors;

public class zweihunderterNormal {
    final static String hostname = "localhost";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";
    static Connection conn = null;

    static double depot;
    static String aktie;

    static LocalDate verkaufdatum, kaufdatum;
    static double kaufclosewert, verkaufclosewert;

    static double closeWert, schnitt200;

    static List<LocalDate> alleDatums;

    static int anzahlaktien;
    static double gesamtDepot;
    //Wenn alarm true -> keine Daten mehr in der DB-> man ist am letzten Tag der DB
    static boolean alarm=false;
    static LocalDate tempVerk;
    static Scanner reader = new Scanner(System.in);
    public static void main(String[] args) {
        conn = null;;
        boolean willEingabe=true;
        if(willEingabe)tempVerk=richtigeLocalDateEingabe("Startdatum [JJJJ-MM-DD]: ");
        else tempVerk=LocalDate.of(2017,01,01);

        gesamtDepot=doubleEingeben("Gesamtdepot: [Egal ob int oder double]");
        System.out.println();

        zweihunderter();


    }
    public static void zweihunderter(){
        createTable();
        List<String> aktien = Aktien.ladeDatei("src/AktienListe.txt");

        for(int a=0;a<aktien.size();a++) {
            depot=gesamtDepot/aktien.size();


            aktie = aktien.get(a);
            int zaehler = 0;
            verkaufdatum= tempVerk;
            dummie(verkaufdatum.minusDays(1));

            alleDatums = getAlleDatums();
            System.out.println("Startdepot "+ aktie+": "+depot);

            do {
                do {
                    closeWert = getClose(zaehler);
                    if(alarm==true) break;
                    schnitt200 = get200(zaehler);
                    if(alarm==true) break;
                    if (closeWert < schnitt200) {
                        kaufdatum = alleDatums.get(zaehler);
                        break;
                    }
                    zaehler++;
                } while (closeWert > schnitt200);
                if(alarm) break;


                einkaufen();
                //Hilfe fürs debuggen
                //System.out.println("KAUFEN: " + kaufdatum + "---" + closeWert + "---" + schnitt200);
                //System.out.println("anzahl Aktien: " + anzahlaktien + " Restdepot: " + depot);

                dbEintrag(1);

                do {
                    zaehler++;
                    closeWert = getClose(zaehler);
                    if(alarm)break;
                    schnitt200 = get200(zaehler);
                    if(alarm)break;
                    if (closeWert > schnitt200) {
                        verkaufdatum = alleDatums.get(zaehler);
                        break;
                    }
                } while (closeWert < schnitt200);
                if(alarm)break;
                verkaufen();
                //Hilfe fürs debuggen
                //System.out.println("VERKAUFEN: " + verkaufdatum + "---" + closeWert + "---" + schnitt200);
                //System.out.println("Depot: " + depot);
                dbEintrag(0);

            } while (zaehler <= alleDatums.size());
            getLetztenEintrag();
            alarm=false;
            enddepot();
        }
    }
    public static void createTable(){

        try {
            conn=DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            System.out.println("* Tabelle 200strategie erstellen, falls nicht vorhanden");
            String sql = "CREATE TABLE if not exists 200strategie(Datum date, Aktie varchar(10), Kaufen boolean, Anzahl Integer, Depot double, PRIMARY KEY(Datum));";
            myStat.executeUpdate(sql);

        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }

    public static List<LocalDate> getAlleDatums(){
        List<LocalDate> list = new ArrayList<>();
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select Datum from "+aktie +" where Datum >='"+verkaufdatum+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            while (reSe.next()) {
                list.add(LocalDate.parse(reSe.getString("Datum")));
            }
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return list;
    }

    public static double getClose(int zaehler){
        double wert = 0;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" where Datum ='"+alleDatums.get(zaehler)+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                wert=(reSe.getDouble("Wert"));
            }
            else{
                alarm=true;
            }
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return wert;
    }
    public static double get200(int zaehler){
        double wert=0;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select Wert from "+aktie +"_200schnitt where Datum ='"+alleDatums.get(zaehler)+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                wert= (reSe.getDouble("Wert"));
            }
            else{
                alarm=true;
            }
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

        return wert;
    }
    public static void einkaufen(){
        anzahlaktien = (int) Math.floor(depot / closeWert);
        depot=depot-(anzahlaktien*closeWert);
    }
    public static void verkaufen(){
        depot+=anzahlaktien*closeWert;
        anzahlaktien=0;
    }
    static public boolean istBuchstabenkette(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }
    //Dummieeintrag
    public static void dummie(LocalDate ld){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();


            String sql = "INSERT IGNORE INTO 200strategie values('"+ld+"','"+aktie+"',"+0+","+anzahlaktien+","+depot+");";
            //System.out.println(sql);
            myStat.execute(sql);


        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }

    //oans->Kaufen; 0-> verkaufen
    public static void dbEintrag(int oansOdaNull){
        try {
            LocalDate temp=null;
            if(oansOdaNull==0){
                temp=verkaufdatum;
            }
            if(oansOdaNull==1){
                temp=kaufdatum;
            }

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();


            String sql = "INSERT IGNORE INTO 200strategie values('"+temp+"','"+aktie+"',"+oansOdaNull+","+anzahlaktien+","+depot+");";
            //System.out.println(sql);
            myStat.execute(sql);


        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }

    //Enddepot -> gibt letzten Verkauf (ist eh immer verkauf) von der Aktie zum vergleichen
    public static void enddepot(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "select depot from 200strategie where aktie='"+aktie+"' order by Datum desc limit 1;";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                System.out.println("Enddepot "+aktie+": "+(reSe.getDouble("Depot"))+"\n");
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

    public static void getLetztenEintrag(){
        int oansOdaNull;
        LocalDate loeschDatum;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from 200strategie where aktie='"+aktie+"' order by Datum desc limit 1;";
            ResultSet reSe=myStat.executeQuery(sql);

            if(reSe.next()){
                oansOdaNull= (reSe.getInt("Kaufen"));
                loeschDatum=LocalDate.parse((reSe.getString("Datum")));
                if(oansOdaNull==1){
                    loeschDatum(loeschDatum);
                }
            }
            else {
                System.exit(1);
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
    public static void loeschDatum(LocalDate ld){
        try {

            Connection conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Delete from 200strategie where Datum='"+ld+"' and aktie='"+aktie+"';";
            //System.out.println(sql);
            myStat.execute(sql);

            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

    }
    public static LocalDate richtigeLocalDateEingabe(String text){
        String eingabe;
        LocalDate ld = LocalDate.MIN;
        boolean korrekt = false;
        do{
            try{
                System.out.print(text);
                eingabe=reader.next();

                ld = LocalDate.parse(eingabe, DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT));
                korrekt=true;
            }catch (Exception e){
                System.out.println("Falsche Eingabe! Format: [JJJJ-MM-DD]");
            }

        }while(!korrekt);
        return ld;
    }
    public static double doubleEingeben(String text){
        //lasst so lange Zeichenkette einlesen bis INT oder DOUBLE korrekt eingegeben wird
        //-> bei INT wird es zu Double konvertiert
        boolean korrekt=true;
        double eingabe= 0.0;
        do {
            System.out.print(text);
            String input = reader.next();
            try {
                double isNum = Double.parseDouble(input);
                if (isNum == Math.floor(isNum)) {
                    korrekt=true;
                    eingabe= Double.valueOf(input);
                    //enter a double again
                } else {
                    korrekt=true;
                    eingabe= Double.valueOf(input);
                    //break
                }
            } catch (Exception e) {
                if (input.toCharArray().length == 1) {
                    System.out.println("Input ist ein Char");
                    korrekt=false;

                    //enter a double again
                } else {
                    System.out.println("Input ist ein String");
                    korrekt=false;

                    //enter a double again
                }
            }
        }while(korrekt==false);
        return eingabe;
    }

}
