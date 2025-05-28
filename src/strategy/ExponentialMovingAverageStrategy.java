package strategy;

import java.util.ArrayList;
import java.util.List;

import model.Candle;
import model.Summary;
import model.Trade;

public class ExponentialMovingAverageStrategy implements Strategy {
	
    private int shortPeriod = 10;
    private int longPeriod = 50;

    private final String strategyName = "Exponential Moving Average (EMA) Crossover";
    private final String strategyDescription = "Buys when short-term EMA crosses above long-term EMA; sells when short-term EMA crosses below long-term EMA.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Trade> trades = new ArrayList<>();
        List<Double> shortEMA = calculateEMA(candles, shortPeriod);
        List<Double> longEMA = calculateEMA(candles, longPeriod);

        boolean holding = false;

        for (int i = 1; i < candles.size(); i++) {
            Double prevShort = shortEMA.get(i - 1);
            Double prevLong = longEMA.get(i - 1);
            Double currShort = shortEMA.get(i);
            Double currLong = longEMA.get(i);

            if (prevShort == null || prevLong == null || currShort == null || currLong == null) continue;

            if (!holding && prevShort <= prevLong && currShort > currLong) {
                trades.add(new Trade(candles.get(i).date, "BUY", candles.get(i).close));
                holding = true;
            } else if (holding && prevShort >= prevLong && currShort < currLong) {
                trades.add(new Trade(candles.get(i).date, "SELL", candles.get(i).close));
                holding = false;
            }
        }

        return trades;
    }

    private List<Double> calculateEMA(List<Candle> candles, int period) {
        List<Double> ema = new ArrayList<>();
        double alpha = 2.0 / (period + 1.0);

        Double previousEMA = null;

        for (int i = 0; i < candles.size(); i++) {
            double price = candles.get(i).close;

            if (i < period - 1) {
                ema.add(null); // Not enough data yet
                continue;
            } else if (i == period - 1) {
                double sum = 0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += candles.get(j).close;
                }
                previousEMA = sum / period;
                ema.add(previousEMA);
                continue;
            }

            double currentEMA = (price - previousEMA) * alpha + previousEMA;
            ema.add(currentEMA);
            previousEMA = currentEMA;
        }

        return ema;
    }
}
