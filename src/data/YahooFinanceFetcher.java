package data;

import model.Candle;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class YahooFinanceFetcher {

    public static Map<String, List<Candle>> fetchDailyData(List<String> symbols, LocalDate from, LocalDate to) {
        Map<String, List<Candle>> result = new HashMap<>();

        for (String symbol : symbols) {
            try {
                long period1 = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
                long period2 = to.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

                String downloadUrl = String.format(
                        "https://query1.finance.yahoo.com/v7/finance/download/%s.NS?period1=%d&period2=%d&interval=1d&events=history",
                        symbol, period1, period2
                );

                // Step 1: Fetch Yahoo page to get cookies
                Connection.Response res = Jsoup.connect("https://finance.yahoo.com/quote/" + symbol + ".NS")
                        .method(Connection.Method.GET)
                        .userAgent("Mozilla/5.0")
                        .execute();

                Map<String, String> cookies = res.cookies();

                // Step 2: Download CSV using cookies
                String csvData = Jsoup.connect(downloadUrl)
                        .cookies(cookies)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0")
                        .execute()
                        .body()
                        .trim();

                // Check if we got a valid CSV response
                if (!csvData.contains("Date,Open,High,Low,Close")) {
                    System.err.println("Invalid or missing CSV for symbol: " + symbol);
                    continue;
                }

                List<String> lines = Arrays.asList(csvData.split("\n"));
                List<Candle> candles = new ArrayList<>();

                for (int i = 1; i < lines.size(); i++) {
                    String[] tokens = lines.get(i).split(",");
                    if (tokens.length < 6 || tokens[1].equals("null")) continue;

                    try {
                        String date = tokens[0];
                        double open = Double.parseDouble(tokens[1]);
                        double high = Double.parseDouble(tokens[2]);
                        double low = Double.parseDouble(tokens[3]);
                        double close = Double.parseDouble(tokens[4]);
                        long volume = Long.parseLong(tokens[6]);

                        candles.add(new Candle(date, open, high, low, close, volume));
                    } catch (Exception e) {
                        System.err.println("Skipping malformed line for symbol " + symbol + ": " + lines.get(i));
                    }
                }

                if (!candles.isEmpty()) {
                    result.put(symbol, candles);
                    System.out.println("Fetched " + candles.size() + " candles for " + symbol);
                } else {
                    System.err.println("No valid candles found for symbol: " + symbol);
                }

            } catch (Exception e) {
                System.err.println("Failed to fetch for " + symbol + ": " + e.getMessage());
            }
        }

        return result;
    }
}
