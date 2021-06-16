import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class BuyAndHold {

    final static String hostname = "localhost";
    final static String dbname = "java";
    final static String user = "java";
    final static String password = "java";

    //Buy and hold
    static double AktiendepotBAH;
    static LocalDate einkaufsdatumBAH=LocalDate.of(2017,01,01), verkaufsdatumBAH;
    static double einkaufswertBAH, verkaufswertBAH;
    static int anzahlAktienBAH;
    static int split;
    //200er

    static double Aktiendepot;
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

    //true setzen wenn programm beenden soll
    static boolean abbrechen=false;

    static Scanner reader = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Buy and HOLD:"+ "\n");
        letztesDatumInDB = getLetztesDBDatum();
        einkaufsdatumBAH=zweihunderterNormal.richtigeLocalDateEingabe("Einkaufsdatum: [2017-01-01]: ");
        String temp;
        do{
            System.out.print("Aktie: [max 5 Zeichen]");
            temp=reader.next();
        }while (aktieOk(temp)==false);
        aktie = temp.toUpperCase();

        AktiendepotBAH=zweihunderterNormal.doubleEingeben("Aktiendepot: [Egal ob int oder double]");
        System.out.println();

        //System.out.println(einkaufsdatumBAH+ " "+aktie+ " "+AktiendepotBAH+"\n");
        buyAndHold();



    }
    static void buyAndHold(){
        buyAndHoldKaufen();
        anzahlAktienBAH = (int) Math.floor(AktiendepotBAH / einkaufswertBAH);
        //System.out.println("Anzahl Aktien:" + anzahlAktienBAH);
        Aktiendepot-=anzahlAktienBAH*einkaufswertBAH;
        System.out.println("Einkaufswert: "+ einkaufswertBAH+ " Aktienanzahl: "+ anzahlAktienBAH+
                " Aktiendepot: "+ AktiendepotBAH);

        buyAndHoldVerkaufen();
        AktiendepotBAH+=anzahlAktienBAH*verkaufswertBAH;
        System.out.println("Verkaufswert: "+ verkaufswertBAH+ " Datum: "+verkaufsdatumBAH+ " Aktiendepot: "+ AktiendepotBAH + "\n");
        System.out.println("Buy and hold ende");
    }
    //Buy and Hold
    static void buyAndHoldKaufen(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" where Datum>= '"+ einkaufsdatumBAH +"' limit 1;";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                einkaufsdatumBAH=LocalDate.parse(reSe.getString("Datum"));
                einkaufswertBAH=Double.parseDouble(reSe.getString("Wert"));
            }
            else {
                System.out.println("keine Werte mehr in DB vorhanden");
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
    static void buyAndHoldVerkaufen(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" order by Datum desc limit 1;";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                verkaufsdatumBAH=LocalDate.parse(reSe.getString("Datum"));
                verkaufswertBAH=Double.parseDouble(reSe.getString("Wert"));
            }
            else {
                System.out.println("keine Daten mehr in DB vorhanden");
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
    //hyperaktiv ver und kaffn
    static void getDatumUndClosewertEinkauf(int daysToAdd){
        Connection conn = null;
        LocalDate temp = null;
        try {

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" where Datum >= '"+ verkaufsdatum.plusDays(daysToAdd)+"' order by Datum limit 1;";
            //Select count(Wert) from tsla where Datum < '2021-04-21';
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                String temp0 = reSe.getString("Wert");
                einkaufswert =Double.parseDouble(temp0);
                String temp2=reSe.getString("Datum");
                einkaufsdatum =LocalDate.parse(temp2);
            }
            else {
                abbrechen=true;
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
    static void getDatumUndClosewertVerkauf(int daysToAdd){
        Connection conn = null;
        try {

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +" where Datum >= '"+ einkaufsdatum.plusDays(daysToAdd)+"' order by Datum limit 1;";
            //Select count(Wert) from tsla where Datum < '2021-04-21';
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                String temp0 = reSe.getString("Wert");
                verkaufswert =Double.parseDouble(temp0);
                String temp2=reSe.getString("Datum");
                verkaufsdatum =LocalDate.parse(temp2);
            }
            else {
                abbrechen=true;
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
            else{
                abbrechen=true;
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
    static Double get200erwertVerkauf(){
        Connection conn = null;
        double Wert200=0.0;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select * from "+aktie +"_200schnitt where Datum = '"+verkaufsdatum+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                Wert200=reSe.getDouble("Wert");
            }
            else {
                abbrechen=true;
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
        Aktiendepot=Aktiendepot-(AnzahlAktien*einkaufswert);
    }
    //schauen ob datum vor heute ist

    static void splitBAH(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+dbname+"?user="+user+"&password="+password+"&serverTimezone=UTC");
            Statement myStat = conn.createStatement();
            String sql = "Select Split from "+aktie +" where Datum >= '"+einkaufsdatumBAH+"';";
            ResultSet reSe=myStat.executeQuery(sql);
            if (reSe.next()) {
                split*=reSe.getDouble("Split");
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

    static public boolean aktieOk(String aktie){
        if(aktie.length()>5){
            return false;
        }
        return true;
    }




}
