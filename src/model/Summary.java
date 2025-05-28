package model;

public class Summary {
	double openingBalance;
	boolean holdingPosition;
	double netPL;
	int tradeCount;
	String strategyName;
	String strategyDescription;
	int winCount;
	int lossCount;
	double totalProfit;
	double totalLoss;
	double closingBalance;

	public double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public boolean isHoldingPosition() {
		return holdingPosition;
	}

	public void setHoldingPosition(boolean holdingPosition) {
		this.holdingPosition = holdingPosition;
	}

	public double getNetPL() {
		return netPL;
	}

	public void setNetPL(double netPL) {
		this.netPL = netPL;
	}

	public int getTradeCount() {
		return tradeCount;
	}

	public void setTradeCount(int tradeCount) {
		this.tradeCount = tradeCount;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getStrategyDescription() {
		return strategyDescription;
	}

	public void setStrategyDescription(String strategyDescription) {
		this.strategyDescription = strategyDescription;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLossCount() {
		return lossCount;
	}

	public void setLossCount(int lossCount) {
		this.lossCount = lossCount;
	}

	public double getTotalProfit() {
		return totalProfit;
	}

	public void setTotalProfit(double totalProfit) {
		this.totalProfit = totalProfit;
	}

	public double getTotalLoss() {
		return totalLoss;
	}

	public void setTotalLoss(double totalLoss) {
		this.totalLoss = totalLoss;
	}

	public double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(double closingBalance) {
		this.closingBalance = closingBalance;
	}

}
