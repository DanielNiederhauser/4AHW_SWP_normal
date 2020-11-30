import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class test1 {
    static String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&outputsize=full&apikey=WEO2Z2E1M7UWU3QXX";
    static List<String> alleDynamischen = new ArrayList<>();
    static double aktienPreis;
    static List<Double> aktienPreise = new ArrayList<>();
    static HashMap<String, Double> aktienPreiseHashmap = new HashMap<String, Double>();


    public static void main(String[] args) throws IOException {
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        JSONObject firstStep = (JSONObject) json.get("Time Series (Daily)");
        for(int i=10;i<31;i++) {

            String temp = LocalDate.now().minusDays(i).toString();
            System.out.println(temp);
            if((LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.MONDAY)) ||(LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.TUESDAY))||
                    (LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.WEDNESDAY))||(LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.THURSDAY))||
                    (LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.FRIDAY))){

                aktienPreis = getWert(firstStep, temp);
                aktienPreise.add(aktienPreis);
                aktienPreiseHashmap.put(temp, aktienPreis);
            }

        }
        System.out.println("jetzt");
        for (int i=0;i<aktienPreise.size();i++){
            System.out.println(aktienPreise.get(i));
        }
        System.out.println("Hashmap");
        System.out.println(aktienPreiseHashmap);



    }
        private static double getWert (JSONObject json, String key) throws JSONException {

            JSONObject jsonO = (JSONObject) json.get(key);
            String Wert = jsonO.getString("4. close");
            return Double.parseDouble(Wert);
        }
    }

