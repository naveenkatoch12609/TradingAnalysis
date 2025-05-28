package model;

public class Trade {
	public String date;
    public String type; // "BUY" or "SELL"
    public double price;
    public Double profitLoss;  // null for buy, set for sell

    public Trade(String date, String type, double price) {
        this.date = date;
        this.type = type.toUpperCase();
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("Trade[%s at %.2f on %s]", type, price, date);
    }
}
