import java.sql.*;
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
            myStat.execute("INSERT INTO Kalender (Montag, Dienstag, Mittwoch) VALUES (value1, value2, value3)");
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
