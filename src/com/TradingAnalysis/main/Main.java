package com.TradingAnalysis.main;

import data.HistoricalDataLoader;
import model.Candle;
import model.Summary;
import model.Trade;
import strategy.CompositeStrategy;
import strategy.MovingAverageStrategy;
import strategy.RSIMeanReversionStrategy;
import strategy.Strategy;
import visualization.ApplicationFrame;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backtest.Backtester;

public class Main {
    public static void main(String[] args) {

        String filePath = "data/"; // CSV path

        // Load all stock data from CSV files
        Map<String, List<Candle>> stockDataMap = HistoricalDataLoader.loadAllStocks(filePath);
        if (stockDataMap.isEmpty()) {
            System.out.println("No data loaded.");
            return;
        }

        // Maps to hold analysis results for each stock
        Map<String, List<Trade>> executedTradesMap = new HashMap<>();
        Map<String, List<Trade>> signalsMap = new HashMap<>();
        Map<String, Summary> summaryMap = new HashMap<>();

        // Initial opening balance
        double openingBalance = 10000;

        // Process each stock
        for (Map.Entry<String, List<Candle>> entry : stockDataMap.entrySet()) {
            String stock = entry.getKey();
            List<Candle> candles = entry.getValue();

            Summary summary = new Summary();
            summary.setOpeningBalance(openingBalance);

            // Setup composite strategy
            List<Strategy> otherStrategies = List.of(new MovingAverageStrategy());
            List<Double> weights = List.of(0.4);

            Strategy strategy = new CompositeStrategy(new RSIMeanReversionStrategy(), otherStrategies, weights);

            // Generate signals (trade ideas)
            List<Trade> signals = strategy.generateSignals(candles, summary);

            // Backtest signals
            List<Trade> executedTrades = new ArrayList<>();
            double finalBalance = Backtester.run(candles, signals, executedTrades, summary);

            System.out.println("Generated Trades for " + stock + ":");
            signals.forEach(System.out::println);
            System.out.printf("Final balance for %s after backtest: $%.2f\n", stock, finalBalance);

            // Store in maps
            executedTradesMap.put(stock, executedTrades);
            signalsMap.put(stock, signals);
            summaryMap.put(stock, summary);
        }

        // Create one ApplicationFrame with all data maps
        try {
            ApplicationFrame frame = new ApplicationFrame("Trading Analysis", stockDataMap, executedTradesMap, signalsMap, summaryMap);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
