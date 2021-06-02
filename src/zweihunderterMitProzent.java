import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class zweihunderterMitProzent {
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
    static double prozent;
    public static void main(String[] args) {
        verkaufdatum=LocalDate.of(2017,01,01);
        depot=100000.0;
        aktie="TSLA";
        prozent=0.03;

        zweihunderter();


    }
    public static void zweihunderter(){
        int zaehler=0;
        alleDatums=getAlleDatums();
        do{
            do {
                closeWert = getClose(zaehler);
                schnitt200 = get200(zaehler);
                if (closeWert < schnitt200) {
                    kaufdatum = alleDatums.get(zaehler);
                    break;
                }
                zaehler++;
            } while (closeWert*(1+prozent) > schnitt200);

            System.out.println("KAUFEN: " + kaufdatum + "---" + closeWert + "---" + schnitt200);
            einkaufen();
            System.out.println("anzahl Aktien: " + anzahlaktien+ " Restdepot: "+ depot);

            do {
                zaehler++;
                closeWert = getClose(zaehler);
                schnitt200 = get200(zaehler);
                if (closeWert > schnitt200) {
                    verkaufdatum = alleDatums.get(zaehler);
                    break;
                }
            } while (closeWert*(1-prozent) < schnitt200);
            System.out.println("VERKAUFEN: "+verkaufdatum + "---" + closeWert + "---" + schnitt200);
            verkaufen();
            System.out.println("Depot: "+ depot);
            System.out.println();
        }while(zaehler<=alleDatums.size());
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
                System.out.println("keine Daten in der DB gefunden");
                System.exit(0);
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
                System.out.println("keine Daten in der DB gefunden");
                System.exit(0);
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

}
