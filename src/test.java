import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
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
import java.util.Scanner;


public class test extends Application{
    @Override public void start(Stage stage) {
        stage.setTitle("Aktienkurs");
        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Tag");
        //creating the chart
        final LineChart<String,Number> lineChart =
                new LineChart<String, Number>(xAxis,yAxis);

        lineChart.setTitle("Aktienkurs"+ marke);
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("Close Wert");

        for (String i : aktienPreiseHashmap.keySet()) {
            series.getData().addAll(new XYChart.Data(i, aktienPreiseHashmap.get(i)));
        }

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);

        stage.setScene(scene);
        stage.show();
    }
    static double aktienPreis;
    static List<Double> aktienPreise = new ArrayList<>();
    static HashMap<String, Double> aktienPreiseHashmap = new HashMap<String, Double>();
    static String marke;

    public static void main(String[] args) throws IOException {

        Scanner reader = new Scanner(System.in);
        System.out.println("Von welcher Marke wollen Sie den Aktienkurs der Letzten 30 Tage wissen?[TSLA, AAPL, AMZN, ...]");
        marke = reader.next();
        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="+marke+"&outputsize=full&apikey=WEO2Z2E1M7UWU3QXX";
        System.out.println(URL);
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        JSONObject firstStep = (JSONObject) json.get("Time Series (Daily)");
        for(int i=10;i<41;i++) {

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

        Application.launch(args);


    }
    private static double getWert (JSONObject json, String key) throws JSONException {

        JSONObject jsonO = (JSONObject) json.get(key);
        String Wert = jsonO.getString("4. close");
        return Double.parseDouble(Wert);
    }
}