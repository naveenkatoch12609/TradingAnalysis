package com.TradingAnalysis.main;

import backtest.Backtester;
import data.LiveNSEFetcher;
import data.NSEBhavcopyFetcher;
import data.YahooFinanceFetcher;
import model.Candle;
import model.Summary;
import model.Trade;
import strategy.CompositeStrategy;
import strategy.MovingAverageStrategy;
import strategy.RSIMeanReversionStrategy;
import strategy.Strategy;
import visualization.ApplicationFrame;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

public class NSEMain {
	public static void main(String[] args) {
		LocalDate from = LocalDate.of(2019, 1, 1);
		LocalDate to = LocalDate.of(2024, 2, 1);

		System.out.println("Fetching all symbols from NSE Bhavcopy...");
		Map<String, List<Candle>> bhavcopyData = NSEBhavcopyFetcher.fetchBhavcopyData(from, to);

		if (bhavcopyData.isEmpty()) {
			System.out.println("No data loaded from Bhavcopy.");
			return;
		}

		System.out.println("Merging with Alpha Vantage data to fill gap till today...");
		Map<String, List<Candle>> fullData = DataMerger.mergeBhavcopyWithAlphaVantage(bhavcopyData, to.plusDays(1),
				LocalDate.now());

		
		Map<String, List<Candle>> candleMap = new HashMap<>();
		Map<String, List<Trade>> tradeMap = new HashMap<>();
		Map<String, List<Trade>> executedTradeMap = new HashMap<>();
		Map<String, Summary> summaryMap = new HashMap<>();

		for (Map.Entry<String, List<Candle>> entry : fullData.entrySet()) {
			String symbol = entry.getKey();
			List<Candle> candles = entry.getValue();

			if (candles == null || candles.size() <= 500)
				continue;

			Summary summary = new Summary();
			summary.setOpeningBalance(10000);

			Strategy strategy = new CompositeStrategy(new RSIMeanReversionStrategy(),
					List.of(new MovingAverageStrategy()), List.of(0.4));

			List<Trade> signals = strategy.generateSignals(candles, summary);
			List<Trade> executed = new ArrayList<>();
			double finalBalance = Backtester.run(candles, signals, executed, summary);

			String lastDate = candles.get(candles.size() - 1).date;
			boolean hasTradeOnLastDay = signals.stream().anyMatch(t -> t.date.equals(lastDate));
			if (!hasTradeOnLastDay)
				continue;

			candleMap.put(symbol, candles);
			tradeMap.put(symbol, signals);
			executedTradeMap.put(symbol, executed);
			summaryMap.put(symbol, summary);

			System.out.printf("✔ %s | Final Balance: %.2f | Trades: %d\n", symbol, finalBalance, executed.size());
		}

		if (candleMap.isEmpty()) {
			System.out.println("No symbols passed filtering.");
			return;
		}

		try {
			ApplicationFrame frame = new ApplicationFrame("Trading Analysis", candleMap, executedTradeMap, tradeMap,
					summaryMap);
			frame.setVisible(true);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
