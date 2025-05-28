package strategy;

import model.Candle;
import model.Summary;
import model.Trade;

import java.util.*;

public class CompositeStrategy implements Strategy {
    private final Strategy primaryStrategy;
    private final List<Strategy> otherStrategies;
    private final List<Double> weights;

    public CompositeStrategy(Strategy primaryStrategy, List<Strategy> otherStrategies, List<Double> weights) {
        this.primaryStrategy = primaryStrategy;
        this.otherStrategies = otherStrategies;
        this.weights = weights;
    }

    @Override
    public List<Trade> generateSignals(List<Candle> candles, Summary summary) {
        summary.setStrategyName("Composite Strategy with Weighted Influence");
        summary.setStrategyDescription("Primary strategy generates signals, others adjust based on weight.");

        // Step 1: Generate initial signals from the primary strategy
        List<Trade> primarySignals = primaryStrategy.generateSignals(candles, summary);

        // Step 2: Process each signal using the other strategies and apply weight
        Map<String, Integer> buyVotes = new HashMap<>();
        Map<String, Integer> sellVotes = new HashMap<>();

        // Add votes based on primary strategy signals
        for (Trade trade : primarySignals) {
            if ("BUY".equals(trade.type)) {
                buyVotes.put(trade.date, 1);  // Primary strategy gives a base vote
            } else if ("SELL".equals(trade.type)) {
                sellVotes.put(trade.date, 1);  // Primary strategy gives a base vote
            }
        }

        // Step 3: Apply votes from other strategies
        for (int i = 0; i < otherStrategies.size(); i++) {
            Strategy strategy = otherStrategies.get(i);
            double weight = weights.get(i);

            List<Trade> secondarySignals = strategy.generateSignals(candles, summary);
            for (Trade trade : secondarySignals) {
                if ("BUY".equals(trade.type)) {
                    buyVotes.merge(trade.date, (int) (weight * 10), Integer::sum);  // Apply weighted vote (scale by 10 for simplicity)
                } else if ("SELL".equals(trade.type)) {
                    sellVotes.merge(trade.date, (int) (weight * 10), Integer::sum);
                }
            }
        }

        // Step 4: Final decision based on weighted votes
        List<Trade> finalTrades = new ArrayList<>();
        boolean holding = false;

        for (Candle candle : candles) {
            String date = candle.date;

            // Get the weighted votes for buy and sell
            int buyScore = buyVotes.getOrDefault(date, 0);
            int sellScore = sellVotes.getOrDefault(date, 0);

            // If the primary strategy suggested buy and weight + votes support it, trigger a buy
            if (!holding && buyScore > sellScore) {
                finalTrades.add(new Trade(date, "BUY", candle.close));
                holding = true;
            } 
            // If we are holding, check if sell is suggested (by weighted influence of other strategies)
            else if (holding && sellScore > buyScore) {
                finalTrades.add(new Trade(date, "SELL", candle.close));
                holding = false;
            }
        }

        return finalTrades;
    }
}
