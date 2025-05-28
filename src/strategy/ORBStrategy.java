package strategy;

import model.Candle;
import model.Trade;
import model.Summary;

import java.util.ArrayList;
import java.util.List;

public class ORBStrategy implements Strategy {

    private final int rangeMinutes = 15; // Opening range is the first 15 minutes
    private final String strategyName = "Opening Range Breakout Strategy";
    private final String strategyDescription = "Buys when price breaks above the opening range high; "
            + "sells when price breaks below the opening range low.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Trade> trades = new ArrayList<>();
        boolean holding = false;

        // Define the opening range based on the first `rangeMinutes` candles
        int rangeEndIndex = rangeMinutes; // Assuming each candle is 1 minute
        if (candles.size() < rangeEndIndex) {
            return trades; // Not enough data to define opening range
        }

        double openingRangeHigh = Double.MIN_VALUE;
        double openingRangeLow = Double.MAX_VALUE;

        // Determine the high and low of the opening range
        for (int i = 0; i < rangeEndIndex; i++) {
            Candle candle = candles.get(i);
            openingRangeHigh = Math.max(openingRangeHigh, candle.high);
            openingRangeLow = Math.min(openingRangeLow, candle.low);
        }

        // Loop through the rest of the candles to find breakout signals
        for (int i = rangeEndIndex; i < candles.size(); i++) {
            Candle currentCandle = candles.get(i);
            double currentPrice = currentCandle.close;

            // Buy signal: Price breaks above the opening range high
            if (!holding && currentPrice > openingRangeHigh) {
                trades.add(new Trade(currentCandle.date, "BUY", currentPrice));
                holding = true;
            }
            // Sell signal: Price breaks below the opening range low
            else if (holding && currentPrice < openingRangeLow) {
                trades.add(new Trade(currentCandle.date, "SELL", currentPrice));
                holding = false;
            }
        }

        return trades;
    }
}
