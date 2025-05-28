package data;

import model.Candle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class AlphaVantageFetcher {

    private static final String API_KEY = "XP2FRTJI7XH17MAL"; // <-- Put your key here
    private static final String API_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=%s&outputsize=full&apikey=%s";

    /**
     * Fetches daily OHLC data from Alpha Vantage for symbol from startDate to today.
     * Dates format: LocalDate
     */
    public static List<Candle> fetchDailyOHLC(String symbol, LocalDate startDate, LocalDate endDate) {
        List<Candle> candles = new ArrayList<>();

        try {
            String urlStr = String.format(API_URL, symbol, API_KEY);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonSB = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonSB.append(line);
            }
            reader.close();

            JSONObject root = new JSONObject(jsonSB.toString());
            JSONObject timeSeries = root.optJSONObject("Time Series (Daily)");
            if (timeSeries == null) {
                System.err.println("No daily data found for symbol: " + symbol);
                return candles;
            }

            for (String dateStr : timeSeries.keySet()) {
                LocalDate date = LocalDate.parse(dateStr);
                if ((date.isBefore(startDate)) || (date.isAfter(endDate))) {
                    continue;  // skip dates outside requested range
                }

                JSONObject dayData = timeSeries.getJSONObject(dateStr);
                double open = dayData.getDouble("1. open");
                double high = dayData.getDouble("2. high");
                double low = dayData.getDouble("3. low");
                double close = dayData.getDouble("4. close");
                long volume = dayData.getLong("6. volume"); // Use adjusted volume

                candles.add(new Candle(dateStr, open, high, low, close, volume));
            }

            // Sort candles by date ascending
            candles.sort(Comparator.comparing(c -> LocalDate.parse(c.date)));

        } catch (Exception e) {
            System.err.println("Error fetching Alpha Vantage data for " + symbol + ": " + e.getMessage());
        }

        return candles;
    }
}
