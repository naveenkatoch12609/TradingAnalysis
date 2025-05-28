package strategy;

import java.util.ArrayList;
import java.util.List;

import model.Candle;
import model.Summary;
import model.Trade;

public class MovingAverageStrategy implements Strategy {
	private int shortPeriod = 10;
    private int longPeriod = 50;
    
	String strategyName = "Moving Average Crossover";
	String strategyDescription = "Buys when short MA crosses above long MA; sells when it crosses below.";

    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
    	summary.setStrategyName(strategyName);
    	summary.setStrategyDescription(strategyDescription);
    	
        List<Trade> trades = new ArrayList<>();
        for (int i = longPeriod; i < candles.size(); i++) {
            double shortMA = average(candles.subList(i - shortPeriod, i));
            double longMA = average(candles.subList(i - longPeriod, i));

            if (shortMA > longMA) {
                trades.add(new Trade(candles.get(i).date, "BUY", candles.get(i).close));
            } else if (shortMA < longMA) {
                trades.add(new Trade(candles.get(i).date, "SELL", candles.get(i).close));
            }
        }
        return trades;
    }

    private double average(List<Candle> subset) {
        return subset.stream().mapToDouble(c -> c.close).average().orElse(0);
    }
}
