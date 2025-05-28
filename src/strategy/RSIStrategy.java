package strategy;

import model.Candle;
import model.Trade;
import model.Summary;

import java.util.ArrayList;
import java.util.List;

public class RSIStrategy implements Strategy {

    private final int period = 14;
    private final double oversold = 30.0;
    private final double overbought = 70.0;

    private final String strategyName = "RSI Momentum Strategy";
    private final String strategyDescription = "Buys when RSI crosses above 30 (oversold), sells when it crosses below 70 (overbought). "
            + "This strategy aims to capture mean-reversion and short-term momentum.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Double> rsiValues = calculateRSI(candles, period);
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;

        for (int i = 1; i < rsiValues.size(); i++) {
            Double prevRSI = rsiValues.get(i - 1);
            Double currRSI = rsiValues.get(i);

            if (prevRSI == null || currRSI == null) continue;

            if (!holding && prevRSI <= oversold && currRSI > oversold) {
                trades.add(new Trade(candles.get(i).date, "BUY", candles.get(i).close));
                holding = true;
            } else if (holding && prevRSI >= overbought && currRSI < overbought) {
                trades.add(new Trade(candles.get(i).date, "SELL", candles.get(i).close));
                holding = false;
            }
        }

        return trades;
    }

    private List<Double> calculateRSI(List<Candle> candles, int period) {
        List<Double> rsi = new ArrayList<>();
        double gain = 0;
        double loss = 0;

        for (int i = 0; i < candles.size(); i++) {
            if (i == 0) {
                rsi.add(null);
                continue;
            }

            double change = candles.get(i).close - candles.get(i - 1).close;
            double currentGain = Math.max(change, 0);
            double currentLoss = Math.max(-change, 0);

            if (i < period) {
                gain += currentGain;
                loss += currentLoss;
                rsi.add(null);
            } else if (i == period) {
                gain += currentGain;
                loss += currentLoss;
                double avgGain = gain / period;
                double avgLoss = loss / period;
                double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
                rsi.add(100 - (100 / (1 + rs)));
            } else {
                double prevRSI = rsi.get(i - 1);
                gain = (gain * (period - 1) + currentGain) / period;
                loss = (loss * (period - 1) + currentLoss) / period;
                double rs = loss == 0 ? 100 : gain / loss;
                rsi.add(100 - (100 / (1 + rs)));
            }
        }

        return rsi;
    }
}
