package strategy;

import model.Candle;
import model.Trade;
import model.Summary;

import java.util.ArrayList;
import java.util.List;

public class ADXStrategy implements Strategy {

    private final int period = 14;
    private final double threshold = 25.0; // ADX value above 25 indicates a strong trend

    private final String strategyName = "ADX Trend Strength Strategy";
    private final String strategyDescription = "Buys when ADX rises above 25, indicating a strong trend. "
            + "Sells when ADX falls below 25, indicating a weak or no trend.";

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName(strategyName);
        summary.setStrategyDescription(strategyDescription);

        List<Double> adxValues = calculateADX(candles, period);
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;

        for (int i = period; i < adxValues.size(); i++) {
            Double prevADX = adxValues.get(i - 1);
            Double currADX = adxValues.get(i);

            if (prevADX == null || currADX == null) continue;

            // Buy when ADX crosses above threshold (strong trend)
            if (!holding && prevADX < threshold && currADX >= threshold) {
                trades.add(new Trade(candles.get(i).date, "BUY", candles.get(i).close));
                holding = true;
            }
            // Sell when ADX crosses below threshold (weak trend)
            else if (holding && prevADX >= threshold && currADX < threshold) {
                trades.add(new Trade(candles.get(i).date, "SELL", candles.get(i).close));
                holding = false;
            }
        }

        return trades;
    }

    private List<Double> calculateADX(List<Candle> candles, int period) {
        List<Double> adx = new ArrayList<>();
        List<Double> plusDI = new ArrayList<>();
        List<Double> minusDI = new ArrayList<>();

        // Calculate Plus and Minus Directional Movements (DM) and True Range (TR)
        for (int i = 1; i < candles.size(); i++) {
            double currentHigh = candles.get(i).high;
            double previousHigh = candles.get(i - 1).high;
            double currentLow = candles.get(i).low;
            double previousLow = candles.get(i - 1).low;
            double previousClose = candles.get(i - 1).close;

            double plusDM = (currentHigh - previousHigh > previousLow - currentLow) ? Math.max(currentHigh - previousHigh, 0) : 0;
            double minusDM = (previousLow - currentLow > currentHigh - previousHigh) ? Math.max(previousLow - currentLow, 0) : 0;

            double tr = Math.max(currentHigh - currentLow, Math.max(Math.abs(currentHigh - previousClose), Math.abs(currentLow - previousClose)));

            plusDI.add(plusDM);
            minusDI.add(minusDM);
        }

        // Calculate the ADX, plusDI, and minusDI averages
        for (int i = period; i < candles.size(); i++) {
            double sumPlusDI = 0;
            double sumMinusDI = 0;
            double sumTR = 0;

            // Calculate the sum of the last `period` values
            for (int j = i - period; j < i; j++) {
                sumPlusDI += plusDI.get(j);
                sumMinusDI += minusDI.get(j);
                sumTR += Math.max(candles.get(j).high - candles.get(j).low,
                        Math.max(Math.abs(candles.get(j).high - candles.get(j - 1).close),
                                 Math.abs(candles.get(j).low - candles.get(j - 1).close)));
            }

            double plusDIPercent = (sumPlusDI / sumTR) * 100;
            double minusDIPercent = (sumMinusDI / sumTR) * 100;

            double adxValue = Math.abs(plusDIPercent - minusDIPercent);
            adx.add(adxValue);
        }

        return adx;
    }
}
