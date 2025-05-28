package strategy;

import model.Candle;
import model.Trade;
import model.Summary;

import java.util.ArrayList;
import java.util.List;

public class MACDStrategy implements Strategy {

    private final int fastPeriod = 12;
    private final int slowPeriod = 26;
    private final int signalPeriod = 9;

    private final String strategyName = "MACD Crossover Strategy";
    private final String strategyDescription = "Uses the MACD line (12 EMA - 26 EMA) and signal line (9 EMA of MACD). "
            + "Buy when MACD crosses above signal; sell when MACD crosses below signal.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Double> closes = candles.stream().map(c -> c.close).toList();
        List<Double> emaFast = calculateEMA(closes, fastPeriod);
        List<Double> emaSlow = calculateEMA(closes, slowPeriod);

        List<Double> macdLine = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            if (emaFast.get(i) == null || emaSlow.get(i) == null) {
                macdLine.add(null);
            } else {
                macdLine.add(emaFast.get(i) - emaSlow.get(i));
            }
        }

        List<Double> signalLine = calculateEMA(macdLine, signalPeriod);
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;

        for (int i = 1; i < candles.size(); i++) {
            Double prevMACD = macdLine.get(i - 1);
            Double currMACD = macdLine.get(i);
            Double prevSignal = signalLine.get(i - 1);
            Double currSignal = signalLine.get(i);

            if (prevMACD == null || currMACD == null || prevSignal == null || currSignal == null)
                continue;

            if (!holding && prevMACD <= prevSignal && currMACD > currSignal) {
                trades.add(new Trade(candles.get(i).date, "BUY", candles.get(i).close));
                holding = true;
            } else if (holding && prevMACD >= prevSignal && currMACD < currSignal) {
                trades.add(new Trade(candles.get(i).date, "SELL", candles.get(i).close));
                holding = false;
            }
        }

        return trades;
    }

    private List<Double> calculateEMA(List<Double> values, int period) {
        List<Double> ema = new ArrayList<>();
        double alpha = 2.0 / (period + 1.0);
        Double prevEMA = null;

        for (int i = 0; i < values.size(); i++) {
            Double price = values.get(i);
            if (price == null) {
                ema.add(null);
                continue;
            }

            if (prevEMA == null) {
                ema.add(null);
                if (i >= period - 1) {
                    double sum = 0;
                    int count = 0;
                    for (int j = i - period + 1; j <= i; j++) {
                        if (values.get(j) != null) {
                            sum += values.get(j);
                            count++;
                        }
                    }
                    if (count == period) {
                        prevEMA = sum / period;
                        ema.set(i, prevEMA);
                    }
                }
            } else {
                prevEMA = (price - prevEMA) * alpha + prevEMA;
                ema.add(prevEMA);
            }
        }

        return ema;
    }
}

