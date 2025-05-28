package backtest;

import java.util.List;

import model.Candle;
import model.Summary;
import model.Trade;

public class Backtester {
	public static double run(List<Candle> candles, List<Trade> signals, List<Trade> actualTrades, Summary summary) {
		double balance = summary.getOpeningBalance();
		boolean holdingPosition = summary.isHoldingPosition();
		int tradeCount = summary.getTradeCount();

		int winCount = 0;
		int lossCount = 0;
		double totalProfit = 0.0;
		double totalLoss = 0.0;

		double entryPrice = 0;
		System.out.printf("Initial Balance: $%.2f\n", balance);

		for (Trade trade : signals) {
			if (trade.type.equals("BUY") && !holdingPosition) {
				entryPrice = trade.price;
				holdingPosition = true;
				actualTrades.add(trade);
				tradeCount++;
			} else if (trade.type.equals("SELL") && holdingPosition) {
				double pnl = (trade.price - entryPrice) / entryPrice; //fractional p/l

				if (pnl > 0) {
					winCount++;
					totalProfit += pnl * balance;
				} else {
					totalLoss += pnl * balance;
					lossCount++;
				}

				balance *= (1 + pnl);
				holdingPosition = false;
				trade.profitLoss = trade.price - entryPrice;
				actualTrades.add(trade);
				tradeCount++;
			}
		}

		summary.setHoldingPosition(holdingPosition);
		summary.setNetPL(balance - summary.getOpeningBalance());
		summary.setTradeCount(tradeCount);
		summary.setWinCount(winCount);
		summary.setLossCount(lossCount);
		summary.setTotalLoss(totalLoss);
		summary.setTotalProfit(totalProfit);
		summary.setClosingBalance(balance);
		if (holdingPosition) {
			System.out.println("WARNING: Trade left open at end of data. Unrealized PnL not counted.");
		}
		return balance;
	}
}
