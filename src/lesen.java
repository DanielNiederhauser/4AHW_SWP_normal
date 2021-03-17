import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class lesen {
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

    public static void main(String[] args) {
        String dateiName = "src/AktienListe.txt";
        List<String> asdf = ladeDatei(dateiName);
        System.out.println(asdf);
    }
}
