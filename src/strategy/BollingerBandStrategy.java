package strategy;

import model.Candle;
import model.Trade;
import model.Summary;

import java.util.ArrayList;
import java.util.List;

public class BollingerBandStrategy implements Strategy {

    private final int period = 20;
    private final double multiplier = 2.0;

    private final String strategyName = "Bollinger Band Reversion";
    private final String strategyDescription = "Buys when price closes below lower Bollinger Band (oversold); "
            + "sells when price closes above upper Bollinger Band (overbought). "
            + "Assumes price will revert to the mean.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Trade> trades = new ArrayList<>();
        boolean holding = false;

        for (int i = period; i < candles.size(); i++) {
            List<Candle> window = candles.subList(i - period, i);
            double mean = window.stream().mapToDouble(c -> c.close).average().orElse(0);
            double stddev = Math.sqrt(window.stream().mapToDouble(c -> Math.pow(c.close - mean, 2)).sum() / period);

            double upper = mean + multiplier * stddev;
            double lower = mean - multiplier * stddev;

            Candle candle = candles.get(i);

            if (!holding && candle.close < lower) {
                trades.add(new Trade(candle.date, "BUY", candle.close));
                holding = true;
            } else if (holding && candle.close > upper) {
                trades.add(new Trade(candle.date, "SELL", candle.close));
                holding = false;
            }
        }

        return trades;
    }
}
