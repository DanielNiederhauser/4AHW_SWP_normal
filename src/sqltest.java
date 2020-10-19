import java.sql.*;
import java.sql.ResultSet;
public class sqltest {
    public static void main(String[] args) {

        // Diese Eintraege werden zum
        // Verbindungsaufbau benoetigt.
        final String hostname = "localhost";
        final String port = "3306";
        final String dbname = "java";
        final String user = "java";
        final String password = "java";

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
            //myStat.execute("INSERT INTO Kalender values (2)");
            ResultSet reSe=myStat.executeQuery("Select * from kalender");
            System.out.println("Montag      Dienstag        Mittwoch        Donnerstag      Freitag     Samstag     Sonntag");
            while(reSe.next()){
                String Montag = reSe.getString("Montag");
                String Dienstag = reSe.getString("Dienstag");
                String Mittwoch = reSe.getString("Mittwoch");
                String Donnerstag = reSe.getString("Donnerstag");
                String Freitag = reSe.getString("Freitag");
                String Samstag = reSe.getString("Samstag");
                String Sonntag = reSe.getString("Sonntag");

                System.out.printf("%1s", Montag);
                System.out.printf("%12s", Dienstag);
                System.out.printf("%16s", Mittwoch);
                System.out.printf("%16s", Donnerstag);
                System.out.printf("%16s", Freitag);
                System.out.printf("%14s", Samstag);
                System.out.printf("%14s", Sonntag);
                System.out.println();
            }

            //resultset für auslesen
            System.out.println("* Datenbank-Verbindung beenden");
            conn.close();
        }
        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }

    } // ende: public static void main()
}
