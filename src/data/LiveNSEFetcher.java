package data;

import model.Candle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LiveNSEFetcher {

    private static final String NSE_BASE_URL = "https://www.nseindia.com";
    private static final String NSE_QUOTE_URL = "https://www.nseindia.com/api/quote-equity?symbol=%s";

    private static CookieManager cookieManager = new CookieManager();

    static {
        CookieHandler.setDefault(cookieManager);
    }

    // Call this once to initialize session & cookies by hitting homepage
    private static void initializeSession() throws Exception {
        URL url = new URL(NSE_BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Failed to connect to NSE homepage, response code: " + code);
        }
        conn.disconnect();
    }

    public static Map<String, Candle> fetchLiveData(List<String> symbols) {
        Map<String, Candle> liveCandles = new HashMap<>();

        try {
            initializeSession();
        } catch (Exception e) {
            System.err.println("Failed to initialize NSE session: " + e.getMessage());
            return liveCandles;
        }

        for (String symbol : symbols) {
            try {
                URL url = new URL(String.format(NSE_QUOTE_URL, symbol));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Accept", "application/json, text/plain, */*");
                conn.setRequestProperty("Referer", NSE_BASE_URL + "/get-quotes/equity?symbol=" + symbol);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Failed to fetch " + symbol + ": HTTP " + responseCode);
                    continue;
                }

                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String jsonText = in.lines().collect(Collectors.joining());

                    JSONObject root = new JSONObject(jsonText);
                    JSONObject priceInfo = root.getJSONObject("priceInfo");
                    JSONObject highLow = priceInfo.getJSONObject("intraDayHighLow");

                    double open = priceInfo.optDouble("open", 0);
                    double close = priceInfo.optDouble("close", 0);
                    double high = highLow.optDouble("max", 0);
                    double low = highLow.optDouble("min", 0);

                    long volume = 0;
                    if (root.has("preOpenMarket")) {
                        volume = root.getJSONObject("preOpenMarket").optLong("totalTradedVolume", 0);
                    }

                    String dateRaw = root.getJSONObject("metadata").optString("lastUpdateTime", "");
                    // Date format example: "16-May-2025 16:00:00"
                    String date = dateRaw.split(" ")[0]; // "16-May-2025"

                    Candle candle = new Candle(date, open, high, low, close, volume);
                    liveCandles.put(symbol, candle);

                    System.out.printf("Fetched %s: O=%.2f H=%.2f L=%.2f C=%.2f Vol=%d Date=%s\n",
                            symbol, open, high, low, close, volume, date);
                }

                // Respectful delay between requests to avoid rate limiting
                Thread.sleep(200);

            } catch (Exception e) {
                System.err.println("Error fetching live data for " + symbol + ": " + e.getMessage());
            }
        }

        return liveCandles;
    }
}
