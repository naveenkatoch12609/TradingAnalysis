package data;

import model.Candle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NSEDataFetcher {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static Map<String, List<Candle>> fetchStockData(List<String> stockSymbols,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        Map<String, List<Candle>> result = new HashMap<>();

        for (String symbol : stockSymbols) {
            System.out.println("\nFetching data for: " + symbol);
            List<Candle> fullData = new ArrayList<>();
            LocalDate currentStart = startDate;

            while (!currentStart.isAfter(endDate)) {
                LocalDate currentEnd = currentStart.plusYears(1).minusDays(1);
                if (currentEnd.isAfter(endDate)) {
                    currentEnd = endDate;
                }

                try {
                    // Step 1: Get fresh cookies for each chunk (or per symbol)
                    Connection.Response homepage = Jsoup.connect("https://www.nseindia.com")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                    + "(KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                            .timeout(10000)
                            .method(Connection.Method.GET)
                            .execute();

                    Map<String, String> cookies = homepage.cookies();

                    // Step 2: Encode URL
                    String seriesParam = URLEncoder.encode("[\"EQ\"]", StandardCharsets.UTF_8);
                    String url = String.format(
                            "https://www.nseindia.com/api/historical/cm/equity?symbol=%s&series=%s&from=%s&to=%s",
                            symbol,
                            seriesParam,
                            formatter.format(currentStart),
                            formatter.format(currentEnd)
                    );

                    // Step 3: Fetch data
                    Connection.Response response = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                    + "(KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                            .referrer("https://www.nseindia.com/get-quotes/equity?symbol=" + symbol)
                            .cookies(cookies)
                            .ignoreContentType(true)
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .timeout(15000)
                            .method(Connection.Method.GET)
                            .execute();

                    String jsonStr = response.body();
                    JSONObject json = new JSONObject(jsonStr);

                    if (!json.has("data")) {
                        System.out.printf("No data for %s from %s to %s\n", symbol,
                                formatter.format(currentStart), formatter.format(currentEnd));
                        currentStart = currentEnd.plusDays(1);
                        continue;
                    }
                    Thread.sleep(2000);
                    JSONArray data = json.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        Candle candle = new Candle(
                                obj.getString("CH_TIMESTAMP"),
                                obj.optDouble("CH_OPENING_PRICE", 0.0),
                                obj.optDouble("CH_TRADE_HIGH_PRICE", 0.0),
                                obj.optDouble("CH_TRADE_LOW_PRICE", 0.0),
                                obj.optDouble("CH_CLOSING_PRICE", 0.0),
                                obj.optLong("CH_TOT_TRADED_QTY", 0)
                        );
                        fullData.add(candle);
                    }

                    System.out.printf("✓ %d candles for %s from %s to %s\n",
                            data.length(), symbol, formatter.format(currentStart), formatter.format(currentEnd));

                    Thread.sleep(700); // be polite

                } catch (Exception e) {
                    System.err.printf("✗ Error fetching %s from %s to %s: %s\n", symbol,
                            formatter.format(currentStart), formatter.format(currentEnd), e.getMessage());

                    // If 401 error, break the loop early and move to next symbol
                    if (e.getMessage().contains("Status=401")) {
                        System.err.println("Skipping " + symbol + " due to access restriction.");
                        break;
                    }
                }

                currentStart = currentEnd.plusDays(1); // next chunk
            }

            fullData.sort(Comparator.comparing(c -> c.date));
            result.put(symbol, fullData);
        }

        return result;
    }
}
