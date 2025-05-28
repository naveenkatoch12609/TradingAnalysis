package com.TradingAnalysis.main;

import java.time.LocalDate;
import java.util.*;

import data.AlphaVantageFetcher;
import model.Candle;

public class DataMerger {

    public static Map<String, List<Candle>> mergeBhavcopyWithAlphaVantage(Map<String, List<Candle>> bhavcopyData, LocalDate gapStart, LocalDate gapEnd) {
        Map<String, List<Candle>> mergedData = new HashMap<>();

        for (String symbol : bhavcopyData.keySet()) {
            List<Candle> bhavCandles = bhavcopyData.get(symbol);
            LocalDate lastDate = bhavCandles.stream()
                    .map(c -> LocalDate.parse(c.date))
                    .max(LocalDate::compareTo)
                    .orElse(gapStart.minusDays(1)); // fallback

            // Fetch Alpha Vantage data from day after last Bhavcopy candle till gapEnd
            List<Candle> alphaCandles = AlphaVantageFetcher.fetchDailyOHLC(symbol, lastDate.plusDays(1), gapEnd);

            // Merge lists
            List<Candle> combined = new ArrayList<>(bhavCandles);

            // Avoid duplicates by date (AlphaVantage may include overlapping dates)
            Set<String> existingDates = new HashSet<>();
            for (Candle c : combined) existingDates.add(c.date);

            for (Candle c : alphaCandles) {
                if (!existingDates.contains(c.date)) {
                    combined.add(c);
                }
            }

            // Sort combined candles by date ascending
            combined.sort(Comparator.comparing(c -> LocalDate.parse(c.date)));

            mergedData.put(symbol, combined);
        }

        return mergedData;
    }
}
