package data;

import model.Candle;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipInputStream;

public class NSEBhavcopyFetcher {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy", Locale.ENGLISH);
    private static final DateTimeFormatter inputDateFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Fetches candles for ALL symbols present in the bhavcopy CSV files between startDate and endDate.
     */
    public static Map<String, List<Candle>> fetchBhavcopyData(LocalDate startDate, LocalDate endDate) {
        Map<String, List<Candle>> stockDataMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isTradingDay(current)) {
                LocalDate finalCurrent = current;
                futures.add(executor.submit(() -> fetchForDate(finalCurrent, stockDataMap)));
            }
            current = current.plusDays(1);
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return stockDataMap;
    }

    private static boolean isTradingDay(LocalDate date) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    /**
     * Fetches data for a single date and adds all symbols found to stockDataMap.
     */
    private static void fetchForDate(LocalDate date, Map<String, List<Candle>> stockDataMap) {
        String dateStr = date.format(dateFormatter).toUpperCase();
        String url = String.format("https://archives.nseindia.com/content/historical/EQUITIES/%d/%s/cm%sbhav.csv.zip",
                date.getYear(), date.getMonth().toString().substring(0, 3), dateStr);

        int retries = 3;
        while (retries-- > 0) {
            try (InputStream input = new URL(url).openStream(); ZipInputStream zis = new ZipInputStream(input)) {

                zis.getNextEntry();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                String line;
                boolean headerSkipped = false;

                while ((line = reader.readLine()) != null) {
                    if (!headerSkipped) {
                        headerSkipped = true;
                        continue;
                    }

                    String[] tokens = line.split(",");
                    if (tokens.length < 11)
                        continue;

                    try {
                        String sym = tokens[0].trim();

                        double open = Double.parseDouble(tokens[2]);
                        double high = Double.parseDouble(tokens[3]);
                        double low = Double.parseDouble(tokens[4]);
                        double close = Double.parseDouble(tokens[5]);
                        long volume = Long.parseLong(tokens[8]);
                        LocalDate parsedDate = LocalDate.parse(tokens[10].trim(), inputDateFormatter);
                        String formattedDate = parsedDate.format(outputFormatter);

                        Candle candle = new Candle(formattedDate, open, high, low, close, volume);

                        // Insert candle into the map for this symbol (thread-safe)
                        stockDataMap.computeIfAbsent(sym, k -> Collections.synchronizedList(new ArrayList<>())).add(candle);

                    } catch (Exception ignored) {
                        // Ignore any parse exceptions on data lines
                        ignored.printStackTrace();
                    }
                }

                System.out.println("Fetched data for: " + dateStr);
                return;

            } catch (IOException e) {
                System.err.println("Retrying... Failed to fetch for: " + dateStr);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        System.err.println("Failed after retries: " + dateStr);
    }
}
