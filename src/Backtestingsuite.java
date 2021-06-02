import java.sql.*;
import java.time.LocalDate;

public class Backtestingsuite {

    final static String hostname = "localhost";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

    static LocalDate Kaufzeitpunkt =LocalDate.of(2017,01,01);
    static double Aktiendepot=100000;
    static String aktie="TSLA";
    static Double einkaufswert = null;
    static LocalDate einkaufsdatum =null;
    static int AnzahlAktien;
    static double Wert200;
    static LocalDate letztesDatumInDB;

    static Double verkaufswert = null;
    static LocalDate verkaufsdatum = null;
    static Boolean einkauf = null;
    static int einkaufszaehler=0, verkaufszaehler=0;

    public static void main(String[] args) {
        letztesDatumInDB = getLetztesDBDatum();

        einkauf = true;
        getWertUndDatumGroesserOderKleiner200erSchnitt();
        einkaufen();
        System.out.println("Erstes Datum und Wert: " + einkaufswert + " " + einkaufsdatum);
        System.out.println("Anzahl Aktien: " + AnzahlAktien);


        System.out.println("Einkauf: " + einkaufswert + "---" + einkaufsdatum + "---" + get200erwertEinkauf());
        einkauf = false;
        getWertUndDatumGroesserOderKleiner200erSchnitt();
        System.out.println("Verkauf: " + einkaufswert + "---" + einkaufsdatum + "---" + get200erwertEinkauf());
        Aktiendepot=AnzahlAktien*einkaufswert;
        System.out.println("Depotwert: "+Aktiendepot);

    }
    static void getDatumUndClosewertEinkauf(int daysToAdd){
        Connection conn = null;
        LocalDate temp = null;
        try {
            if(einkauf == true){
                temp=Kaufzeitpunkt;
            }
            else if(einkauf == false){
                temp = einkaufsdatum;
            }
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" where Datum >= '"+ temp.plusDays(daysToAdd)+"' order by Datum limit 1;";
            //Select count(Wert) from tsla where Datum < '2021-04-21';
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                String temp0 = reSe.getString("Wert");
                einkaufswert =Double.parseDouble(temp0);
                String temp2=reSe.getString("Datum");
                einkaufsdatum =LocalDate.parse(temp2);
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
    static Double get200erwertEinkauf(){
        Connection conn = null;
        double Wert200=0.0;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +"_200schnitt where Datum = '"+einkaufsdatum+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                Wert200=reSe.getDouble("Wert");
            }
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
        return Wert200;
    }
    static void getWertUndDatumGroesserOderKleiner200erSchnitt(){

        if(einkauf==true){
            int zaehler=0;
            do{
                getDatumUndClosewertEinkauf(zaehler);
                zaehler++;
                Wert200=get200erwertEinkauf();
            }while(einkaufswert <= Wert200);
            System.out.println(zaehler);
        }
        else if(einkauf == false){
            int zaehler=0;

            do{
                getDatumUndClosewertEinkauf(zaehler);
                zaehler++;
                Wert200=get200erwertEinkauf();
            }while(einkaufswert >= Wert200);
            System.out.println(zaehler);
        }

    }
    static LocalDate getLetztesDBDatum(){
        LocalDate ld = null;
        Connection conn = null;
        double Wert200=0.0;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select Datum from "+aktie +"_200schnitt order by Datum desc limit 1;";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                ld=LocalDate.parse(reSe.getString("Datum"));
            }
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

        return ld;
    }

    static void einkaufen() {
        AnzahlAktien = (int) Math.floor(Aktiendepot / einkaufswert);
    }
}
